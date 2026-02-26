pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()

        maven(System.getenv("CORE_REPOSITORY_URL")) {
            credentials {
                username = System.getenv("CORE_REPOSITORY_USERNAME")
                password = System.getenv("CORE_REPOSITORY_PASSWORD")
            }
        }

        maven(System.getenv("PUBLISH_REPOSITORY_URL")) {
            credentials {
                username = System.getenv("PUBLISH_REPOSITORY_USERNAME")
                password = System.getenv("PUBLISH_REPOSITORY_PASSWORD")
            }
        }

        mavenLocal()
    }
}

buildscript {
    repositories {
        mavenCentral()
        google()

        maven(System.getenv("CORE_REPOSITORY_URL")) {
            credentials {
                username = System.getenv("CORE_REPOSITORY_USERNAME")
                password = System.getenv("CORE_REPOSITORY_PASSWORD")
            }
        }

        maven(System.getenv("PUBLISH_REPOSITORY_URL")) {
            credentials {
                username = System.getenv("PUBLISH_REPOSITORY_USERNAME")
                password = System.getenv("PUBLISH_REPOSITORY_PASSWORD")
            }
        }

        mavenLocal()
    }
    dependencies {
        classpath("com.squareup:javapoet:1.13.0") // for hilt plugin
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")

        maven(System.getenv("CORE_REPOSITORY_URL")) {
            credentials {
                username = System.getenv("CORE_USERNAME")
                password = System.getenv("CORE_PASSWORD")
            }
        }

        maven(System.getenv("PUBLISH_REPOSITORY_URL")) {
            credentials {
                username = System.getenv("PUBLISH_REPOSITORY_USERNAME")
                password = System.getenv("PUBLISH_REPOSITORY_PASSWORD")
            }
        }

        mavenLocal()
    }

    versionCatalogs {
        create("libsCustom") {
            from(files("./gradle/libsCustom.versions.toml"))
        }
    }
}



rootProject.name = "SafeNest-Core-Features"

include(":app")
include(":modules:baseApp")
include(":modules:home:api")
include(":modules:home:impl")
include(":modules:authChallenge:api")
include(":modules:authChallenge:impl")
include(":modules:signIn:api")
include(":modules:signIn:impl")
include(":modules:callDetection:api")
include(":modules:callDetection:impl")
include(":modules:call_detection:api")
include(":modules:call_detection:impl")