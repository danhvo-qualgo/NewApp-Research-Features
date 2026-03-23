# SafeNest Gate 1 — Android Integration Guide

## Overview

Gate 1 is an on-device URL safety classifier. It runs entirely locally — no network calls needed.

**Architecture:** LateFusionURLClassifier (CNN + MLP, late fusion)

- CNN branch: character-level URL embeddings → independent logit
- MLP branch: 30 engineered features (typosquat, homograph, lexical) → independent logit
- Fusion: learnable positive scalar weights combine branch logits (prevents cross-branch suppression)
- Allowlist gate: 3,652 known-safe brand domains, O(1) hash lookup

**Performance (500-URL unseen QA test set, threshold=0.2):**

| Metric          | Value           |
| --------------- | --------------- |
| Recall          | 98.4%           |
| Precision       | 96.9%           |
| F1              | 0.976           |
| AUC-ROC         | 0.993           |
| False negatives | 4               |
| False positives | 8               |
| Inference time  | <10ms on-device |

---

## Package Contents

```
android/
├── Gate1Classifier.kt             # Android classifier wrapper
├── gate1_jni.c                    # JNI bridge for C library
├── CMakeLists.txt                 # NDK build config
├── build.gradle.kts               # Module config
├── gate1_late_fusion.onnx         # ONNX model — 1.0 MB
├── brands.bin                     # Brand allowlist — 86 KB
├── gate1_config.json              # Thresholds & config (OTA-updatable)
└── native/                        # C feature extraction library
    ├── include/gate1_features.h   # Public API
    ├── src/gate1_features.c       # Implementation (~600 lines)
    └── CMakeLists.txt             # Build config
```

---

## Integration Steps

### 1. Add the module

Copy this directory as a module in your project, then in `settings.gradle.kts`:

```kotlin
include(":gate1")
```

In your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":gate1"))
}
```

### 2. Add assets

Copy to `gate1/src/main/assets/`:

- `gate1_late_fusion.onnx`
- `brands.bin`
- `gate1_config.json`

### 3. Usage

```kotlin
// Initialize (once, at app startup)
val classifier = Gate1Classifier(context)

// Classify a URL — returns unified response (same shape as Gate 2 Backend API)
val response = classifier.classify("https://techc0mbank.com")

when (response.data.verdict) {
    "scam" -> {
        // Show verdict immediately, then call Gate 2 for keyFindings
        Log.w("Gate1", "SCAM — risk: ${"%.0f".format(response.data.riskScore * 100)}%")
        // escalateToGate2(url)  // fills in keyFindings async
    }
    "suspicious" -> {
        // Borderline — escalate to Gate 2 for a second opinion
        Log.w("Gate1", "SUSPICIOUS — escalating to Gate 2")
        // escalateToGate2(url)
    }
    else -> Log.i("Gate1", "SAFE — risk: ${"%.0f".format(response.data.riskScore * 100)}%")
}

// Batch classify
val responses = classifier.classify(listOf(
    "https://google.com",           // → safe (allowlisted, riskScore: 1.0)
    "https://g00gle.com",           // → scam (homoglyph)
    "https://evil-phishing.xyz",    // → scam
))

// Don't forget to close when done
classifier.close()
```

### 4. Build requirements

- NDK 26+ (for CMake C compilation)
- ONNX Runtime Android: already declared in `build.gradle.kts`
- Min SDK: 24 (Android 7.0)

---

## Response Format

Gate 1 returns a **unified response** matching the Gate 2 Backend API shape, so the mobile team can use one UI layer for both gates.

```json
{
  "data": {
    "riskScore": 0.8125,
    "verdict": "scam",
    "keyFindings": []
  },
  "responseTime": "4ms",
  "timestamp": 1733328600000
}
```

| Field                | Type           | Description                                                              |
| -------------------- | -------------- | ------------------------------------------------------------------------ |
| `data.riskScore`   | Float 0.0–1.0 | Confidence in the verdict (threshold-relative). Use for UI display.      |
| `data.verdict`     | String         | `"safe"`, `"suspicious"`, or `"scam"`                              |
| `data.keyFindings` | List           | Always `[]` from Gate 1. Gate 2 populates this with detailed findings. |
| `responseTime`     | String         | Inference time, e.g.`"4ms"`                                            |
| `timestamp`        | Long           | Unix epoch milliseconds                                                  |

### Verdict logic

| Condition                                 | Verdict          | riskScore |
| ----------------------------------------- | ---------------- | --------- |
| Allowlisted domain                        | `"safe"`       | 1.0       |
| Confidence <`suspicious_confidence`     | `"suspicious"` | 0.0–0.5  |
| Probability ≥ threshold, high confidence | `"scam"`       | 0.5–1.0  |
| Probability < threshold, high confidence  | `"safe"`       | 0.5–1.0  |

### Gate 2 comparison

| Field            | Gate 1                                     | Gate 2                                                 |
| ---------------- | ------------------------------------------ | ------------------------------------------------------ |
| `riskScore`    | Threshold-relative confidence              | Model confidence                                       |
| `verdict`      | `"safe"` / `"suspicious"` / `"scam"` | Same                                                   |
| `keyFindings`  | `[]` (no content analysis)               | Detailed findings with category, description, severity |
| `responseTime` | ~4ms (on-device)                           | ~2500ms (backend)                                      |

> **Backend escalation:** When `verdict` is `"suspicious"` or `"scam"`, forward the URL to the **Gate 2 Backend API**. For suspicious URLs, Gate 2 provides a second opinion. For scam URLs, Gate 2 enriches the result with `keyFindings` — detailed explanations (e.g. "typosquat of vietcombank.com.vn", "page harvests banking credentials") that give the user a reason, not just a verdict. Gate 1 responds in ~4ms; Gate 2's `keyFindings` arrive ~2.5s later to fill in the detail.

---

## How riskScore Works

`riskScore` is **not** the raw model probability. The classification threshold is 0.2 (not 0.5), so raw probability would be confusing to users. Instead, `riskScore` is rescaled relative to the decision boundary:

- **Scam side** (prob ≥ 0.2): `riskScore = (prob − 0.2) / 0.8` → 0% at boundary, 100% at prob=1.0
- **Safe side** (prob < 0.2): `riskScore = (0.2 − prob) / 0.2` → 0% at boundary, 100% at prob=0.0
- **Allowlisted**: `riskScore = 1.0`

### Suspicious zone

When `riskScore < suspicious_confidence` (default 0.5), the verdict is `"suspicious"` regardless of which side of the threshold it falls on.

```
riskScore:   0%           50%                    100%
             |─ suspicious ─|─── safe or scam ───|
                            suspicious_confidence
```

---

## Threshold Tuning

Default threshold: **0.2** (recall-biased — prioritizes catching scams; Gate 2 removes false positives)

Performance on 500-URL QA test set:

| Threshold      | Recall          | Precision       | F1              | FP          | FN          |
| -------------- | --------------- | --------------- | --------------- | ----------- | ----------- |
| 0.50           | 88.4%           | 98.0%           | 0.929           | —          | —          |
| 0.30           | 90.4%           | 94.9%           | 0.926           | —          | —          |
| **0.20** | **98.4%** | **96.9%** | **0.976** | **8** | **4** |
| 0.10           | 99.2%           | 93.3%           | 0.962           | —          | —          |
| 0.05           | 100%            | 91.9%           | 0.958           | —          | —          |

---

## Configuration & OTA Updates

All tunable parameters live in `gate1_config.json`:

```json
{
  "model": {
    "threshold": 0.2,
    "suspicious_confidence": 0.5
  }
}
```

| Parameter                 | Default | Description                                             |
| ------------------------- | ------- | ------------------------------------------------------- |
| `threshold`             | 0.2     | Classification boundary (`prob >= threshold` → scam) |
| `suspicious_confidence` | 0.5     | Below this riskScore →`"suspicious"` verdict         |

### Config-driven initialization

```kotlin
val configJson = JSONObject(context.assets.open("gate1_config.json").bufferedReader().readText())
val modelConfig = configJson.getJSONObject("model")

val classifier = Gate1Classifier(
    context,
    threshold = modelConfig.getDouble("threshold").toFloat(),
    suspiciousConfidence = modelConfig.getDouble("suspicious_confidence").toFloat()
)
```

### OTA update

Host on your CDN: `gate1_late_fusion.onnx`, `gate1_config.json`, `brands.bin`. On app launch, check for new versions, download to cache, reinitialize with fresh config values.

Tuning `threshold` or `suspicious_confidence` server-side takes effect on next app launch — no Play Store review needed.

---

## Size Impact

| Asset                | Size              |
| -------------------- | ----------------- |
| ONNX model           | 1.0 MB            |
| brands.bin           | 86 KB             |
| C library (compiled) | ~50 KB            |
| **Total**      | **~1.1 MB** |
