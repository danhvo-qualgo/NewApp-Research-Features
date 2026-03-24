// build.gradle.kts — Android library module for URL Analyzer (Gate 1 + Gate 2)
//
// Gate 1: ONNX Runtime (LightGBM) + C feature extraction via NDK
// Gate 2: MNN LLM engine (Qwen3 0.6B) + local URL analyzers

plugins {
    alias(libs.plugins.uney.android.module)
    alias(libs.plugins.kotlin.android)
}

androidModule {
    hilt = true
    serialization = true
}

android {
    namespace = "com.safenest.gate1"
    ndkVersion = "26.1.10909125"

    defaultConfig {

        externalNativeBuild {
            cmake {
                cppFlags("-std=c++17")
                arguments("-DANDROID_STL=c++_shared")
            }
        }

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
        }
    }

    externalNativeBuild {
        cmake {
            path = file("CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    // Gate 1: ONNX Runtime for LightGBM inference
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.22.0")
    
    // Coroutines for LocalURLAnalyzer (parallel sub-analyses)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "gate1"
}