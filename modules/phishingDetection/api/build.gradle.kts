plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "net.qualgo.safeNest.features.phishingDetection.api"
}

uneyPublishing {
    groupId = "net.qualgo.safeNest.features"
    artifactId = "phishingDetection-api"
}

dependencies {
    implementation(libs.core.utils.kotlin)
    implementation(libs.core.router)
}