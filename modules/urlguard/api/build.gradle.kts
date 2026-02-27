plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "net.qualgo.safeNest.urlguard.api"
}

uneyPublishing {
    groupId = "net.qualgo.safeNest.feature"
    artifactId = "urlguard-api"
}

dependencies {
    implementation(libs.core.utils.kotlin)
    implementation(libs.core.router)
}