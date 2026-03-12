plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "com.safeNest.demo.features.call.api"
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "call-api"
}

dependencies {
    implementation(libs.core.router)
}