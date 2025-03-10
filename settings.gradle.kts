import java.net.URI

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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = URI("https://jitpack.io") }
        google()
        mavenCentral()
    }
}

rootProject.name = "AScore2"
include(":kscore")
include(":app")
include(":kscoreandroid")
include(":mp3converter")
include(":ui")
