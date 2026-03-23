/*
 * gate1_features.c — SafeNest Gate 1 feature extraction, URL encoding, allowlist.
 *
 * Self-contained C99 implementation. No external dependencies.
 * Ports the Python feature extraction from test_manual_cases.py.
 */
#include "gate1_features.h"

#include <ctype.h>
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* ── Internal Constants ───────────────────────────────────────────────────── */

static const char* SUSPICIOUS_TLDS[] = {
        "xyz", "top", "buzz", "click", "fun", "sbs", "cfd", "icu", "gq", "ml",
        "cf", "ga", "tk", "work", "rest", "surf", "monster", "cam", "quest",
        NULL
};

static const char* SUSPICIOUS_KEYWORDS[] = {
        "login", "verify", "secure", "account", "update", "confirm", "bank",
        "free", "gift", "prize", "winner", "offer", "limited", "urgent",
        "suspend", "alert", "wallet", "crypto", "earn", "money", "fast",
        "password", "signin", "phishing", NULL
};

static const char* COMPOUND_TLDS[] = {
        "com.vn", "com.au", "com.br", "com.cn", "com.hk", "com.mx",
        "com.my", "com.ng", "com.ph", "com.sg", "com.tw", "com.ua",
        "co.uk", "co.kr", "co.jp", "co.id", "co.in", "co.nz", "co.za",
        "co.th", "org.vn", "gov.vn", "edu.vn", "net.vn", "ac.uk",
        "org.uk", "gov.uk", "gob.mx", "go.id", "or.id", NULL
};

/* ASCII homoglyphs: digit -> letter it looks like */
typedef struct { char digit; char letter; } AsciiHomoglyph;
static const AsciiHomoglyph ASCII_HOMOGLYPHS[] = {
        {'0', 'o'}, {'1', 'l'}, {'3', 'e'}, {'5', 's'}, {0, 0}
};

/* ── Brand List Implementation ────────────────────────────────────────────── */

#define MAX_BRANDS 8000
#define MAX_DOMAIN_LEN 128

typedef struct {
    char domain[MAX_DOMAIN_LEN];   /* full domain: e.g. "techcombank.com.vn" */
    char brand[MAX_DOMAIN_LEN];    /* brand/base: e.g. "techcombank" */
    int  domain_len;
    int  brand_len;
} BrandEntry;

struct Gate1BrandList {
    BrandEntry* entries;
    int count;
    /* Simple hash set for O(1) allowlist lookup */
    uint32_t* domain_hashes;
    int hash_capacity;
    char** domain_strs;    /* parallel array for collision resolution */
};

static uint32_t fnv1a(const char* s, int len) {
    uint32_t h = 2166136261u;
    for (int i = 0; i < len; i++) {
        h ^= (uint8_t)s[i];
        h *= 16777619u;
    }
    return h;
}

Gate1BrandList* gate1_load_brands_from_buffer(const void* data, size_t len) {
    if (!data || len < 4) return NULL;
    const uint8_t* p = (const uint8_t*)data;

    uint32_t count;
    memcpy(&count, p, 4); p += 4;
    if (count > MAX_BRANDS) count = MAX_BRANDS;

    Gate1BrandList* bl = calloc(1, sizeof(Gate1BrandList));
    bl->entries = calloc(count, sizeof(BrandEntry));
    bl->count = (int)count;

    /* Hash set sized at 2x for low collision rate */
    bl->hash_capacity = (int)count * 2 + 1;
    bl->domain_hashes = calloc(bl->hash_capacity, sizeof(uint32_t));
    bl->domain_strs = calloc(bl->hash_capacity, sizeof(char*));

    for (uint32_t i = 0; i < count; i++) {
        if ((size_t)(p - (const uint8_t*)data) >= len) break;
        uint8_t dlen = *p++;
        if (dlen >= MAX_DOMAIN_LEN) dlen = MAX_DOMAIN_LEN - 1;
        memcpy(bl->entries[i].domain, p, dlen);
        bl->entries[i].domain[dlen] = '\0';
        bl->entries[i].domain_len = dlen;
        p += dlen;

        /* Skip the brand display name from binary (e.g. "Vietcombank (VCB)") */
        uint8_t blen = *p++;
        p += blen;

        /* Derive brand base from domain: first label before any dot
         * (matches Python: base = domain.split(".")[0]) */
        {
            const char* dot = strchr(bl->entries[i].domain, '.');
            int base_len = dot ? (int)(dot - bl->entries[i].domain) : dlen;
            if (base_len >= MAX_DOMAIN_LEN) base_len = MAX_DOMAIN_LEN - 1;
            memcpy(bl->entries[i].brand, bl->entries[i].domain, base_len);
            bl->entries[i].brand[base_len] = '\0';
            bl->entries[i].brand_len = base_len;
        }

        /* Insert domain into hash set */
        uint32_t h = fnv1a(bl->entries[i].domain, dlen) % bl->hash_capacity;
        while (bl->domain_strs[h] != NULL) {
            h = (h + 1) % bl->hash_capacity;
        }
        bl->domain_hashes[h] = fnv1a(bl->entries[i].domain, dlen);
        bl->domain_strs[h] = bl->entries[i].domain;
    }
    return bl;
}

Gate1BrandList* gate1_load_brands_from_file(const char* path) {
    FILE* f = fopen(path, "rb");
    if (!f) return NULL;
    fseek(f, 0, SEEK_END);
    long sz = ftell(f);
    fseek(f, 0, SEEK_SET);
    void* buf = malloc(sz);
    if (fread(buf, 1, sz, f) != (size_t)sz) { free(buf); fclose(f); return NULL; }
    fclose(f);
    Gate1BrandList* bl = gate1_load_brands_from_buffer(buf, sz);
    free(buf);
    return bl;
}

void gate1_free_brands(Gate1BrandList* bl) {
    if (!bl) return;
    free(bl->entries);
    free(bl->domain_hashes);
    free(bl->domain_strs);
    free(bl);
}

int gate1_brand_count(const Gate1BrandList* bl) {
    return bl ? bl->count : 0;
}

/* ── URL Parsing Helpers ──────────────────────────────────────────────────── */

typedef struct {
    char scheme[8];      /* "http" or "https" */
    char host[256];      /* hostname (lowercase) */
    char path[1024];     /* path including leading / */
    char query[1024];    /* query string (after ?) */
    int  port;           /* 0 if not specified */
    int  is_https;
} ParsedURL;

static void parse_url(const char* url, ParsedURL* out) {
    memset(out, 0, sizeof(ParsedURL));
    const char* p = url;

    /* Scheme */
    if (strncmp(p, "https://", 8) == 0) {
        strcpy(out->scheme, "https");
        out->is_https = 1;
        p += 8;
    } else if (strncmp(p, "http://", 7) == 0) {
        strcpy(out->scheme, "http");
        p += 7;
    } else {
        p = url;
    }

    /* Host (until / or : or end) */
    int hi = 0;
    while (*p && *p != '/' && *p != ':' && *p != '?' && hi < 255) {
        out->host[hi++] = tolower((unsigned char)*p++);
    }
    out->host[hi] = '\0';

    /* Port */
    if (*p == ':') {
        p++;
        out->port = atoi(p);
        while (*p && *p != '/' && *p != '?') p++;
    }

    /* Path */
    int pi = 0;
    if (*p == '/') {
        while (*p && *p != '?' && pi < 1023) {
            out->path[pi++] = *p++;
        }
    }
    out->path[pi] = '\0';

    /* Default empty path to "/" (matches Python urlparse behaviour) */
    if (pi == 0) {
        out->path[0] = '/';
        out->path[1] = '\0';
    }

    /* Query */
    if (*p == '?') {
        p++;
        int qi = 0;
        while (*p && qi < 1023) {
            out->query[qi++] = *p++;
        }
        out->query[qi] = '\0';
    }
}

/* Get TLD from hostname, handling compound TLDs */
static const char* get_tld(const char* host) {
    /* Check compound TLDs first */
    int hlen = (int)strlen(host);
    for (int i = 0; COMPOUND_TLDS[i]; i++) {
        int clen = (int)strlen(COMPOUND_TLDS[i]);
        if (hlen > clen + 1 && host[hlen - clen - 1] == '.' &&
            strcmp(host + hlen - clen, COMPOUND_TLDS[i]) == 0) {
            return COMPOUND_TLDS[i];
        }
    }
    /* Simple TLD: last dot */
    const char* last_dot = strrchr(host, '.');
    return last_dot ? last_dot + 1 : host;
}

/* Get registered domain (domain + TLD) */
static void get_registered_domain(const char* host, char* out, int maxlen) {
    const char* tld = get_tld(host);
    int tld_start = (int)(tld - host);
    if (tld_start <= 0) {
        strncpy(out, host, maxlen - 1);
        out[maxlen - 1] = '\0';
        return;
    }
    /* Find dot before TLD part */
    int i = tld_start - 2;  /* skip the dot before TLD */
    while (i >= 0 && host[i] != '.') i--;
    const char* start = (i >= 0) ? host + i + 1 : host;
    int len = (int)strlen(start);
    if (len >= maxlen) len = maxlen - 1;
    memcpy(out, start, len);
    out[len] = '\0';
}

/* Get domain without TLD */
static void get_domain_no_tld(const char* host, char* out, int maxlen) {
    char reg[256];
    get_registered_domain(host, reg, sizeof(reg));
    const char* tld = get_tld(host);
    int tld_len = (int)strlen(tld);
    int reg_len = (int)strlen(reg);
    int base_len = reg_len - tld_len - 1;  /* minus the dot */
    if (base_len <= 0) {
        strncpy(out, reg, maxlen - 1);
        out[maxlen - 1] = '\0';
        return;
    }
    if (base_len >= maxlen) base_len = maxlen - 1;
    memcpy(out, reg, base_len);
    out[base_len] = '\0';
}

/* ── Levenshtein Distance ─────────────────────────────────────────────────── */

int gate1_levenshtein(const char* a, int len_a, const char* b, int len_b) {
    if (len_a == 0) return len_b;
    if (len_b == 0) return len_a;
    /* Single-row DP */
    int* row = (int*)malloc((len_b + 1) * sizeof(int));
    for (int j = 0; j <= len_b; j++) row[j] = j;
    for (int i = 1; i <= len_a; i++) {
        int prev = row[0];
        row[0] = i;
        for (int j = 1; j <= len_b; j++) {
            int old = row[j];
            int cost = (a[i-1] == b[j-1]) ? 0 : 1;
            int del = row[j] + 1;
            int ins = row[j-1] + 1;
            int sub = prev + cost;
            row[j] = del < ins ? (del < sub ? del : sub) : (ins < sub ? ins : sub);
            prev = old;
        }
    }
    int result = row[len_b];
    free(row);
    return result;
}

/* ── Allowlist Check ──────────────────────────────────────────────────────── */

static int hash_lookup(const Gate1BrandList* bl, const char* domain) {
    int dlen = (int)strlen(domain);
    uint32_t h = fnv1a(domain, dlen) % bl->hash_capacity;
    for (int probe = 0; probe < bl->hash_capacity; probe++) {
        int idx = (h + probe) % bl->hash_capacity;
        if (bl->domain_strs[idx] == NULL) return 0;
        if (strcmp(bl->domain_strs[idx], domain) == 0) return 1;
    }
    return 0;
}

int gate1_is_allowlisted(const char* url, const Gate1BrandList* brands) {
    if (!brands) return 0;
    ParsedURL pu;
    parse_url(url, &pu);
    char reg[256];
    get_registered_domain(pu.host, reg, sizeof(reg));
    if (hash_lookup(brands, reg)) return 1;
    if (hash_lookup(brands, pu.host)) return 1;
    return 0;
}

/* ── URL Encoding ─────────────────────────────────────────────────────────── */

Gate1URLEncoding gate1_encode_url(const char* url) {
    Gate1URLEncoding enc;
    memset(&enc, 0, sizeof(enc));
    int len = (int)strlen(url);
    int max = len < GATE1_MAX_URL_LEN ? len : GATE1_MAX_URL_LEN;
    for (int i = 0; i < max; i++) {
        unsigned char ch = (unsigned char)url[i];
        int v = (int)ch;
        enc.ids[i] = (v > 96 ? 96 : v) + 1;  /* min(ord(ch), 96) + 1 */
    }
    return enc;
}

/* ── Feature Extraction ───────────────────────────────────────────────────── */

static int is_ip_address(const char* host) {
    /* Check IPv4 */
    int dots = 0, digits = 0;
    for (const char* p = host; *p; p++) {
        if (*p == '.') { dots++; digits = 0; }
        else if (isdigit((unsigned char)*p)) digits++;
        else return 0;
    }
    if (dots == 3 && digits > 0) return 1;
    /* Check hex IP (0x...) */
    if (strncmp(host, "0x", 2) == 0 || strncmp(host, "0X", 2) == 0) return 1;
    /* Check octal IP (0NNN.0NNN...) */
    if (host[0] == '0' && isdigit((unsigned char)host[1])) return 1;
    return 0;
}

static float compute_entropy(const char* s) {
    int counts[256] = {0};
    int total = 0;
    for (const char* p = s; *p; p++) {
        counts[(unsigned char)*p]++;
        total++;
    }
    if (total == 0) return 0.0f;
    float entropy = 0.0f;
    for (int i = 0; i < 256; i++) {
        if (counts[i] > 0) {
            float p = (float)counts[i] / total;
            entropy -= p * log2f(p);
        }
    }
    return entropy;
}

static int count_subdomains(const char* host) {
    int dots = 0;
    for (const char* p = host; *p; p++) {
        if (*p == '.') dots++;
    }
    return dots > 0 ? dots - 1 : 0;
}

static int is_suspicious_tld(const char* tld) {
    for (int i = 0; SUSPICIOUS_TLDS[i]; i++) {
        if (strcmp(tld, SUSPICIOUS_TLDS[i]) == 0) return 1;
    }
    return 0;
}

static int count_suspicious_keywords(const char* url_lower) {
    int count = 0;
    for (int i = 0; SUSPICIOUS_KEYWORDS[i]; i++) {
        if (strstr(url_lower, SUSPICIOUS_KEYWORDS[i]) != NULL) count++;
    }
    return count;
}

static int count_special_chars(const char* s) {
    int count = 0;
    for (const char* p = s; *p; p++) {
        if (!isalnum((unsigned char)*p) && *p != '.' && *p != '/') count++;
    }
    return count;
}

static int max_consecutive_repeat(const char* s) {
    int max_rep = 1, cur = 1;
    for (int i = 1; s[i]; i++) {
        if (s[i] == s[i-1]) { cur++; if (cur > max_rep) max_rep = cur; }
        else cur = 1;
    }
    return max_rep;
}

static int count_query_params(const char* query) {
    if (!query[0]) return 0;
    int count = 1;
    for (const char* p = query; *p; p++) {
        if (*p == '&') count++;
    }
    return count;
}

static int has_encoded_chars(const char* url) {
    for (const char* p = url; *p; p++) {
        if (*p == '%' && isxdigit((unsigned char)p[1]) && isxdigit((unsigned char)p[2]))
            return 1;
    }
    return 0;
}

/* Homograph analysis (matches Python: iterates over full domain, uses len(domain) as denominator)
 * Python CONFUSABLES includes: digits 0→o, 1→l, 3→e, 5→s plus Unicode confusables.
 * Python mixed_scripts = has_latin AND has_non_ascii (>127). ASCII homoglyphs do NOT trigger mixed. */
static void compute_homograph(const char* domain,
                              float* score, int* confusable_count, int* mixed_scripts) {
    int total = (int)strlen(domain);
    int conf = 0;
    int has_latin = 0, has_non_ascii = 0;
    for (const char* p = domain; *p; p++) {
        unsigned char ch = (unsigned char)*p;
        /* ASCII homoglyphs (digits that look like letters) */
        for (int i = 0; ASCII_HOMOGLYPHS[i].digit; i++) {
            if ((char)ch == ASCII_HOMOGLYPHS[i].digit) {
                conf++;
                goto next_char;
            }
        }
        if (ch <= 127) {
            has_latin = 1;
        }
        if (ch > 127) {
            /* Non-ASCII: Unicode confusable */
            conf++;
            has_non_ascii = 1;
        }
        next_char: ;
    }
    *score = total > 0 ? (float)conf / total : 0.0f;
    *confusable_count = conf;
    /* Python: has_mixed = has_latin and has_non_ascii (ASCII homoglyphs don't count) */
    *mixed_scripts = (has_latin && has_non_ascii) ? 1 : 0;
}

static int is_idn(const char* host) {
    /* Matches Python: is_idn = any(ord(ch) > 127 for ch in domain) */
    for (const char* p = host; *p; p++) {
        if ((unsigned char)*p > 127) return 1;
    }
    return 0;
}

/* Simplified phonetic comparison */
static void simplify_phonetic(const char* s, char* out, int maxlen) {
    int oi = 0;
    for (int i = 0; s[i] && oi < maxlen - 1; i++) {
        char ch = tolower((unsigned char)s[i]);
        if (!isalpha((unsigned char)ch)) continue;
        /* ph -> f */
        if (ch == 'p' && s[i+1] == 'h') { out[oi++] = 'f'; i++; continue; }
        /* ck -> k */
        if (ch == 'c' && s[i+1] == 'k') { out[oi++] = 'k'; i++; continue; }
        /* Skip consecutive duplicates */
        if (oi > 0 && out[oi-1] == ch) continue;
        out[oi++] = ch;
    }
    out[oi] = '\0';
}

static int sounds_alike(const char* a, const char* b) {
    char sa[128], sb[128];
    simplify_phonetic(a, sa, sizeof(sa));
    simplify_phonetic(b, sb, sizeof(sb));
    return strcmp(sa, sb) == 0;
}

/* Segment-based brand containment (matches Python _compute_containment).
 * Receives full domain (e.g. "vietcombank-verify.xyz"), strips TLD internally. */
static float compute_containment(const char* full_domain, const Gate1BrandList* brands) {
    if (!brands || brands->count == 0) return 0.0f;

    /* Strip TLD to get base (matches Python logic) */
    char buf[256];
    strncpy(buf, full_domain, sizeof(buf) - 1);
    buf[sizeof(buf) - 1] = '\0';
    for (int i = 0; buf[i]; i++) buf[i] = tolower((unsigned char)buf[i]);

    /* Find base: everything before the TLD portion.
     * Python: parts = domain.split("."), then base = ".".join(parts[:-1]) for simple TLDs
     *         or ".".join(parts[:-2]) for compound TLDs */
    const char* tld = get_tld(buf);
    int tld_offset = (int)(tld - buf);
    if (tld_offset > 0) {
        buf[tld_offset - 1] = '\0';  /* Remove ".tld" */
    }

    /* Split base on [-._] and check segments */
    float best = 0.0f;
    char* token = strtok(buf, "-._");
    while (token) {
        int tlen = (int)strlen(token);
        if (tlen >= 3) {
            for (int i = 0; i < brands->count; i++) {
                int blen = brands->entries[i].brand_len;
                if (blen < 3) continue;
                /* Exact segment match */
                if (blen == tlen && strcmp(token, brands->entries[i].brand) == 0) {
                    return 1.0f;
                }
                /* Substring containment */
                if (strstr(token, brands->entries[i].brand) != NULL) {
                    float ratio = (float)blen / tlen;
                    if (ratio >= 0.6f && ratio > best) {
                        best = ratio;
                    }
                }
            }
        }
        token = strtok(NULL, "-._");
    }
    return best >= 0.6f ? best : 0.0f;
}

/* Typosquat features: compare domain against all brands */
static void compute_typosquat(const char* domain_no_tld, const Gate1BrandList* brands,
                              float* similarity, int* distance, int* is_match,
                              int* is_tld_swap, int* psounds_alike,
                              int has_suspicious_tld) {
    *similarity = 0.0f;
    *distance = 99;
    *is_match = 0;
    *is_tld_swap = 0;
    *psounds_alike = 0;

    if (!brands || brands->count == 0) return;

    int base_len = (int)strlen(domain_no_tld);
    if (base_len == 0) return;

    float best_sim = 0.0f;
    int best_dist = 99;

    for (int i = 0; i < brands->count; i++) {
        int blen = brands->entries[i].brand_len;
        /* Length pre-filter: skip if length diff > 3 */
        if (abs(blen - base_len) > 3) continue;

        int dist = gate1_levenshtein(domain_no_tld, base_len,
                                     brands->entries[i].brand, blen);
        int maxlen = base_len > blen ? base_len : blen;
        float sim = maxlen > 0 ? 1.0f - (float)dist / maxlen : 0.0f;

        if (sim > best_sim) {
            best_sim = sim;
            best_dist = dist;

            /* TLD swap: domain exactly matches brand AND TLD is suspicious
             * (matches Python: domain_base == brand and has_suspicious_tld > 0) */
            if (dist == 0 && has_suspicious_tld) {
                *is_tld_swap = 1;
            }

            /* Typosquat match: close but not exact (1 or 2 edits away)
             * (matches Python: 0 < dist <= 2 and domain_base != brand) */
            if (dist > 0 && dist <= 2) {
                *is_match = 1;
            }

            /* Sounds-alike check (only for close but non-exact matches)
             * (matches Python: _sounds_like(...) and domain_base != brand) */
            if (dist > 0 && sounds_alike(domain_no_tld, brands->entries[i].brand)) {
                *psounds_alike = 1;
            }
        }
    }

    *similarity = best_sim;
    *distance = best_dist;
}

/* ── Main Feature Extraction ──────────────────────────────────────────────── */

Gate1Features gate1_extract_features(const char* url, const Gate1BrandList* brands) {
    Gate1Features feat;
    memset(&feat, 0, sizeof(feat));

    /* Parse URL */
    ParsedURL pu;
    parse_url(url, &pu);

    /* Make lowercase URL for keyword matching */
    char url_lower[2048];
    int url_len = (int)strlen(url);
    for (int i = 0; i < url_len && i < 2047; i++)
        url_lower[i] = tolower((unsigned char)url[i]);
    url_lower[url_len < 2047 ? url_len : 2047] = '\0';

    /* Domain analysis */
    const char* tld = get_tld(pu.host);
    char domain_no_tld[256];
    get_domain_no_tld(pu.host, domain_no_tld, sizeof(domain_no_tld));
    int domain_len = (int)strlen(pu.host);

    /* Path analysis — count non-empty segments (matches Python path.split("/") filter) */
    int path_len = (int)strlen(pu.path);
    int path_depth = 0;
    int in_segment = 0;
    for (int i = 0; i < path_len; i++) {
        if (pu.path[i] == '/') {
            in_segment = 0;
        } else if (!in_segment) {
            in_segment = 1;
            path_depth++;
        }
    }

    /* Character ratios (computed on full URL) */
    int digits = 0, vowels = 0, consonants = 0;
    for (int i = 0; i < url_len; i++) {
        char ch = tolower((unsigned char)url[i]);
        if (isdigit((unsigned char)url[i])) digits++;
        if (isalpha((unsigned char)url[i])) {
            if (ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u')
                vowels++;
            else
                consonants++;
        }
    }

    /* Homograph features (computed on full domain, matching Python) */
    float homo_score;
    int homo_conf, homo_mixed;
    compute_homograph(pu.host, &homo_score, &homo_conf, &homo_mixed);

    /* Strip hyphens from domain for typosquat comparison (matches Python) */
    char domain_stripped[256];
    int si = 0;
    for (int i = 0; domain_no_tld[i] && si < 255; i++) {
        if (domain_no_tld[i] != '-')
            domain_stripped[si++] = tolower((unsigned char)domain_no_tld[i]);
    }
    domain_stripped[si] = '\0';

    /* Typosquat features */
    int suspicious_tld = is_suspicious_tld(tld);
    float typo_sim;
    int typo_dist, typo_match, typo_tld_swap, typo_sounds;
    compute_typosquat(domain_stripped, brands,
                      &typo_sim, &typo_dist, &typo_match,
                      &typo_tld_swap, &typo_sounds,
                      suspicious_tld);

    /* Containment: computed on full domain (matches Python _compute_containment(domain, brand_set)) */
    float typo_containment = compute_containment(pu.host, brands);

    /* Has dash in domain (not in URL scheme/path) */
    int has_dash = (strchr(pu.host, '-') != NULL) ? 1 : 0;

    /* Fill feature vector (order must match FEATURE_NAMES) */
    feat.values[0]  = (float)url_len;                           /* url_length */
    feat.values[1]  = (float)domain_len;                        /* domain_length */
    feat.values[2]  = (float)path_len;                          /* path_length */
    feat.values[3]  = (float)path_depth;                        /* path_depth */
    feat.values[4]  = (float)count_subdomains(pu.host);         /* subdomain_count */
    feat.values[5]  = (float)is_ip_address(pu.host);            /* has_ip_address */
    feat.values[6]  = (float)(pu.port > 0 ? 1 : 0);            /* has_port */
    feat.values[7]  = (float)pu.is_https;                       /* is_https */
    feat.values[8]  = (float)count_special_chars(url);          /* special_char_count */
    feat.values[9]  = url_len > 0 ? (float)digits / url_len : 0; /* digit_ratio */
    int total_alpha = vowels + consonants;
    feat.values[10] = total_alpha > 0 ? (float)vowels / total_alpha : 0; /* vowel_ratio */
    feat.values[11] = total_alpha > 0 ? (float)consonants / total_alpha : 0; /* consonant_ratio */
    feat.values[12] = compute_entropy(url);                     /* entropy */
    feat.values[13] = (float)suspicious_tld;                     /* has_suspicious_tld */
    feat.values[14] = (float)strlen(tld);                       /* tld_length */
    feat.values[15] = (float)has_dash;                          /* has_dash */
    feat.values[16] = (float)max_consecutive_repeat(pu.host);    /* consecutive_char_repeat */
    feat.values[17] = (float)count_query_params(pu.query);      /* query_param_count */
    feat.values[18] = (float)has_encoded_chars(url);            /* has_encoded_chars */
    feat.values[19] = (float)count_suspicious_keywords(url_lower); /* suspicious_keyword_count */
    feat.values[20] = homo_score;                               /* homograph_score */
    feat.values[21] = (float)homo_conf;                         /* homograph_confusable_count */
    feat.values[22] = (float)homo_mixed;                        /* has_mixed_scripts */
    feat.values[23] = (float)is_idn(pu.host);                   /* is_idn */
    feat.values[24] = typo_sim;                                 /* typosquat_similarity */
    feat.values[25] = (float)typo_dist;                         /* typosquat_distance */
    feat.values[26] = (float)typo_match;                        /* typosquat_is_match */
    feat.values[27] = (float)typo_tld_swap;                     /* typosquat_is_tld_swap */
    feat.values[28] = (float)typo_sounds;                       /* typosquat_sounds_alike */
    feat.values[29] = typo_containment;                         /* typosquat_containment */

    return feat;
}
