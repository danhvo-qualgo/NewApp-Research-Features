/*
 * gate1_features.h — SafeNest Gate 1 URL Safety Classifier (LightGBM v1.0)
 *
 * Portable C99 library for URL feature extraction and allowlist check.
 * Designed for iOS (via bridging header) and Android (via JNI).
 *
 * Changes from v0 (Late Fusion, 30 features):
 *   - 33 features (added: subdomain_depth, domain_char_entropy, typosquat_risk_score,
 *     relative_edit_distance, homoglyph_edit_ratio, brand_in_subdomain,
 *     suspicious_tld_x_has_dash, scam_db_hit)
 *   - No URL encoding needed (LightGBM is features-only, no CNN branch)
 */
#ifndef GATE1_FEATURES_H
#define GATE1_FEATURES_H

#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

/* ── Constants ────────────────────────────────────────────────────────────── */

#define GATE1_N_FEATURES     33
#define GATE1_THRESHOLD     0.2f

/* ── Types ────────────────────────────────────────────────────────────────── */

/** Extracted feature vector (33 floats). */
typedef struct {
    float values[GATE1_N_FEATURES];
} Gate1Features;

/** Classification result. */
typedef struct {
    float probability;  /* Model output probability, 0.0-1.0 */
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

/** Extract 33 engineered features from a URL.
 *  Requires brand list for typosquat/containment features. */
Gate1Features gate1_extract_features(const char* url, const Gate1BrandList* brands);

/* ── Utility ──────────────────────────────────────────────────────────────── */

/** Compute Levenshtein distance between two strings. */
int gate1_levenshtein(const char* a, int len_a, const char* b, int len_b);

#ifdef __cplusplus
}
#endif
#endif /* GATE1_FEATURES_H */
