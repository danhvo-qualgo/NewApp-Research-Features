# URL Analyzer Android — Integration Guide (2026-03-23)

## What Changed

**Gate 1 is a new model.** Everything else (Gate 2, local analyzers, orchestrator) is the
same pipeline from `url_analyzer_android`, just reorganized to sit alongside the new Gate 1.

| Component | Before | Now |
|---|---|---|
| Gate 1 architecture | Late-fusion hybrid (CNN + features) | **LightGBM** (features only) |
| Gate 1 features | 30 | **33** |
| Gate 1 inference | Placeholder (not implemented) | **Working** (ONNX Runtime) |
| Gate 1 model file | `gate1_hybrid.onnx` | **`gate1_lightgbm.onnx`** (3.3 MB) |
| Gate 1 URL encoding | `nativeEncodeUrl()` needed | **Removed** (no CNN branch) |
| Gate 1 threshold | 0.45 / 0.55 | **0.2** (tuned for high recall) |
| Gate 2 SLM runtime | llama.cpp (placeholder JNI) | **MNN** (reuses your existing `PhishingLlmAnalyzer`) |
| Gate 2 JNI | Not wired | **Wired** via `PhishingLlmAnalyzer` bridge |

### Gate 1 Performance

| Metric | Value |
|---|---|
| Recall (scam) | 91.2% |
| Precision (scam) | 95.6% |
| AUC-ROC | 0.980 |
| Inference | <1ms |
| 490/490 test URLs | 100% match vs Python training pipeline |

---

## Important: MNN / PhishingLlmAnalyzer Bridge

Gate 2's `LMClient.kt` needs to call MNN for LLM inference. Rather than duplicating
the JNI bindings, it **delegates to your existing `PhishingLlmAnalyzer`** class:

```
LMClient.kt  →  PhishingLlmAnalyzer.kt  →  llm_jni.cpp (unchanged)
                 (bridge class)               (your existing MNN native code)
```

`PhishingLlmAnalyzer.kt` lives in `gate2/` but declares:

```kotlin
package com.safeNest.demo.features.scamAnalyzer.impl.utils
```

This matches the JNI symbols in your existing `libmnnllmphishing.so`. **Do not rename it.**

The `cpp/` folder in this package is a **reference copy** of your MNN native code —
use your existing build, not this copy.

---

## Package Layout

```
android/
├── gate1/                          # NEW — Gate 1 LightGBM
│   ├── Gate1Classifier.kt         #   ONNX inference + keyFinding mapping
│   └── native/                    #   C feature extraction (33 features)
│       ├── include/gate1_features.h
│       └── src/
│           ├── gate1_features.c   #   Byte-for-byte identical to iOS
│           └── gate1_jni.c        #   4 JNI methods
│
├── gate2/                          # Gate 2 SLM (from url_analyzer_android)
│   ├── Gate2Classifier.kt         #   Signals → prompt → SLM → parse JSON
│   ├── SignalExtractor.kt         #   Config-driven signal extraction
│   ├── PromptBuilder.kt           #   Prompt assembly from templates
│   ├── LMClient.kt                #   MNN wrapper with streaming + early stop
│   └── PhishingLlmAnalyzer.kt     #   JNI bridge (matches your existing native)
│
├── local_analyzer/                 # URL sub-analyzers (from url_analyzer_android)
│   ├── LocalURLAnalyzer.kt        #   Orchestrates 4 parallel sub-analyses
│   ├── HomographAnalyzer.kt       #   Unicode confusable detection
│   ├── TyposquatAnalyzer.kt       #   Levenshtein brand matching
│   ├── SSLChecker.kt              #   TLS cert extraction
│   └── PageFetcher.kt             #   HTTP GET + HTML parsing
│
├── Models.kt                       # Shared types (Gate1Result, Gate2Result, etc.)
├── URLAnalyzerOrchestrator.kt      # Decision router: Gate 1 → maybe Gate 2
├── ResultCombiner.kt               # Verdict matrix + weighted risk score
├── URLAnalyzerError.kt             # Error types
│
├── config/                          # JSON configs → copy to assets/
│   ├── gate1_config.json           #   Model metadata, thresholds, feature names
│   ├── gate1_keyfinding_mappings.json  # Feature index → human-readable finding
│   ├── gate2_signal_mappings.json  #   Signal extraction rules
│   └── gate2_prompts.json          #   System + user prompt templates
│
├── assets/                          # Binary assets → copy to assets/
│   ├── gate1_lightgbm.onnx        #   LightGBM ONNX model (3.3 MB)
│   └── brands.bin                  #   Binary brand allowlist (86 KB)
│
├── cpp/                             # REFERENCE COPY of your MNN native code
│   ├── llm_jni.cpp                 #   (use your existing build, not this copy)
│   ├── include/ llm_include/ jniLibs/
│   └── SETUP.md
│
├── CMakeLists.txt                   # Unified native build config
└── build.gradle.kts                 # Dependencies
```

---

## Step-by-Step Integration

### 1. Kotlin Source

Copy into your module's source tree, preserving package structure:

| Source | Target package |
|---|---|
| `Models.kt`, `URLAnalyzerOrchestrator.kt`, `ResultCombiner.kt`, `URLAnalyzerError.kt` | `com.safenest.urlanalyzer` |
| `gate1/Gate1Classifier.kt` | `com.safenest.urlanalyzer.gate1` |
| `gate2/*.kt` | `com.safenest.urlanalyzer.gate2` |
| `gate2/PhishingLlmAnalyzer.kt` | `com.safeNest.demo.features.scamAnalyzer.impl.utils` (must match JNI) |
| `local_analyzer/*.kt` | `com.safenest.urlanalyzer.local_analyzer` |

### 2. Native Code (Gate 1 only)

Copy into your NDK build:

```
app/src/main/cpp/
├── gate1_jni.c                  # from gate1/native/src/
├── gate1_features.c             # from gate1/native/src/
└── include/gate1_features.h     # from gate1/native/include/
```

Add to your `CMakeLists.txt`:

```cmake
add_library(gate1_features SHARED gate1_jni.c gate1_features.c)
target_include_directories(gate1_features PRIVATE include)
target_link_libraries(gate1_features log m)
target_link_options(gate1_features PRIVATE "-Wl,-z,max-page-size=16384")
```

Gate 2 uses your **existing** `libmnnllmphishing.so` — no new native build needed.

### 3. Assets

Copy to `app/src/main/assets/`:

```
gate1_lightgbm.onnx              # from assets/
brands.bin                        # from assets/
brands.csv                        # your existing brand CSV
gate1_keyfinding_mappings.json    # from config/
gate1_config.json                 # from config/
gate2_signal_mappings.json        # from config/
gate2_prompts.json                # from config/
```

### 4. Dependencies

Add to `build.gradle.kts`:

```kotlin
dependencies {
    // Gate 1: ONNX Runtime
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.17.0")

    // Local analyzer: coroutines for parallel sub-analyses
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Gate 2: MNN — your existing dependency, no change needed
}
```

### 5. Manifest

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

Only needed for Gate 2's local analyzers (SSL check, page fetch). Gate 1 is fully offline.

---

## Usage

### Full Pipeline (Gate 1 + Gate 2)

```kotlin
val gate1 = Gate1Classifier(context)

val signalExtractor = SignalExtractor(context)
val promptBuilder = PromptBuilder(context)
val lmClient = LMClient(mnnModelConfigPath)
lmClient.load(promptBuilder.buildSystemPrompt())

val gate2 = Gate2Classifier(signalExtractor, promptBuilder, lmClient)
val localAnalyzer = LocalURLAnalyzer(gate1.brandNames, gate1.brandDomains)

val orchestrator = URLAnalyzerOrchestrator(gate1, gate2, lmClient, localAnalyzer)

// In a coroutine:
val result = orchestrator.analyze("https://vietcombank-verify.xyz/login")
// result.verdict       → "scam"
// result.keyFindings   → [KeyFinding(category="Impersonation", ...)]
// result.source        → "gate1_and_gate2"
```

### Gate 1 Only (Fast Mode)

```kotlin
val gate1 = Gate1Classifier(context)
val result = gate1.classify("https://example.com")
// result.verdict, result.riskScore, result.keyFindings
gate1.close()
```

---

## Decision Router Logic

The orchestrator only escalates to Gate 2 when needed:

| Gate 1 says | Action |
|---|---|
| **safe** | Return immediately (~1ms) |
| **suspicious** + has keyFindings | Return Gate 1 result (fast) |
| **suspicious** + no keyFindings | Escalate to Gate 2 |
| **scam** + low confidence (<0.2) | Return Gate 1 result |
| **scam** + high confidence (≥0.2) | Escalate to Gate 2 |

Gate 2 costs ~3-15s (MNN inference + network I/O for SSL/page checks).

---

## Size Impact

| Component | Size |
|---|---|
| `gate1_lightgbm.onnx` | 3.3 MB |
| `brands.bin` | 86 KB |
| `libgate1_features.so` | ~50 KB |
| JSON configs (4 files) | ~5 KB |
| **Gate 1 total** | **~3.4 MB** |
| Qwen3 MNN model (Gate 2) | ~378 MB (already in your app) |
