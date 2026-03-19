/*
 * gate1_features.h — SafeNest Gate 1 URL Safety Classifier
 *
 * Portable C99 library for URL feature extraction, encoding, and allowlist check.
 * Designed for iOS (via bridging header) and Android (via JNI).
 */
#ifndef GATE1_FEATURES_H
#define GATE1_FEATURES_H

#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

/* ── Constants ────────────────────────────────────────────────────────────── */

#define GATE1_N_FEATURES     30
#define GATE1_MAX_URL_LEN   200
#define GATE1_VOCAB_SIZE     98
#define GATE1_THRESHOLD     0.2f

/* ── Types ────────────────────────────────────────────────────────────────── */

/** Extracted feature vector (30 floats). */
typedef struct {
    float values[GATE1_N_FEATURES];
} Gate1Features;

/** Encoded URL (integer IDs for model input). */
typedef struct {
    int32_t ids[GATE1_MAX_URL_LEN];
} Gate1URLEncoding;

/** Classification result. */
typedef struct {
    float probability;  /* Sigmoid of model logit, 0.0-1.0 */
    int   is_scam;      /* 1 if prob >= threshold, 0 otherwise */
    int   is_allowlisted; /* 1 if domain was in allowlist */
} Gate1Result;

/** Opaque brand list handle. */
typedef struct Gate1BrandList Gate1BrandList;

/* ── Brand List Management ────────────────────────────────────────────────── */

/** Load brand list from binary file (brands.bin).
 *  Returns NULL on failure. Caller must free with gate1_free_brands(). */
Gate1BrandList* gate1_load_brands_from_file(const char* path);

/** Load brand list from in-memory buffer (brands.bin contents). */
Gate1BrandList* gate1_load_brands_from_buffer(const void* data, size_t len);

/** Free brand list. */
void gate1_free_brands(Gate1BrandList* brands);

/** Get number of brands loaded. */
int gate1_brand_count(const Gate1BrandList* brands);

/* ── Core Functions ───────────────────────────────────────────────────────── */

/** Check if URL's registered domain is in the allowlist.
 *  Returns 1 if allowlisted (safe), 0 otherwise. */
int gate1_is_allowlisted(const char* url, const Gate1BrandList* brands);

/** Extract 30 engineered features from a URL.
 *  Requires brand list for typosquat/containment features. */
Gate1Features gate1_extract_features(const char* url, const Gate1BrandList* brands);

/** Encode URL characters to integer IDs for model input.
 *  Encoding: min(ord(ch), 96) + 1, zero-padded to GATE1_MAX_URL_LEN. */
Gate1URLEncoding gate1_encode_url(const char* url);

/* ── Utility ──────────────────────────────────────────────────────────────── */

/** Compute Levenshtein distance between two strings. */
int gate1_levenshtein(const char* a, int len_a, const char* b, int len_b);

#ifdef __cplusplus
}
#endif
#endif /* GATE1_FEATURES_H */
