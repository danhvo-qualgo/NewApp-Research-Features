plugins {
    alias(libs.plugins.uney.android.module)
    alias(libs.plugins.kotlin.android)
}

androidModule {
    compose = true
    hilt = true
}

android {
    namespace = "com.safeNest.demo.features.baseApp"
}

dependencies {
    implementation(libs.core.utils.android)
    implementation(libs.core.router)
    implementation(libs.core.logger)
    implementation(libs.core.remote.config.api)
    implementation(libs.core.remote.config.impl)
    api(libs.hilt.workmanager)
    api(libs.workmanager)
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "base-app"
}