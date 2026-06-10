//game-bridge/build.gradle.kts
plugins {
    alias(grunt.plugins.android.library)
    alias(grunt.plugins.jetbrains.kotlin.android)
    alias(grunt.plugins.jetbrains.kotlin.compose)
}

android {
    namespace = "com.brainwallet.gamebridge"
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
    sourceSets["main"].assets.srcDir("${rootProject.projectDir}/bw-gdlib/assets")
}

dependencies {
    implementation("ltd.grunt.brainwallet.gdx:core:1.0.2")
    implementation(platform(grunt.koin.bom))
    implementation(project(":game-contract"))
    implementation(platform(grunt.androidx.compose.bom))
    implementation(grunt.bundles.androidx.compose)
    implementation(grunt.bundles.koin)

    val gdxVersion = "1.14.1"
    implementation("com.badlogicgames.gdx:gdx-backend-android:$gdxVersion")
}
// Method to extract the .so files into the jniLabs
// 1. Set the CACHE: CACHE=~/.gradle/caches/modules-2/files-2.1/com.badlogicgames.gdx/gdx-XXXXX-1.14.1
// 2. Run this in terminal: for abi in armeabi-v7a arm64-v8a x86 x86_64; do
//  mkdir -p game-bridge/src/main/jniLibs/$abi
//  jar=$(find $CACHE -name "gdx-XXXXX-1.14.1-natives-$abi.jar")
//  unzip -o "$jar" -d /tmp/gdxnat-$abi "*.so"
//  cp /tmp/gdxnat-$abi/*.so game-bridge/src/main/jniLibs/$abi/
//  done
