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
        val gamesToml = file("modules/games/gradle/libs.versions.toml")
        if (gamesToml.exists()) {
            create("games") {
                from(files(gamesToml))
            }
        } else {
            logger.lifecycle("⚠️ Submodule catalog 'games' not loaded — file not found: $gamesToml")
        }
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

val gamesDir = file("modules/games/content")
if (gamesDir.exists()) {
    include(":modules:games:content")
} else {
    logger.lifecycle("⚠️ Submodule ':modules:games:content' not included — folder not found: $gamesDir")
}