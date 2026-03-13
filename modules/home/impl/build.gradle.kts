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
    namespace = "com.safeNest.demo.features.home.impl"
}

dependencies {
    implementation(project(":modules:home:api"))
    implementation(libs.core.router)
    implementation(project(":modules:baseApp"))
    implementation(project(":modules:designSystem"))
    implementation(project(":modules:callProtection:impl"))
    implementation(project(":modules:urlGuard:api"))
    implementation(project(":modules:permissionManager1:api"))
    implementation(project(":modules:phishingDetection:api"))
    implementation(project(":modules:notificationInterceptor:api"))
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "home-impl"
}