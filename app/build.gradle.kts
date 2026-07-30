import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.gradle.kotlin.dsl.androidTestImplementation
import org.gradle.kotlin.dsl.grunt
import org.gradle.kotlin.dsl.project

plugins {
    alias(grunt.plugins.android.application)
    alias(grunt.plugins.jetbrains.kotlin.android)
    alias(grunt.plugins.jetbrains.kotlin.compose)
    alias(grunt.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(grunt.plugins.ksp)
    alias(grunt.plugins.buildlogic.test)
    alias(grunt.plugins.buildlogic.detekt)
}

val localProperties = gradleLocalProperties(rootDir, providers)

android {
    namespace = "com.brainwallet"
    compileSdk = 36

    firebaseCrashlytics {
        nativeSymbolUploadEnabled = true
    }

    sourceSets["main"].assets.srcDir("${rootProject.projectDir}/bw-gdlib/assets")

    defaultConfig {
        applicationId = "ltd.grunt.brainwallet"
        minSdk = 29
        targetSdk = 35
        versionCode = 202506346
        versionName = "v4.11.0"
        multiDexEnabled = true
        base.archivesName.set("${defaultConfig.versionName}(${defaultConfig.versionCode})")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.addAll(setOf("x86", "x86_64", "armeabi-v7a", "arm64-v8a"))
        }
        ndkVersion = "25.1.8937393"
        externalNativeBuild {
            cmake {
                version = "3.22.1"
                arguments("-DANDROID_TOOLCHAIN=clang")
            }
        }
    }

    assetPacks.addAll(setOf(":install_time_asset_pack"))

    signingConfigs {
        getByName("debug") {
            storeFile = file(localProperties.getProperty("DEBUG_STORE_FILE"))
            storePassword = localProperties.getProperty("DEBUG_STORE_PASSWORD")
            keyAlias = localProperties.getProperty("DEBUG_KEY_ALIAS")
            keyPassword = localProperties.getProperty("DEBUG_KEY_PASSWORD")
        }
        val release by creating {
            storeFile = file(localProperties.getProperty("RELEASE_STORE_FILE"))
            storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD")
            keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS")
            keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        val debug by getting {
            isDebuggable = true
            isMinifyEnabled = false
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
            ndk {
                isDebuggable = true
                isMinifyEnabled = false
            }
            resValue("string", "firebase_analytics_collection_enabled", "false")
        }
        val release by getting {
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            resValue("string", "firebase_analytics_collection_enabled", "true")
            ndk {
                isDebuggable = false
                isMinifyEnabled = true
            }
        }
    }

    externalNativeBuild {
        cmake {
            // When you specify a version of CMake, as shown below,
            // the Android plugin searches for its binary within your
            // PATH environmental variable.
            path("CMakeLists.txt") //path can only be set outside (in android block)
        }
    }

    flavorDimensions.add("mode")
    productFlavors {
        create("brainwallet") {
            dimension = "mode"

            applicationId = "ltd.grunt.brainwallet"
            resValue("string", "app_name", "Brainwallet")

            externalNativeBuild {
                cmake {
                    // When you specify a version of CMake, as shown below,
                    // the Android plugin searches for its binary within your
                    // PATH environmental variable.
                    cFlags("-DLITECOIN_TESTNET=0")
                    targets("core-lib")
                }
            }
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    buildFeatures {
        buildConfig = true
        dataBinding = true
        viewBinding = true
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.valueOf("VERSION_${grunt.versions.jvm.target.get()}")
        targetCompatibility = JavaVersion.valueOf("VERSION_${grunt.versions.jvm.target.get()}")
        isCoreLibraryDesugaringEnabled = true
    }
    kotlin {
        jvmToolchain(grunt.versions.jvm.target.get().toInt())
        compilerOptions {
            jvmTarget.set(
                org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(grunt.versions.jvm.target.get())
            )
        }
    }

    packaging {
        resources {
            pickFirsts.add("protobuf.meta")
            excludes += setOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
                "META-INF/NOTICE.md",
                "META-INF/NOTICE",
                "META-INF/LICENSE"
            )
        }
    }
        testFixtures {
            enable = true
        }
    //TODO: rename output apk/bundle
}

dependencies {
    implementation(project(":iap"))
    implementation(project(":core"))
    implementation("androidx.webkit:webkit:1.9.0")
    implementation(libs.keepsafe.relinker)
    implementation(libs.androidx.benchmark.traceprocessor)
    implementation(libs.androidx.ui.graphics)
    testImplementation(testFixtures(project(":app")))
    androidTestImplementation(testFixtures(project(":app")))
    implementation(grunt.androidx.core.ktx)
    implementation(grunt.app.startup)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.legacy.support)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.gridlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.bundles.androidx.navigation)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.bundles.androidx.lifecycle)
    implementation(libs.androidx.work)
    implementation(libs.androidx.browser)
    implementation(platform(grunt.androidx.compose.bom))
    implementation(grunt.bundles.androidx.compose)
    implementation(grunt.kotlin.immutable)
    implementation(grunt.material)
    implementation(libs.google.material)
    implementation(libs.google.zxing)
    implementation(platform(libs.firebase.bom))
    implementation(platform(libs.firebase.analytics))
    implementation(libs.bundles.firebase)
    implementation(libs.bundles.google.play.asset.delivery)
    implementation(libs.bundles.google.play.feature.delivery)
    implementation(libs.bundles.google.play.review)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(platform(grunt.koin.bom))
    implementation(grunt.bundles.koin)
    implementation(platform(grunt.koin.annotation.bom))
    implementation(grunt.koin.annotation)
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation(libs.androidx.junit.ktx)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.foundation)
    implementation(libs.play.services.games)
    ksp(grunt.koin.annotation.compiler)
    implementation(platform(libs.squareup.okhttp.bom))
    implementation(libs.bundles.squareup.okhttp)
    implementation(libs.bundles.squareup.retrofit)
    implementation(libs.jakewarthon.timber)
    implementation(libs.commons.io)
    implementation(libs.bundles.eclipse.jetty)
    implementation(libs.slf4j)
    implementation(libs.org.json)
    implementation(libs.sigpipe.jbsdiff)
    implementation(libs.unstoppable.domain)
    implementation(libs.razir.progressbutton)
    implementation(libs.appsflyer)
    implementation(libs.android.installreferrer)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.slf4j.android)
    testImplementation(libs.kotlinx.coroutines.tests)
    implementation("androidx.compose.animation:animation:1.10.6")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7")
    implementation("io.coil-kt:coil-compose:2.0.0-rc01")
    implementation("androidx.emoji2:emoji2-emojipicker:1.6.0")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    androidTestImplementation(platform(grunt.androidx.compose.bom))
    androidTestImplementation("androidx.test:core-ktx:1.5.0")
    androidTestImplementation(grunt.bundles.androidx.compose.ui.test)
    androidTestImplementation(libs.bundles.android.test)
    androidTestImplementation(libs.fastlane.screengrab)
    androidTestImplementation(libs.slf4j.android)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("io.mockk:mockk-android:1.13.8")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    val gdxVersion = "1.14.1"
    val miniaudioVersion = "0.7"
    implementation("com.badlogicgames.gdx:gdx-backend-android:${gdxVersion}")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi-v7a")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-arm64-v8a")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86_64")
    implementation("ltd.grunt.brainwallet.gdx:core:1.14.1")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")


}

tasks.withType<Test> {
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}
