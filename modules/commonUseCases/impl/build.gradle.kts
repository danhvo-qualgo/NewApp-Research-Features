plugins {
    alias(libs.plugins.uney.android.module)
    alias(libs.plugins.kotlin.android)
}

androidModule {
}

android {
    namespace = "com.safeNest.demo.features.commonUseCases.impl"
}

dependencies {
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "common-usecases-api"
}