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
    implementation(libs.coil.compose)
    implementation(project(":modules:scamAnalyzer:api"))
    implementation(libs.core.router)
    implementation(project(":modules:designSystem"))
    implementation("com.googlecode.libphonenumber:libphonenumber:8.13.40")
    implementation("com.google.mlkit:text-recognition:16.0.1")
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "scam-analyzer-impl"
}