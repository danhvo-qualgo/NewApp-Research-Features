plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "com.safeNest.features.core.authChallenge.api"
}

uneyPublishing {
    groupId = "com.safeNest.features.core"
    artifactId = "authChallenge-api"
}

dependencies {
    implementation(libs.core.utils.kotlin)
    implementation(libs.core.router)
}