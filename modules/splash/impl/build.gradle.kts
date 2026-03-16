plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
    hilt = true
    compose = true
    serialization = true
}

android {
    namespace = "com.safeNest.demo.features.splash.impl"
}

dependencies {
    implementation(project(":modules:splash:api"))
    implementation(project(":modules:urlGuard:api"))
    implementation(project(":modules:notificationInterceptor:api"))
    implementation(libs.core.router)
    implementation(libs.core.utils.android)
    implementation(libs.core.resources)
    implementation(libs.core.router)
    implementation(libs.core.logger)
    implementation(libs.splashscreen)
    implementation(project(":modules:baseApp"))
    implementation(project(":modules:designSystem"))
    implementation(project(":modules:commonAndroid"))
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "splash-impl"
}