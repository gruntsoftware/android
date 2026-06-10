
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(grunt.plugins.android.application) apply false
    alias(grunt.plugins.android.library) apply false
    alias(grunt.plugins.jetbrains.kotlin.android) apply false
    alias(grunt.plugins.jetbrains.kotlin.compose) apply false
    alias(grunt.plugins.jetbrains.kotlin.serialization) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(grunt.plugins.ksp) apply false
    alias(grunt.plugins.detekt) apply true
    alias(grunt.plugins.buildlogic.detekt) apply true
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
