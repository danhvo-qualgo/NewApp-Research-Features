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

        fun loadProps(): java.util.Properties {
            return java.util.Properties().apply {
                val file = File("local.properties")
                if (file.exists()) {
                    file.inputStream().use { load(it) }
                }
            }
        }

        maven(settings.extra["JFROG_URL"].toString()) {
            credentials {
                val props by lazy { loadProps() }
                username = System.getenv("JFROG_USERNAME") ?: props.getProperty("jfrog.username")
                password = System.getenv("JFROG_PASSWORD") ?: props.getProperty("jfrog.password")
            }
        }

        mavenLocal()
    }
}

buildscript {
    repositories {
        mavenCentral()
        google()

        fun loadProps(): java.util.Properties {
            return java.util.Properties().apply {
                val file = File("local.properties")
                if (file.exists()) {
                    file.inputStream().use { load(it) }
                }
            }
        }

        maven(settings.extra["JFROG_URL"].toString()) {
            credentials {
                val props by lazy { loadProps() }
                username = System.getenv("JFROG_USERNAME") ?: props.getProperty("jfrog.username")
                password = System.getenv("JFROG_PASSWORD") ?: props.getProperty("jfrog.password")
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

        fun loadProps(): java.util.Properties {
            return java.util.Properties().apply {
                val file = File("local.properties")
                if (file.exists()) {
                    file.inputStream().use { load(it) }
                }
            }
        }

        maven(settings.extra["JFROG_URL"].toString()) {
            credentials {
                val props by lazy { loadProps() }
                username = System.getenv("JFROG_USERNAME") ?: props.getProperty("jfrog.username")
                password = System.getenv("JFROG_PASSWORD") ?: props.getProperty("jfrog.password")
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
