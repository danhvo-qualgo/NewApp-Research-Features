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
}

uneyPublishing {
    groupId = "net.qualgo.safeNest.feauture"
    artifactId = "permissionmanager-impl"
}
