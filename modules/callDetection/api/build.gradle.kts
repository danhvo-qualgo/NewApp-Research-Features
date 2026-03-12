plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "com.safeNest.demo.call.main.api"
}

uneyPublishing {
    groupId = "com.safeNest.demo.call"
    artifactId = "main-api"
}

dependencies {
    implementation(libs.core.router)
}