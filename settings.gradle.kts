pluginManagement {
    includeBuild("gruntsoftware-build-logic")
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
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("grunt") {
            from(files("gruntsoftware-build-logic/gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "Brainwallet Android"
include(":app")
include(":install_time_asset_pack")
include(":modules:private-general-purpose:games")
