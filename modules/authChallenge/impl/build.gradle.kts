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
    namespace = "com.safeNest.features.core.authChallenge.impl"
}

dependencies {
    implementation(project(":modules:authChallenge:api"))
    implementation(libs.core.utils.kotlin)
    implementation(libs.core.router)

    // custom
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.gms.play.services.auth)
}

uneyPublishing {
    groupId = "com.safeNest.features.core.authChallenge"
    artifactId = "impl"
}