plugins {
    alias(libs.plugins.uney.android.module)
    alias(libs.plugins.kotlin.android)
}

androidModule {
}

android {
    namespace = "com.safeNest.demo.features.commonAndroid"
}

dependencies {
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "common-android"
}