// build.gradle.kts — Android library module for Gate 1 classifier
// Add this module to your app's settings.gradle.kts: include(":gate1")

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
                cppFlags("")
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
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.22.0")
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "gate1"
}