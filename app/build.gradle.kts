plugins {
    alias(libs.plugins.uney.application.module)
    alias(libs.plugins.google.services)
}

applicationModule {
    buildName = "SafeNest-Demo"
}

android {
    namespace = "com.safeNest.demo.app"

    defaultConfig {
        applicationId = "com.safeNest.demo.app"
    }
}


dependencies {
    implementation(project(":modules:baseApp"))
    implementation(project(":modules:home:impl"))
    implementation(project(":modules:callDetection:api"))
    implementation(project(":modules:callDetection:impl"))
    implementation(project(":modules:urlguard:impl"))
    implementation(project(":modules:permissionmanager:impl"))
    implementation(project(":modules:phishingDetection:impl"))
    implementation(project(":modules:notificationInterceptor:impl"))
}