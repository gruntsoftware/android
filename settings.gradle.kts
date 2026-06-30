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
        mavenLocal()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://central.sonatype.com/repository/maven-snapshots/") }
    }
}
includeBuild("bw-gdlib") {
    dependencySubstitution {
        substitute(module("ltd.grunt.brainwallet.gdx:core")).using(project(":android"))
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://central.sonatype.com/repository/maven-snapshots/") }
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
include(":core",":iap",":general-purpose-app")
project(":general-purpose-app").projectDir = file("modules/private-general-purpose/app")
project(":core").projectDir = file("modules/private-general-purpose/core")
project(":iap").projectDir = file("modules/private-general-purpose/iap")




