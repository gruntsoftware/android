// game-contract/build.gradle.kts
plugins {
    alias(grunt.plugins.android.library)
    alias(grunt.plugins.jetbrains.kotlin.android)
    alias(grunt.plugins.jetbrains.kotlin.compose)
}

android {
    namespace = "com.brainwallet.game.contract"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
    }

    compileOptions {
        sourceCompatibility = JavaVersion.valueOf("VERSION_${grunt.versions.jvm.target.get()}")
        targetCompatibility = JavaVersion.valueOf("VERSION_${grunt.versions.jvm.target.get()}")
    }
    kotlin {
        jvmToolchain(grunt.versions.jvm.target.get().toInt())
        compilerOptions {
            jvmTarget.set(
                org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(grunt.versions.jvm.target.get())
            )
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(grunt.androidx.compose.bom))
    implementation(grunt.bundles.androidx.compose)
}
