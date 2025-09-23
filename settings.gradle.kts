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

val privateGeneralPurpDir = file("modules/private-general-purpose/content")
if (privateGeneralPurpDir.exists()) {
    include(":modules:private-general-purpose:content")
} else {
    logger.lifecycle("⚠️ Submodule ':modules:private-general-purpose:content' not included — folder not found: $privateGeneralPurpDir")
}