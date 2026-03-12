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
    implementation(project(":modules:callDetection:impl"))
    implementation(project(":modules:urlguard:api"))
    implementation(project(":modules:permissionmanager:api"))
    implementation(project(":modules:phishingDetection:api"))
    implementation(project(":modules:notificationInterceptor:api"))
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "home-impl"
}