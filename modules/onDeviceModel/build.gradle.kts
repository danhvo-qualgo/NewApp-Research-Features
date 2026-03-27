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

    ndkVersion = "27.2.12479018"

    defaultConfig {
        ndk {
            abiFilters += listOf("arm64-v8a")
        }

        externalNativeBuild {
            cmake {
                arguments += listOf(
                    "-DANDROID_STL=c++_shared",
                    "-DLLAMA_NATIVE=OFF",
                    "-DLLAMA_BUILD_TESTS=OFF",
                    "-DLLAMA_BUILD_EXAMPLES=OFF",
                    "-DLLAMA_BUILD_SERVER=OFF",
                    "-DCMAKE_BUILD_TYPE=Release",
                    "-DCMAKE_C_FLAGS_RELEASE=-O3 -DNDEBUG",
                    "-DCMAKE_CXX_FLAGS_RELEASE=-O3 -DNDEBUG"
                )
                cppFlags += listOf("-std=c++17", "-O3")
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    // ML Kit for OCR
    implementation("com.google.mlkit:text-recognition:16.0.0")
    // Vietnamese text recognition
    implementation("com.google.mlkit:text-recognition-chinese:16.0.0")

    // ONNX Runtime for Gate 1
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.17.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "gate1"
}