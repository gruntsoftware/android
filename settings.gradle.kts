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
include(":core", ":games", ":iap", ":general-purpose-app")
project(":general-purpose-app").projectDir = file("modules/private-general-purpose/app")
project(":core").projectDir = file("modules/private-general-purpose/core")
project(":games").projectDir = file("modules/private-general-purpose/games")
project(":iap").projectDir = file("modules/private-general-purpose/iap")
include(":modules:design-system")
include(":modules:ltc")
