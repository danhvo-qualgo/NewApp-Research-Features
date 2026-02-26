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
    namespace = "com.safeNest.features.call.callDetection.impl"
}

dependencies {
    implementation(project(":modules:callDetection:api"))
    implementation(libs.core.router)
}

uneyPublishing {
    groupId = "com.safeNest.features.call"
    artifactId = "call-detection-impl"
}