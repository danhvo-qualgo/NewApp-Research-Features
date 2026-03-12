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

    val ktorVersion = "2.3.7"
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")

    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.14.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.3")
}

uneyPublishing {
    groupId = "net.qualgo.safeNest.feautures"
    artifactId = "phishingDetection-impl"
}