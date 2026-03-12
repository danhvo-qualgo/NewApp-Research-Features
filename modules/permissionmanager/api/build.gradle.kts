plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "net.qualgo.safeNest.permissionmanager.api"
}

uneyPublishing {
    groupId = "net.qualgo.safeNest.feauture"
    artifactId = "permissionmanager-api"
}

dependencies {
    implementation(libs.core.utils.kotlin)
    implementation(libs.core.router)
}
