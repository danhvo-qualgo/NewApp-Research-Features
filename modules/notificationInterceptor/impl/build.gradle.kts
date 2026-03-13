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
    namespace = "com.safeNest.demo.features.notificationInterceptor.impl"
}

dependencies {
    implementation(project(":modules:notificationInterceptor:api"))
    implementation(libs.core.router)
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "notificationInterceptor-impl"
}