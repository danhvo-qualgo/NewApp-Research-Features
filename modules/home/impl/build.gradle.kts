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
    namespace = "net.qualgo.safeNest.core.home.impl"
}

dependencies {
    implementation(project(":modules:home:api"))
    implementation(project(":modules:callDetection:impl"))
    implementation(libs.core.router)
    implementation(project(":modules:signIn:api"))
    implementation(project(":modules:urlguard:api"))
    implementation(project(":modules:permissionmanager:api"))
    implementation(project(":modules:phishingDetection:api"))
    implementation(project(":modules:notificationInterceptor:api"))
}

uneyPublishing {
    groupId = "net.qualgo.safeNest.core"
    artifactId = "home-impl"
}