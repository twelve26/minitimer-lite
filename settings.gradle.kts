pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com[.]android.*")
                includeGroupByRegex("androidx[.].*")
                includeGroupByRegex("com[.]google[.]android.*")
                includeGroupByRegex("com[.]google[.]testing.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com[.]android.*")
                includeGroupByRegex("androidx[.].*")
                includeGroupByRegex("com[.]google[.]android.*")
                includeGroupByRegex("com[.]google[.]testing.*")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "MiniTimer lite"
include(":composeApp")
