/*
 * gate1_features.c — SafeNest Gate 1 feature extraction, allowlist (LightGBM v1.0).
 *
 * Self-contained C99 implementation. No external dependencies.
 * Extracts 33 features matching the Python training pipeline in train_lightgbm.py.
 *
 * Feature order (must match FEATURE_NAMES):
 *   0  url_length              1  domain_length           2  subdomain_depth
 *   3  domain_char_entropy     4  subdomain_count         5  has_ip_address
 *   6  has_port                7  is_https                8  special_char_count
 *   9  digit_ratio            10  vowel_ratio            11  consonant_ratio
 *  12  entropy                13  has_suspicious_tld     14  tld_length
 *  15  has_dash               16  consecutive_char_repeat 17 suspicious_tld_x_has_dash
 *  18  has_encoded_chars      19  suspicious_keyword_count
 *  20  homograph_score        21  homograph_confusable_count
 *  22  has_mixed_scripts      23  is_idn
 *  24  typosquat_similarity   25  typosquat_distance     26 typosquat_risk_score
 *  27  typosquat_is_tld_swap  28  typosquat_sounds_alike
 *  29  relative_edit_distance  30 homoglyph_edit_ratio   31 brand_in_subdomain
 *  32  scam_db_hit
 */
#include "gate1_features.h"

#include <ctype.h>
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* ── Internal Constants ───────────────────────────────────────────────────── */

static const char* SUSPICIOUS_TLDS[] = {
    "xyz", "top", "buzz", "club", "work", "info", "site", "online",
    "icu", "cyou", "tk", "ml", "ga", "cf", "gq", "pw", "cc",
    "click", "link", "live", "support", "rest", "monster",
    "store", "stream", "download", "review", "country",
    "cricket", "science", "party", "gdn", "racing", "win",
    "bid", "trade", "webcam", "date", "faith", "loan",
    "fun", "sbs", "cfd", "cam", "beauty", "hair", "quest", "one",
    "today", "world", "surf", "bar", NULL
};

static const char* SUSPICIOUS_KEYWORDS[] = {
    "login", "signin", "sign-in", "verify", "verification",
    "secure", "security", "update", "confirm", "account",
    "banking", "bank", "paypal", "apple", "microsoft",
    "support", "recover", "restore", "unlock", "suspend",
    "expired", "alert", "urgent", "immediately", "password",
    "credential", "authenticate", "wallet", "reward", "prize",
    "free", "gift", "offer", "bonus", "promo", NULL
};

static const char* COMPOUND_TLDS[] = {
    "com.vn", "com.au", "com.br", "com.cn", "com.hk", "com.mx",
    "com.my", "com.ng", "com.ph", "com.sg", "com.tw", "com.ua",
    "co.uk", "co.kr", "co.jp", "co.id", "co.in", "co.nz", "co.za",
    "co.th", "org.vn", "gov.vn", "edu.vn", "net.vn", "ac.uk",
    "org.uk", "gov.uk", "gob.mx", "go.id", "or.id", NULL
};

/* Homoglyph map: multi-char and single-char substitutions */
typedef struct { const char* from; const char* to; } HomoglyphEntry;
static const HomoglyphEntry HOMOGLYPH_MAP[] = {
    {"vv", "w"}, {"rn", "m"}, {"cl", "d"},
    {"0", "o"}, {"1", "l"}, {"3", "e"}, {"5", "s"},
    {NULL, NULL}
};

/* ASCII homoglyphs for homograph scoring */
typedef struct { char digit; char letter; } AsciiHomoglyph;
static const AsciiHomoglyph ASCII_HOMOGLYPHS[] = {
    {'0', 'o'}, {'1', 'l'}, {'3', 'e'}, {'5', 's'}, {0, 0}
};

#define MIN_BRAND_BASE_LEN 4

/* ── Brand List Implementation ────────────────────────────────────────────── */

#define MAX_BRANDS 8000
#define MAX_DOMAIN_LEN 128

typedef struct {
    char domain[MAX_DOMAIN_LEN];   /* full domain: e.g. "techcombank.com.vn" */
    char brand[MAX_DOMAIN_LEN];    /* brand/base: e.g. "techcombank" */
    char tld[MAX_DOMAIN_LEN];      /* tld: e.g. "com.vn" */
    int  domain_len;
    int  brand_len;
} BrandEntry;

struct Gate1BrandList {
    BrandEntry* entries;
    int count;
    uint32_t* domain_hashes;
    int hash_capacity;
    char** domain_strs;
};

static uint32_t fnv1a(const char* s, int len) {
    uint32_t h = 2166136261u;
    for (int i = 0; i < len; i++) {
        h ^= (uint8_t)s[i];
        h *= 16777619u;
    }
    return h;
}

/* Extract TLD from a domain string */
static void extract_tld_from_domain(const char* domain, char* tld_out, int maxlen) {
    int dlen = (int)strlen(domain);
    /* Check compound TLDs first */
    for (int i = 0; COMPOUND_TLDS[i]; i++) {
        int clen = (int)strlen(COMPOUND_TLDS[i]);
        if (dlen > clen + 1 && domain[dlen - clen - 1] == '.' &&
            strcmp(domain + dlen - clen, COMPOUND_TLDS[i]) == 0) {
            int len = clen < maxlen - 1 ? clen : maxlen - 1;
            memcpy(tld_out, COMPOUND_TLDS[i], len);
            tld_out[len] = '\0';
            return;
        }
    }
    /* Simple TLD */
    const char* last_dot = strrchr(domain, '.');
    if (last_dot) {
        const char* t = last_dot + 1;
        int len = (int)strlen(t);
        if (len >= maxlen) len = maxlen - 1;
        memcpy(tld_out, t, len);
        tld_out[len] = '\0';
    } else {
        tld_out[0] = '\0';
    }
}

/* Extract base name (domain without TLD) from a domain string */
static void extract_base_from_domain(const char* domain, const char* tld, char* base_out, int maxlen) {
    int dlen = (int)strlen(domain);
    int tlen = (int)strlen(tld);
    int base_len = dlen - tlen - 1;  /* minus the dot */
    if (base_len <= 0) {
        strncpy(base_out, domain, maxlen - 1);
        base_out[maxlen - 1] = '\0';
        return;
    }
    if (base_len >= maxlen) base_len = maxlen - 1;
    memcpy(base_out, domain, base_len);
    base_out[base_len] = '\0';
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

        uint8_t blen = *p++;
        if (blen >= MAX_DOMAIN_LEN) blen = MAX_DOMAIN_LEN - 1;
        memcpy(bl->entries[i].brand, p, blen);
        bl->entries[i].brand[blen] = '\0';
        bl->entries[i].brand_len = blen;
        p += blen;

        /* Extract TLD and base name from domain (not from brands.bin brand field,
         * which may contain display names like "twitter/x" or "vietcombank (vcb)").
         * Python uses tldextract(domain).domain as the base for comparison. */
        extract_tld_from_domain(bl->entries[i].domain, bl->entries[i].tld, MAX_DOMAIN_LEN);

        /* Overwrite brand with base name extracted from domain */
        /* First, strip any subdomain by finding the registered domain part */
        {
            char reg[MAX_DOMAIN_LEN];
            /* For domains like "blog.twitter.com", we need "twitter" not "blog" */
            /* Simple approach: find the part just before the TLD */
            const char* dom = bl->entries[i].domain;
            const char* tld = bl->entries[i].tld;
            int dom_len = bl->entries[i].domain_len;
            int tld_len = (int)strlen(tld);

            if (tld_len > 0 && dom_len > tld_len + 1) {
                /* Find the dot before the TLD */
                int base_end = dom_len - tld_len - 1; /* position of dot before TLD */
                /* Find the previous dot (start of base name) */
                int base_start = base_end - 1;
                while (base_start >= 0 && dom[base_start] != '.') base_start--;
                base_start++; /* skip the dot or start from 0 */

                int blen = base_end - base_start;
                if (blen > 0 && blen < MAX_DOMAIN_LEN) {
                    memcpy(bl->entries[i].brand, dom + base_start, blen);
                    bl->entries[i].brand[blen] = '\0';
                    bl->entries[i].brand_len = blen;
                }
            } else if (tld_len == 0) {
                /* No TLD — use entire domain as brand */
                strncpy(bl->entries[i].brand, dom, MAX_DOMAIN_LEN - 1);
                bl->entries[i].brand[MAX_DOMAIN_LEN - 1] = '\0';
                bl->entries[i].brand_len = dom_len;
            }
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
    char scheme[8];
    char host[256];
    int  port;
    int  is_https;
} ParsedURL;

static void parse_url(const char* url, ParsedURL* out) {
    memset(out, 0, sizeof(ParsedURL));
    const char* p = url;

    if (strncmp(p, "https://", 8) == 0) {
        strcpy(out->scheme, "https");
        out->is_https = 1;
        p += 8;
    } else if (strncmp(p, "http://", 7) == 0) {
        strcpy(out->scheme, "http");
        p += 7;
    }

    int hi = 0;
    while (*p && *p != '/' && *p != ':' && *p != '?' && hi < 255) {
        out->host[hi++] = tolower((unsigned char)*p++);
    }
    out->host[hi] = '\0';

    if (*p == ':') {
        p++;
        out->port = atoi(p);
    }
}

/* Get TLD from hostname, handling compound TLDs.
 * IMPORTANT: always returns a pointer INTO the host string (not into the
 * static COMPOUND_TLDS array), so that (tld - host) gives the correct offset. */
static const char* get_tld(const char* host) {
    int hlen = (int)strlen(host);
    for (int i = 0; COMPOUND_TLDS[i]; i++) {
        int clen = (int)strlen(COMPOUND_TLDS[i]);
        if (hlen > clen + 1 && host[hlen - clen - 1] == '.' &&
            strcmp(host + hlen - clen, COMPOUND_TLDS[i]) == 0) {
            return host + hlen - clen;  /* pointer into host, not static array */
        }
    }
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
    int i = tld_start - 2;
    while (i >= 0 && host[i] != '.') i--;
    const char* start = (i >= 0) ? host + i + 1 : host;
    int len = (int)strlen(start);
    if (len >= maxlen) len = maxlen - 1;
    memcpy(out, start, len);
    out[len] = '\0';
}

/* Get domain name without TLD */
static void get_domain_no_tld(const char* host, char* out, int maxlen) {
    char reg[256];
    get_registered_domain(host, reg, sizeof(reg));
    const char* tld = get_tld(host);
    int tld_len = (int)strlen(tld);
    int reg_len = (int)strlen(reg);
    int base_len = reg_len - tld_len - 1;
    if (base_len <= 0) {
        strncpy(out, reg, maxlen - 1);
        out[maxlen - 1] = '\0';
        return;
    }
    if (base_len >= maxlen) base_len = maxlen - 1;
    memcpy(out, reg, base_len);
    out[base_len] = '\0';
}

/* Get subdomain (everything before registered domain) */
static void get_subdomain(const char* host, char* out, int maxlen) {
    char reg[256];
    get_registered_domain(host, reg, sizeof(reg));
    int reg_start = (int)(strstr(host, reg) - host);
    if (reg_start <= 1) {
        out[0] = '\0';
        return;
    }
    int len = reg_start - 1; /* exclude trailing dot */
    if (len >= maxlen) len = maxlen - 1;
    memcpy(out, host, len);
    out[len] = '\0';
}

/* ── Levenshtein Distance ─────────────────────────────────────────────────── */

int gate1_levenshtein(const char* a, int len_a, const char* b, int len_b) {
    if (len_a == 0) return len_b;
    if (len_b == 0) return len_a;
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

/* ── Helper Functions ─────────────────────────────────────────────────────── */

static int is_ip_address(const char* host) {
    int dots = 0, digits = 0;
    for (const char* p = host; *p; p++) {
        if (*p == '.') { dots++; digits = 0; }
        else if (isdigit((unsigned char)*p)) digits++;
        else return 0;
    }
    if (dots == 3 && digits > 0) return 1;
    if (strncmp(host, "0x", 2) == 0 || strncmp(host, "0X", 2) == 0) return 1;
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

static int count_subdomain_parts(const char* subdomain) {
    if (!subdomain[0]) return 0;
    int count = 1;
    for (const char* p = subdomain; *p; p++) {
        if (*p == '.') count++;
    }
    return count;
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

static int count_special_chars(const char* domain) {
    int count = 0;
    for (const char* p = domain; *p; p++) {
        if (*p == '-' || *p == '_' || *p == '.' || *p == '@' || *p == '~' || *p == '!')
            count++;
    }
    return count;
}

static int has_consecutive_repeat(const char* s) {
    /* Check for 3+ consecutive identical chars */
    int cur = 1;
    for (int i = 1; s[i]; i++) {
        if (s[i] == s[i-1]) { cur++; if (cur >= 3) return 1; }
        else cur = 1;
    }
    return 0;
}

static int has_encoded_chars(const char* url) {
    for (const char* p = url; *p; p++) {
        if (*p == '%' && isxdigit((unsigned char)p[1]) && isxdigit((unsigned char)p[2]))
            return 1;
    }
    return 0;
}

/* Homograph analysis — matches Python _homograph_score() exactly.
 * Python only checks UNICODE_CONFUSABLES (non-ASCII chars like Cyrillic/Greek),
 * NOT ASCII digit homoglyphs (those are handled separately in normalize_homoglyphs). */
static void compute_homograph(const char* domain_no_tld,
                              float* score, int* confusable_count,
                              int* mixed_scripts, int* pidn) {
    int conf = 0;
    int has_ascii_alpha = 0;
    int has_nonascii = 0;

    for (const char* p = domain_no_tld; *p; p++) {
        if (*p == '.') continue;
        unsigned char ch = (unsigned char)*p;
        if (ch > 127) {
            /* Non-ASCII: could be Unicode confusable (Cyrillic, Greek, etc.) */
            conf++;
            has_nonascii = 1;
        } else if (isalpha(ch)) {
            has_ascii_alpha = 1;
        }
    }

    /* Python: min(confusable_count * 0.3, 1.0) */
    *score = conf > 0 ? ((float)conf * 0.3f < 1.0f ? (float)conf * 0.3f : 1.0f) : 0.0f;
    *confusable_count = conf;
    /* Python: has_mixed = any(ch in UNICODE_CONFUSABLES) and any(ascii alpha) */
    *mixed_scripts = (has_nonascii && has_ascii_alpha) ? 1 : 0;
    *pidn = has_nonascii ? 1 : 0;
}

static int is_idn(const char* host) {
    for (const char* p = host; *p; p++) {
        if ((unsigned char)*p > 127) return 1;
    }
    if (strstr(host, "xn--") != NULL) return 1;
    return 0;
}

/* Normalize homoglyphs in a string */
static void normalize_homoglyphs(const char* s, char* out, int maxlen) {
    int oi = 0;
    int slen = (int)strlen(s);
    for (int i = 0; i < slen && oi < maxlen - 1; i++) {
        int found = 0;
        /* Try multi-char entries first */
        for (int j = 0; HOMOGLYPH_MAP[j].from; j++) {
            int flen = (int)strlen(HOMOGLYPH_MAP[j].from);
            if (i + flen <= slen && strncmp(s + i, HOMOGLYPH_MAP[j].from, flen) == 0) {
                int tlen = (int)strlen(HOMOGLYPH_MAP[j].to);
                if (oi + tlen < maxlen) {
                    memcpy(out + oi, HOMOGLYPH_MAP[j].to, tlen);
                    oi += tlen;
                }
                i += flen - 1;
                found = 1;
                break;
            }
        }
        if (!found) {
            out[oi++] = s[i];
        }
    }
    out[oi] = '\0';
}

/* Standard American Soundex — matches jellyfish.soundex() */
static void soundex(const char* s, char out[5]) {
    static const char table[] = {
        /*  A  B  C  D  E  F  G  H  I  J  K  L  M  N  O  P  Q  R  S  T  U  V  W  X  Y  Z */
        '0','1','2','3','0','1','2','0','0','2','2','4','5','5','0','1','2','6','2','3','0','1','0','2','0','2'
    };
    out[0] = out[1] = out[2] = out[3] = '0'; out[4] = '\0';

    /* Find first alpha char */
    int si = 0;
    while (s[si] && !isalpha((unsigned char)s[si])) si++;
    if (!s[si]) return;

    out[0] = toupper((unsigned char)s[si]);
    char last = table[toupper((unsigned char)s[si]) - 'A'];
    int oi = 1;
    si++;

    while (s[si] && oi < 4) {
        char ch = toupper((unsigned char)s[si]);
        si++;
        if (!isalpha((unsigned char)ch)) continue;
        char code = table[ch - 'A'];
        if (code != '0' && code != last) {
            out[oi++] = code;
        }
        /* Always update last (H and W don't separate identical codes in standard Soundex,
           but jellyfish does update last for non-H/W) */
        if (ch != 'H' && ch != 'W') last = code;
    }
}

/* Simple Metaphone — matches jellyfish.metaphone() for common cases.
 * This is a simplified version covering the cases that matter for URL typosquats. */
static void metaphone(const char* s, char* out, int maxlen) {
    int oi = 0;
    int slen = (int)strlen(s);
    for (int i = 0; i < slen && oi < maxlen - 1; i++) {
        char c = toupper((unsigned char)s[i]);
        if (!isalpha((unsigned char)c)) continue;
        char next = (i + 1 < slen) ? toupper((unsigned char)s[i+1]) : 0;
        char prev = (i > 0) ? toupper((unsigned char)s[i-1]) : 0;

        switch (c) {
        case 'A': case 'E': case 'I': case 'O': case 'U':
            if (i == 0) out[oi++] = c; /* vowels only at start */
            break;
        case 'B':
            if (prev != 'M') out[oi++] = 'B';
            break;
        case 'C':
            if (next == 'H') { out[oi++] = 'X'; i++; }
            else if (next == 'I' || next == 'E' || next == 'Y') out[oi++] = 'S';
            else out[oi++] = 'K';
            break;
        case 'D':
            if (next == 'G' && (i+2 < slen) &&
                (s[i+2]=='I'||s[i+2]=='i'||s[i+2]=='E'||s[i+2]=='e'||s[i+2]=='Y'||s[i+2]=='y'))
                out[oi++] = 'J';
            else out[oi++] = 'T';
            break;
        case 'F': out[oi++] = 'F'; break;
        case 'G':
            if (next == 'H' && (i+2 >= slen || !isalpha((unsigned char)s[i+2]))) { i++; break; }
            if (next == 'H') { i++; break; } /* GH silent before consonant or end */
            if (i > 0 && (next == 'N' || next == 0)) break; /* silent G */
            out[oi++] = 'K';
            break;
        case 'H':
            if ((c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U') ||
                (prev == 'A' || prev == 'E' || prev == 'I' || prev == 'O' || prev == 'U'))
                break; /* silent H after/before vowel handled by default */
            if (next && (next == 'A' || next == 'E' || next == 'I' || next == 'O' || next == 'U'))
                out[oi++] = 'H';
            break;
        case 'J': out[oi++] = 'J'; break;
        case 'K':
            if (prev != 'C') out[oi++] = 'K';
            break;
        case 'L': out[oi++] = 'L'; break;
        case 'M': out[oi++] = 'M'; break;
        case 'N': out[oi++] = 'N'; break;
        case 'P':
            if (next == 'H') { out[oi++] = 'F'; i++; }
            else out[oi++] = 'P';
            break;
        case 'Q': out[oi++] = 'K'; break;
        case 'R': out[oi++] = 'R'; break;
        case 'S':
            if (next == 'H' || (next == 'I' && (i+2<slen) && (s[i+2]=='O'||s[i+2]=='o'||s[i+2]=='A'||s[i+2]=='a')))
                { out[oi++] = 'X'; i++; }
            else if (next == 'C' && (i+2<slen) && (s[i+2]=='E'||s[i+2]=='e'||s[i+2]=='I'||s[i+2]=='i'))
                { out[oi++] = 'S'; i++; }
            else out[oi++] = 'S';
            break;
        case 'T':
            if (next == 'H') { out[oi++] = '0'; i++; }
            else if (next == 'I' && (i+2<slen) && (s[i+2]=='O'||s[i+2]=='o'||s[i+2]=='A'||s[i+2]=='a'))
                { out[oi++] = 'X'; i++; }
            else out[oi++] = 'T';
            break;
        case 'V': out[oi++] = 'F'; break;
        case 'W': case 'Y':
            if (next && (next == 'A' || next == 'E' || next == 'I' || next == 'O' || next == 'U'))
                out[oi++] = c;
            break;
        case 'X': out[oi++] = 'K'; out[oi++] = 'S'; break;
        case 'Z': out[oi++] = 'S'; break;
        default: break;
        }
    }
    out[oi] = '\0';
}

/* Phonetic match — matches Python: jellyfish.soundex(a) == jellyfish.soundex(b) or
 * jellyfish.metaphone(a) == jellyfish.metaphone(b) */
static int sounds_alike(const char* a, const char* b) {
    char sa[5], sb[5];
    soundex(a, sa);
    soundex(b, sb);
    if (strcmp(sa, sb) == 0) return 1;

    char ma[128], mb[128];
    metaphone(a, ma, sizeof(ma));
    metaphone(b, mb, sizeof(mb));
    return strcmp(ma, mb) == 0;
}

/* Typosquat analysis — returns best match info */
typedef struct {
    float sim;
    int   dist;
    int   homo_dist;
    float risk;
    int   tld_swap;
    int   sounds;
    int   found;
} TyposquatResult;

static TyposquatResult compute_typosquat(const char* input_base, const char* input_tld,
                                          const Gate1BrandList* brands) {
    TyposquatResult result = {0.0f, 0, 99, 0.0f, 0, 0, 0};
    if (!brands || brands->count == 0 || !input_base[0]) return result;

    int base_len = (int)strlen(input_base);

    /* Normalize input for homoglyph comparison */
    char input_norm[256];
    normalize_homoglyphs(input_base, input_norm, sizeof(input_norm));

    float best_risk = 0.0f;
    float best_sim = 0.0f;
    int best_dist = 99;
    int best_homo_dist = 99;
    int best_tld_swap = 0;
    int best_sounds = 0;

    for (int i = 0; i < brands->count; i++) {
        int blen = brands->entries[i].brand_len;
        if (blen < MIN_BRAND_BASE_LEN) continue;

        /* Exact match = not a typosquat, skip */
        if (strcmp(input_base, brands->entries[i].brand) == 0 &&
            strcmp(input_tld, brands->entries[i].tld) == 0)
            continue;

        /* Length pre-filter */
        if (abs(blen - base_len) > 3) continue;

        int base_dist = gate1_levenshtein(input_base, base_len,
                                          brands->entries[i].brand, blen);

        /* Normalize brand for homoglyph comparison */
        char brand_norm[256];
        normalize_homoglyphs(brands->entries[i].brand, brand_norm, sizeof(brand_norm));
        int homo_dist = gate1_levenshtein(input_norm, (int)strlen(input_norm),
                                          brand_norm, (int)strlen(brand_norm));

        int maxlen = base_len > blen ? base_len : blen;
        float sim = maxlen > 0 ? 1.0f - (float)base_dist / maxlen : 0.0f;
        float norm_sim = maxlen > 0 ? 1.0f - (float)homo_dist / maxlen : 0.0f;
        float best_s = sim > norm_sim ? sim : norm_sim;

        int is_tld_swap = (strcmp(input_base, brands->entries[i].brand) == 0 &&
                           strcmp(input_tld, brands->entries[i].tld) != 0);
        int is_embedded = (strstr(input_base, brands->entries[i].brand) != NULL &&
                           strcmp(input_base, brands->entries[i].brand) != 0);

        /* Python computes sounds_alike unconditionally (no dist guard) */
        int p_sounds = sounds_alike(input_base, brands->entries[i].brand);

        int structural = is_tld_swap || is_embedded;
        int edit_hit = (base_dist >= 1 && base_dist <= 4 &&
                        (homo_dist <= 1 || best_s >= 0.6f));
        if (!structural && !edit_hit) continue;

        /* Compute risk score matching Python */
        float risk = best_s * 0.30f;
        if (homo_dist == 0 && base_dist > 0) risk += 0.25f;
        else if (homo_dist == 1) risk += 0.10f;
        if (is_tld_swap) risk += 0.15f;
        if (is_embedded) risk += 0.10f;
        if (p_sounds) risk += 0.05f;
        /* suspicious dash */
        if (strchr(input_base, '-') != NULL && strchr(brands->entries[i].brand, '-') == NULL)
            risk += 0.05f;
        if (risk > 1.0f) risk = 1.0f;

        if (risk > best_risk) {
            best_risk = risk;
            best_sim = best_s;
            best_dist = base_dist;
            best_homo_dist = homo_dist;
            best_tld_swap = is_tld_swap;
            best_sounds = p_sounds;
            result.found = 1;
        }
    }

    result.sim = best_sim;
    result.dist = best_dist;
    result.homo_dist = best_homo_dist;
    result.risk = best_risk;
    result.tld_swap = best_tld_swap;
    result.sounds = best_sounds;
    return result;
}

/* Relative edit distance: min(edit_dist / brand_len) across brands with base >= 4 */
static float compute_relative_edit_distance(const char* input_base, const Gate1BrandList* brands) {
    if (!brands || !input_base[0]) return 1.0f;
    int base_len = (int)strlen(input_base);
    float min_rel = 1.0f;
    for (int i = 0; i < brands->count; i++) {
        int blen = brands->entries[i].brand_len;
        if (blen < MIN_BRAND_BASE_LEN) continue;
        int dist = gate1_levenshtein(input_base, base_len, brands->entries[i].brand, blen);
        float rel = (float)dist / (blen > 0 ? blen : 1);
        if (rel < min_rel) min_rel = rel;
    }
    return min_rel;
}

/* Brand in subdomain check */
static int check_brand_in_subdomain(const char* subdomain, const Gate1BrandList* brands) {
    if (!brands || !subdomain[0]) return 0;
    /* Split subdomain on dots and check each part */
    char buf[256];
    strncpy(buf, subdomain, sizeof(buf) - 1);
    buf[sizeof(buf) - 1] = '\0';

    char* token = strtok(buf, ".");
    while (token) {
        /* Lowercase token */
        for (char* p = token; *p; p++) *p = tolower((unsigned char)*p);
        int tlen = (int)strlen(token);
        for (int i = 0; i < brands->count; i++) {
            if (brands->entries[i].brand_len < MIN_BRAND_BASE_LEN) continue;
            if (brands->entries[i].brand_len == tlen &&
                strcmp(token, brands->entries[i].brand) == 0) {
                return 1;
            }
        }
        token = strtok(NULL, ".");
    }
    return 0;
}

/* ── Main Feature Extraction ──────────────────────────────────────────────── */

Gate1Features gate1_extract_features(const char* url, const Gate1BrandList* brands) {
    Gate1Features feat;
    memset(&feat, 0, sizeof(feat));

    ParsedURL pu;
    parse_url(url, &pu);

    /* Lowercase URL for keyword matching */
    char url_lower[2048];
    int url_len = (int)strlen(url);
    for (int i = 0; i < url_len && i < 2047; i++)
        url_lower[i] = tolower((unsigned char)url[i]);
    url_lower[url_len < 2047 ? url_len : 2047] = '\0';

    /* Domain analysis — special handling for IP addresses to match Python tldextract */
    int host_is_ip = is_ip_address(pu.host);
    const char* tld;
    char domain_no_tld[256];
    char subdomain[256];
    char registered[256];

    if (host_is_ip) {
        /* tldextract: domain="192.168.1.1", suffix="", subdomain="", registered_domain="" */
        /* Python uses ext.domain as the domain, registered_domain is empty */
        tld = "";
        strncpy(domain_no_tld, pu.host, sizeof(domain_no_tld) - 1);
        domain_no_tld[sizeof(domain_no_tld) - 1] = '\0';
        subdomain[0] = '\0';
        /* Python: domain = ext.registered_domain or ext.domain -> ext.domain = full IP */
        strncpy(registered, pu.host, sizeof(registered) - 1);
        registered[sizeof(registered) - 1] = '\0';
    } else {
        tld = get_tld(pu.host);
        get_domain_no_tld(pu.host, domain_no_tld, sizeof(domain_no_tld));
        get_subdomain(pu.host, subdomain, sizeof(subdomain));
        get_registered_domain(pu.host, registered, sizeof(registered));
    }

    int domain_len = (int)strlen(registered);
    int subdomain_parts = host_is_ip ? 0 : count_subdomain_parts(subdomain);

    /* Character ratios (computed on registered domain, matching Python) */
    char domain_lower[256];
    int dl = (int)strlen(registered);
    for (int i = 0; i < dl && i < 255; i++)
        domain_lower[i] = tolower((unsigned char)registered[i]);
    domain_lower[dl < 255 ? dl : 255] = '\0';

    int total_chars = dl > 0 ? dl : 1;
    int digits = 0, vowels = 0, consonants = 0;
    for (int i = 0; i < dl; i++) {
        char ch = domain_lower[i];
        if (isdigit((unsigned char)ch)) digits++;
        if (isalpha((unsigned char)ch)) {
            if (ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u')
                vowels++;
            else
                consonants++;
        }
    }

    /* Homograph features */
    float homo_score;
    int homo_conf, homo_mixed, homo_idn;
    compute_homograph(domain_no_tld, &homo_score, &homo_conf, &homo_mixed, &homo_idn);

    /* Typosquat features */
    TyposquatResult typo = compute_typosquat(domain_no_tld, tld, brands);

    /* Relative edit distance */
    float rel_edit = compute_relative_edit_distance(domain_no_tld, brands);

    /* Homoglyph edit ratio */
    float homo_ratio = 1.0f;
    if (typo.found && typo.dist > 0) {
        homo_ratio = (float)typo.homo_dist / (float)typo.dist;
    }

    /* Brand in subdomain */
    int brand_in_sub = check_brand_in_subdomain(subdomain, brands);

    /* Has dash in domain name */
    int has_dash = (strchr(domain_no_tld, '-') != NULL) ? 1 : 0;

    /* Suspicious TLD */
    int susp_tld = is_suspicious_tld(tld);

    /* Domain char entropy (domain name only, without TLD) */
    float domain_char_entropy = compute_entropy(domain_no_tld);

    /* Fill feature vector (33 features, must match FEATURE_NAMES order) */
    feat.values[0]  = (float)url_len;                                /* url_length */
    feat.values[1]  = (float)domain_len;                             /* domain_length */
    feat.values[2]  = (float)subdomain_parts;                        /* subdomain_depth */
    feat.values[3]  = domain_char_entropy;                           /* domain_char_entropy */
    feat.values[4]  = (float)subdomain_parts;                        /* subdomain_count */
    feat.values[5]  = (float)host_is_ip;                              /* has_ip_address */
    feat.values[6]  = (float)(pu.port > 0 ? 1 : 0);                 /* has_port */
    feat.values[7]  = (float)pu.is_https;                            /* is_https */
    feat.values[8]  = (float)count_special_chars(domain_lower);      /* special_char_count */
    feat.values[9]  = (float)digits / total_chars;                   /* digit_ratio */
    feat.values[10] = (float)vowels / total_chars;                   /* vowel_ratio */
    feat.values[11] = (float)consonants / total_chars;               /* consonant_ratio */
    feat.values[12] = compute_entropy(domain_lower);                 /* entropy */
    feat.values[13] = (float)susp_tld;                               /* has_suspicious_tld */
    feat.values[14] = (float)strlen(tld);                            /* tld_length */
    feat.values[15] = (float)has_dash;                               /* has_dash */
    feat.values[16] = (float)has_consecutive_repeat(domain_no_tld);  /* consecutive_char_repeat */
    feat.values[17] = (float)(susp_tld && has_dash);                 /* suspicious_tld_x_has_dash */
    feat.values[18] = (float)has_encoded_chars(url);                 /* has_encoded_chars */
    feat.values[19] = (float)count_suspicious_keywords(url_lower);   /* suspicious_keyword_count */
    feat.values[20] = homo_score;                                    /* homograph_score */
    feat.values[21] = (float)homo_conf;                              /* homograph_confusable_count */
    feat.values[22] = (float)homo_mixed;                             /* has_mixed_scripts */
    feat.values[23] = (float)is_idn(pu.host);                       /* is_idn */
    feat.values[24] = typo.found ? typo.sim : 0.0f;                 /* typosquat_similarity */
    feat.values[25] = typo.found ? (float)typo.dist : 0.0f;         /* typosquat_distance */
    feat.values[26] = typo.found ? typo.risk : 0.0f;                /* typosquat_risk_score */
    feat.values[27] = typo.found ? (float)typo.tld_swap : 0.0f;     /* typosquat_is_tld_swap */
    feat.values[28] = typo.found ? (float)typo.sounds : 0.0f;       /* typosquat_sounds_alike */
    feat.values[29] = rel_edit;                                      /* relative_edit_distance */
    feat.values[30] = homo_ratio;                                    /* homoglyph_edit_ratio */
    feat.values[31] = (float)brand_in_sub;                           /* brand_in_subdomain */
    feat.values[32] = 0.0f;                                          /* scam_db_hit (no bloom filter) */

    return feat;
}
