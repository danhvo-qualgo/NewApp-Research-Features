plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "net.qualgo.safeNest.core.home.api"
}

uneyPublishing {
    groupId = "net.qualgo.safeNest.core"
    artifactId = "home-api"
}

dependencies {
    implementation(libs.core.utils.kotlin)
    implementation(libs.core.router)
}