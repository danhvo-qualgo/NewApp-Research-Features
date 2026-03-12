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
    namespace = "com.safeNest.demo.features.permissionmanager.impl"
}

dependencies {
    implementation(project(":modules:permissionmanager:api"))
    implementation(libs.core.router)
    implementation(libs.hilt.navigation)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)
}

uneyPublishing {
    groupId = "com.safeNest.demo"
    artifactId = "permissionmanager-impl"
}
