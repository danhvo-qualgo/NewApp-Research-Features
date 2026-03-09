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
    namespace = "net.qualgo.safeNest.features.notificationInterceptor.impl"
}

dependencies {
    implementation(project(":modules:notificationInterceptor:api"))
    implementation(libs.core.router)
}

uneyPublishing {
    groupId = "net.qualgo.safeNest.features"
    artifactId = "notificationInterceptor-impl"
}