plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "com.safeNest.demo.features.phishingDetection.api"
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "phishingDetection-api"
}

dependencies {
    implementation(libs.core.utils.kotlin)
    implementation(libs.core.router)
}