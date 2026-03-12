plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "com.safeNest.demo.phishingDetection.api"
}

uneyPublishing {
    groupId = "com.safeNest.demo"
    artifactId = "phishingDetection-api"
}

dependencies {
    implementation(libs.core.utils.kotlin)
    implementation(libs.core.router)
}