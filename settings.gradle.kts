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

    versionCatalogs {
        create("libsCustom") {
            from(files("./gradle/libsCustom.versions.toml"))
        }
    }
}



rootProject.name = "SafeNest-Demo"

include(":app")
include(":modules:baseApp")
include(":modules:home:api")
include(":modules:home:impl")
include(":modules:callProtection:api")
include(":modules:callProtection:impl")
include(":modules:urlGuard:api")
include(":modules:urlGuard:impl")
include(":modules:permissionManager:api")
include(":modules:permissionManager:impl")
//include(":modules:phishingDetection:impl")
//include(":modules:phishingDetection:api")
include(":modules:notificationInterceptor:impl")
include(":modules:notificationInterceptor:api")
include(":modules:designSystem")
include(":modules:splash:api")
include(":modules:splash:impl")
include(":modules:scamAnalyzer:api")
include(":modules:scamAnalyzer:impl")
include(":modules:safeBrowsing:api")
include(":modules:safeBrowsing:impl")
include(":modules:commonKotlin")
include(":modules:commonAndroid")
include(":modules:commonUseCases:api")
include(":modules:commonUseCases:impl")
include(":modules:gate1")
