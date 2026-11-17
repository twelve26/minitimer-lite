plugins {
    id("com.android.application") version "8.13.2"
    kotlin("multiplatform") version "2.3.10"
    kotlin("plugin.compose") version "2.3.10"
    kotlin("plugin.serialization") version "2.3.10"
    id("org.jetbrains.compose") version "1.9.3"
}

kotlin {
    androidTarget {
        compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "MiniTimerShared"
            isStatic = true
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(compose.foundation)
            implementation(compose.components.resources)
            implementation(compose.material3)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        androidUnitTest.dependencies {
            implementation(kotlin("test-junit"))
        }
        androidMain.dependencies {
            implementation("androidx.activity:activity-compose:1.10.1")
        }
    }
}

android {
    namespace = "app.contadores"
    compileSdk = 36
    buildToolsVersion = "36.0.0"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    defaultConfig {
        applicationId = "app.contadores"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1"
    }
}

compose.resources {
    packageOfResClass = "app.contadores.resources"
}
