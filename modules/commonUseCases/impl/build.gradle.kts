plugins {
    alias(libs.plugins.uney.android.module)
    alias(libs.plugins.kotlin.android)
}

androidModule {
    hilt = true
    serialization = true
}

android {
    namespace = "com.safeNest.demo.features.commonUseCases.impl"
}

dependencies {
    implementation(libs.core.utils.kotlin)
    implementation(libs.core.utils.android)
    implementation(libs.core.network.api)
    implementation(project(":modules:commonUseCases:api"))
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "commonUsecases-api"
}