plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "com.safeNest.demo.features.safeBrowsing.api"
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "safe-browsing-api"
}

dependencies {
    implementation(libs.core.router)
}