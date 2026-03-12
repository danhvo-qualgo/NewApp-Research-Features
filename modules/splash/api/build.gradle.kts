plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "com.safeNest.demo.features.splash.api"
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "splash-api"
}

dependencies {
    implementation(libs.core.router)
}