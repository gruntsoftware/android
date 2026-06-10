import org.gradle.kotlin.dsl.project

pluginManagement {
    includeBuild("android-build-logic")
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
            from(files("android-build-logic/gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "Brainwallet Android"
include(":app")
include(":install_time_asset_pack")
include(":core",":iap",":bw-gdlib:core",":general-purpose-app")
project(":general-purpose-app").projectDir = file("modules/private-general-purpose/app")
project(":core").projectDir = file("modules/private-general-purpose/core")
project(":iap").projectDir = file("modules/private-general-purpose/iap")
project(":bw-gdlib:core").projectDir = file("modules/private-general-purpose/bw-gdlib/core")
