plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "com.safeNest.demo.features.permissionManager1.api"
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "permissionManager1-api"
}

dependencies {
    implementation(libs.core.utils.kotlin)
    implementation(libs.core.router)
}
