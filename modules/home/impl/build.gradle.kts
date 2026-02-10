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
    namespace = "com.safeNest.features.core.home.impl"
}

dependencies {
    implementation(project(":modules:home:api"))
    implementation(libs.core.router)
    implementation(project(":modules:signIn:api"))
}

uneyPublishing {
    groupId = "com.safeNest.features.core"
    artifactId = "home-impl"
}