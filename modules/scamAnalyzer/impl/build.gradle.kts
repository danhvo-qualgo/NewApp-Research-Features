plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
    hilt = true
    compose = true
    serialization = true
}

android {
    namespace = "com.safeNest.demo.features.scamAnalyzer.impl"
}

dependencies {
    implementation(project(":modules:scamAnalyzer:api"))
    implementation(libs.core.router)
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "scam-analyzer-impl"
}