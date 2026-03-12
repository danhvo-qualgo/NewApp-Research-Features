plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "com.safeNest.demo.features.permissionmanager.api"
}

uneyPublishing {
    groupId = "com.safeNest.demo"
    artifactId = "permissionmanager-api"
}

dependencies {
    implementation(libs.core.utils.kotlin)
    implementation(libs.core.router)
}
