import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.kapt)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

val localProperties = gradleLocalProperties(rootDir, providers)

android {
    namespace = "com.breadwallet"
    compileSdk = 34

    defaultConfig {
        applicationId = "ltd.grunt.brainwallet"
        minSdk = 29
        targetSdk = 34
        versionCode = 202501251
        versionName = "v4.1.1"

        multiDexEnabled = true

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

    assetPacks.addAll(setOf(":install_time_asset_pack", ":fast_follow_asset_pack_01"))

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
            buildConfigField("boolean", "LITECOIN_TESTNET", "false")

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
            buildConfigField("boolean", "LITECOIN_TESTNET", "false")
            buildConfigField("String[]", "SCREENGRAB_PAPERKEY", 
                "new String[] {${localProperties.getProperty("SCREENGRAB_PAPERKEY", "")
                    .split(",")
                    .joinToString { "\"$it\"" }}}")

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

val ktlint by configurations.creating

dependencies {
    ktlint(libs.pinterest.ktlint) {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }

    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.legacy.support)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.gridlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.bundles.androidx.navigation)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.browser)
    implementation(libs.google.material)
    implementation(libs.google.zxing)
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)
    implementation(libs.bundles.google.play.asset.delivery)
    implementation(libs.bundles.google.play.feature.delivery)
    implementation(libs.bundles.google.play.review)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.squareup.okhttp)
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

    androidTestImplementation(libs.bundles.android.test)
    androidTestImplementation(libs.fastlane.screengrab)
}

val ktlintCheck by tasks.registering(JavaExec::class) {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Check Kotlin code style"
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    // see https://pinterest.github.io/ktlint/install/cli/#command-line-usage for more information
    args(
        "**/src/**/*.kt",
        "**.kts",
        "!**/build/**",
    )
}

tasks.check {
    dependsOn(ktlintCheck)
}

tasks.register<JavaExec>("ktlintFormat") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Check Kotlin code style and format"
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
    // see https://pinterest.github.io/ktlint/install/cli/#command-line-usage for more information
    args(
        "-F",
        "**/src/**/*.kt",
        "**.kts",
        "!**/build/**",
    )
}
