plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "com.safeNest.demo.features.notificationInterceptor.api"
}

uneyPublishing {
    groupId = "com.safeNest.demo"
    artifactId = "notificationInterceptor-api"
}

dependencies {
    implementation(libs.core.utils.kotlin)
    implementation(libs.core.router)
}