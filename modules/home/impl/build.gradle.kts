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
    namespace = "com.safeNest.demo.features.home.impl"
}

dependencies {
    implementation(project(":modules:designSystem"))
    implementation(project(":modules:callProtection:impl"))
    implementation(project(":modules:home:api"))
    implementation(project(":modules:urlGuard:api"))
    implementation(libs.core.router)
    implementation(project(":modules:urlGuard:api"))
    implementation(project(":modules:scamAnalyzer:api"))
    implementation(project(":modules:onDeviceModel"))
    implementation(project(":modules:notificationInterceptor:api"))
    implementation(project(":modules:commonUseCases:impl"))
    implementation(libs.core.network.api)
    implementation(libs.androidx.datastore.preferences)
    implementation("androidx.compose.material:material-icons-extended:1.6.3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation(libs.coil.compose)
}

uneyPublishing {
    groupId = "net.qualgo.safeNest.core"
    artifactId = "home-impl"
}