plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "net.qualgo.safeNest.core.signIn.api"
}

uneyPublishing {
    groupId = "net.qualgo.safeNest.core"
    artifactId = "sign-in-api"
}

dependencies {
    implementation(libs.core.utils.kotlin)
    implementation(libs.core.router)
}