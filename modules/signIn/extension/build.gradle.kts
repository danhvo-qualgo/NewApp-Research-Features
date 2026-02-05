plugins {
    alias(libs.plugins.uney.android.module)
}

androidModule {
}

android {
    namespace = "com.safeNest.features.core.signIn.extension"
}

uneyPublishing {
    groupId = "com.safeNest.features.core.signIn"
    artifactId = "extension"
}