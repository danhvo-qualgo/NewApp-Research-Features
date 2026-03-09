plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "net.qualgo.safeNest.features.notificationInterceptor.api"
}

uneyPublishing {
    groupId = "net.qualgo.safeNest.features"
    artifactId = "notificationInterceptor-api"
}

dependencies {
    implementation(libs.core.utils.kotlin)
    implementation(libs.core.router)
}