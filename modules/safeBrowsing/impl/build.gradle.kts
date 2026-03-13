plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
    hilt = true
    compose = true
    serialization = true
}

android {
    namespace = "com.safeNest.demo.features.safeBrowsing.impl"
}

dependencies {
    implementation(project(":modules:safeBrowsing:api"))
    implementation(libs.core.router)
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "safe-browsing-impl"
}