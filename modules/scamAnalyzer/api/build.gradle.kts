plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
    serialization = true
}

android {
    namespace = "com.safeNest.demo.features.scamAnalyzer.api"
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "scam-analyzer-api"
}

dependencies {
    implementation(libs.core.router)
}