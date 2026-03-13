plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "com.safeNest.demo.features.urlGuard1.api"
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "urlGuard1-api"
}

dependencies {
    implementation(libs.core.utils.kotlin)
    implementation(libs.core.router)
}