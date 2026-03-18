plugins {
    alias(libs.plugins.uney.java.module)
}

dependencies {
    implementation(libs.core.utils.kotlin)
}

uneyPublishing {
    groupId = "com.safeNest.demo.features"
    artifactId = "commonUsecases-api"
}
