plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "com.safeNest.features.core.home.api"
}

uneyPublishing {
    groupId = "com.safeNest.features.core"
    artifactId = "home-api"
}

dependencies {
    implementation(libs.core.utils.kotlin)
    implementation(libs.core.router)
}