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
    implementation(project(":modules:baseApp"))
    implementation(project(":modules:home:impl"))
    implementation(project(":modules:authChallenge:impl"))
    implementation(project(":modules:signIn:impl"))
}