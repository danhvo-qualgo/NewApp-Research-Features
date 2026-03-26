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
    implementation(project(":modules:onDeviceModel"))
    implementation(libsCustom.googlecode.libphonenumber)
    implementation(libsCustom.google.mlkit.textRecognition)
    implementation(libs.mlkit.text.recognition)
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.14.0")
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.core.network.api)
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "scam-analyzer-impl"
}