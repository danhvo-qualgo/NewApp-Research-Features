plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "com.safeNest.demo.features.urlGuard.api"
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "urlGuard-api"
}

dependencies {
    implementation(libs.core.utils.kotlin)
    implementation(libs.core.router)
    implementation(libs.kotlinx.coroutines.core)
}