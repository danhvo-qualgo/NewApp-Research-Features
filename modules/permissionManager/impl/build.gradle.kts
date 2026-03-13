plugins {
    alias(libs.plugins.uney.android.module)
    alias(libs.plugins.kotlin.android)
}

androidModule {
    hilt = true
    compose = true
    serialization = true
}

android {
    namespace = "com.safeNest.demo.features.permissionManager.impl"
}

dependencies {
    implementation(project(":modules:permissionManager:api"))
    implementation(libs.core.router)
    implementation(libs.hilt.navigation)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "permissionManager-impl"
}
