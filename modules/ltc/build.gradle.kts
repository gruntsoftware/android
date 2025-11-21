plugins {
    alias(grunt.plugins.android.library)
    alias(grunt.plugins.jetbrains.kotlin.android)
    alias(grunt.plugins.jetbrains.kotlin.compose)
    alias(grunt.plugins.buildlogic.test)
    alias(grunt.plugins.buildlogic.detekt)
    alias(grunt.plugins.ksp)
}

android {
    namespace = "com.brainwallet.ltc"
    compileSdk = 36

    packaging {
        resources {
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }
    defaultConfig {
        minSdk = 29

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        resourceConfigurations += listOf(
            "en", "ar", "de", "es", "fa", "fr", "hi", "in", "it", "ja",
            "ko", "pa", "pl", "pt", "ru", "sv", "tr", "uk", "zh-rCN", "zh-rTW"
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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
    implementation(project(":core"))
    implementation(project(":iap"))
    implementation(project(":modules:design-system"))
    implementation(platform(grunt.androidx.compose.bom))
    implementation(grunt.bundles.androidx.compose)
    implementation(grunt.androidx.core.ktx)
    implementation(grunt.material)
    implementation(grunt.app.startup)
    implementation(grunt.kotlin.immutable)
    implementation(grunt.bundles.revenue.cat)
    implementation(platform(grunt.koin.bom))
    implementation(grunt.bundles.koin)
    implementation(platform(grunt.koin.annotation.bom))
    implementation(grunt.koin.annotation)
    ksp(grunt.koin.annotation.compiler)
    androidTestImplementation(platform(grunt.androidx.compose.bom))
    androidTestImplementation(grunt.bundles.androidx.compose.ui.test)
}
