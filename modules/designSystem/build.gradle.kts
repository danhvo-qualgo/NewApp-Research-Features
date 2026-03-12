plugins {
    alias(libs.plugins.uney.android.module)
    alias(libs.plugins.kotlin.android)
}

androidModule {
    compose = true
}

android {
    namespace = "com.safeNest.demo.features.designSystem"
}

dependencies {
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "design-system"
}