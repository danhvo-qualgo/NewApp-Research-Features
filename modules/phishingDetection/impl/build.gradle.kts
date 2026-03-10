plugins {
    alias(libs.plugins.uney.android.module)
    alias(libs.plugins.kotlin.android)
}

androidModule {
    hilt = true
    compose = true
    serialization = true
}

android {
    namespace = "net.qualgo.safeNest.features.phishingDetection.impl"

    ndkVersion = "27.2.12479018"

    defaultConfig {
        ndk {
            abiFilters += "arm64-v8a"
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
    implementation(project(":modules:phishingDetection:api"))
    implementation(libs.core.router)
    implementation(libs.mlkit.text.recognition)
    implementation(libs.libphonenumber)
}

uneyPublishing {
    groupId = "net.qualgo.safeNest.feautures"
    artifactId = "phishingDetection-impl"
}