plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "com.safeNest.demo.urlguard.api"
}

uneyPublishing {
    groupId = "com.safeNest.demo"
    artifactId = "urlguard-api"
}

dependencies {
    implementation(libs.core.utils.kotlin)
    implementation(libs.core.router)
}