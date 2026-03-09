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
    namespace = "net.qualgo.safeNest.urlguard.impl"
}

dependencies {
    implementation(project(":modules:urlguard:api"))
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
    groupId = "net.qualgo.safeNest.feauture"
    artifactId = "urlguard-impl"
}