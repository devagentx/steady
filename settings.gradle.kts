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
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Steady"

include(":app")
include(":core:model")
include(":core:database")
include(":core:data")
include(":core:domain")
include(":core:designsystem")
include(":core:notifications")
include(":feature:today")
include(":feature:habits")
include(":feature:insights")
include(":feature:settings")
