plugins {
    alias(libs.plugins.uney.application.module)
}

applicationModule {
    buildName = "CoreFeatureApp"
}

android {
    namespace = "com.safeNest.features.core.app"

    defaultConfig {
        applicationId = "com.safeNest.features.core.app"
    }
}


dependencies {
}