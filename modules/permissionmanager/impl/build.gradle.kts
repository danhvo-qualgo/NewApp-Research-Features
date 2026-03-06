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
    namespace = "net.qualgo.safeNest.permissionmanager.impl"
}

dependencies {
    implementation(project(":modules:permissionmanager:api"))
    implementation(libs.core.router)
    implementation(libs.hilt.navigation)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)
}

uneyPublishing {
    groupId = "net.qualgo.safeNest.feauture"
    artifactId = "permissionmanager-impl"
}
