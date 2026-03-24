plugins {
    alias(libs.plugins.uney.android.module)
    alias(libs.plugins.kotlin.android)
}

androidModule {
    hilt = true
    compose = true
    serialization = true
}

android {
    namespace = "com.safeNest.demo.features.urlGuard.impl"
}

dependencies {
    implementation(project(":modules:urlGuard:api"))
    implementation(project(":modules:notificationInterceptor:api"))
    implementation(project(":modules:onDeviceModel"))
    implementation(project(":modules:permissionManager:api"))
    implementation(project(":modules:callProtection:api"))
    //TODO: change after demo
    implementation(project(":modules:callProtection:impl"))
    implementation(project(":modules:scamAnalyzer:api"))
    implementation(project(":modules:commonAndroid"))
    implementation(project(":modules:commonKotlin"))
    implementation(libs.core.router)

    // Ktor HTTP client
    val ktorVersion = "2.3.7"
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "urlGuard-impl"
}