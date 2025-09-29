import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.gradle.kotlin.dsl.grunt

plugins {
    alias(grunt.plugins.android.application)
    alias(grunt.plugins.jetbrains.kotlin.android)
    alias(grunt.plugins.jetbrains.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.kapt)
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

    defaultConfig {
        applicationId = "ltd.grunt.brainwallet"
        minSdk = 29
        targetSdk = 36
        versionCode = 202506296
        versionName = "v4.7.2"

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

            ndk {
                isDebuggable = true
                isMinifyEnabled = false
            }

            firebaseCrashlytics {
                nativeSymbolUploadEnabled = true
            }
            buildConfigField("String[]", "SCREENGRAB_PAPERKEY",
                "new String[] {${
                    localProperties.getProperty("SCREENGRAB_PAPERKEY", "")
                        .split(" ")
                        .joinToString { "\"$it\"" }
                }}"
            )
        }

        val release by getting {
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")

            ndk {
                isDebuggable = false
                isMinifyEnabled = true
            }

            firebaseCrashlytics {
                nativeSymbolUploadEnabled = true
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

        create("screengrab") {
            dimension = "mode"

            applicationId = "ltd.grunt.brainwallet.screengrab"
            versionNameSuffix = "-screengrab"
            resValue("string", "app_name", "Brainwallet (screengrab)")

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    packaging {
        resources {
            pickFirsts.add("protobuf.meta")
        }
    }

    //TODO: rename output apk/bundle
}

dependencies {
    implementation(project(":games"))
    implementation("androidx.webkit:webkit:1.9.0")
    implementation(grunt.androidx.core.ktx)
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
    implementation (libs.airbnb.lottie.compose)
    implementation(platform(grunt.koin.bom))
    implementation(grunt.bundles.koin)
    implementation(platform(grunt.koin.annotation.bom))
    implementation(grunt.koin.annotation)
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

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.slf4j.android)
    testImplementation(libs.kotlinx.coroutines.tests)

    androidTestImplementation(platform(grunt.androidx.compose.bom))
    androidTestImplementation(grunt.bundles.androidx.compose.ui.test)
    androidTestImplementation(libs.bundles.android.test)
    androidTestImplementation(libs.fastlane.screengrab)
    androidTestImplementation(libs.slf4j.android)
}

tasks.withType<Test> {
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}
