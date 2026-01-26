plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "com.safeNest.features.core.home.extension"
}

uneyPublishing {
    groupId = "com.safeNest.features.core.home"
    artifactId = "extension"
}