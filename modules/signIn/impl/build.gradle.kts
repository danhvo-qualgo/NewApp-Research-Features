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
    namespace = "com.safeNest.features.core.signIn.impl"
}

dependencies {
    implementation(project(":modules:signIn:api"))
    implementation(libs.core.router)
    implementation(libs.core.utils.kotlin)
    implementation(project(":modules:authChallenge:api"))
}

uneyPublishing {
    groupId = "com.safeNest.features.core.signIn"
    artifactId = "impl"
}