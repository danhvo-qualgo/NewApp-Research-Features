plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "net.qualgo.safeNest.core.authChallenge.api"
}

uneyPublishing {
    groupId = "net.qualgo.safeNest.core"
    artifactId = "auth-challenge-api"
}

dependencies {
    implementation(libs.core.utils.kotlin)
    implementation(libs.core.router)
}