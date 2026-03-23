plugins {
    alias(libs.plugins.uney.android.module)
    alias(libsCustom.plugins.jetbrains.kotlin.serialization)
}

androidModule {
}

android {
    namespace = "com.safeNest.demo.features.callProtection.api"
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "callProtection-api"
}

dependencies {
    implementation(libs.core.router)
}