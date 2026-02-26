plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "com.safeNest.features.call.callDetection.api"
}

uneyPublishing {
    groupId = "com.safeNest.features.call"
    artifactId = "call-detection-api"
}

dependencies {
    implementation(libs.core.router)
}