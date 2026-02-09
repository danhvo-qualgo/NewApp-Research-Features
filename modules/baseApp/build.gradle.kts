plugins {
    alias(libs.plugins.uney.android.module)
    alias(libs.plugins.kotlin.android)
}

androidModule {
    compose = true
    hilt = true
}

android {
    namespace = "com.uney.core.baseApp"
}

dependencies {
    implementation(libs.splashscreen)

    implementation(libs.core.utils.android)
    implementation(libs.core.resources)
    implementation(libs.core.router)
    implementation(libs.core.logger)
    implementation(libs.core.remote.config.api)
    implementation(libs.core.remote.config.impl)
    api(libs.hilt.workmanager)
    api(libs.workmanager)
}

uneyPublishing {
    groupId = "com.uney.core"
    artifactId = "base-app"
}