plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "com.safeNest.features.core.authChallenge.extension"
}

uneyPublishing {
    groupId = "com.safeNest.features.core.authChallenge"
    artifactId = "extension"
}