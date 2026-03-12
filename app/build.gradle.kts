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
    implementation(project(":modules:designSystem"))
    implementation(project(":modules:splash:impl"))
    implementation(project(":modules:home:impl"))
    implementation(project(":modules:callDetection:api"))
    implementation(project(":modules:callDetection:impl"))
    implementation(project(":modules:urlguard:impl"))
    implementation(project(":modules:permissionmanager:impl"))
    implementation(project(":modules:phishingDetection:impl"))
    implementation(project(":modules:notificationInterceptor:impl"))
    implementation(project(":modules:scamAnalyzer:impl"))
    implementation(project(":modules:safeBrowsing:impl"))
    implementation(project(":modules:commonKotlin"))
    implementation(project(":modules:commonAndroid"))
}