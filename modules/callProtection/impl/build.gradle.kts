plugins {
    alias(libs.plugins.uney.android.module)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libsCustom.plugins.jetbrains.kotlin.serialization)
}

androidModule {
    hilt = true
    compose = true
    serialization = true
}

android {
    namespace = "com.safeNest.demo.features.callProtection.impl"
}

dependencies {
    implementation(project(":modules:callProtection:api"))
    implementation(libs.core.router)
    implementation(libs.libphonenumber)
    implementation(libs.room)
    implementation(libs.room.ktx)
    implementation(libsCustom.androidx.navigation3.ui)
    implementation(libsCustom.androidx.navigation3.runtime)
    implementation(libsCustom.androidx.lifecycle.viewmodel.navigation3)
    implementation(libsCustom.androidx.material3.adaptive.navigation3)
    implementation(libsCustom.kotlinx.serialization.core)
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.core.storage.api)
    implementation(project(":modules:designSystem"))
    implementation("androidx.compose.material:material-icons-extended:1.6.3")

    ksp(libs.room.compiler)
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "callProtection-impl"
}