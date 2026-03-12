plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "com.safeNest.demo.features.home.api"
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "home-api"
}

dependencies {
    implementation(libs.core.utils.kotlin)
    implementation(libs.core.router)
}