plugins {
    alias(libs.plugins.uney.application.module)
    alias(libs.plugins.google.services)
}

applicationModule {
    buildName = "SafeNest-Core-Features"
}

android {
    namespace = "com.safeNest.features.core.app"

    defaultConfig {
        applicationId = "com.safeNest.features.core.app"
    }
}


dependencies {
    implementation(project(":modules:baseApp"))
    implementation(project(":modules:home:impl"))
    implementation(project(":modules:authChallenge:impl"))
    implementation(project(":modules:signIn:impl"))
    implementation(project(":modules:urlguard:impl"))
    implementation(project(":modules:phishingDetection:impl"))
    implementation(project(":modules:notificationInterceptor:impl"))
}