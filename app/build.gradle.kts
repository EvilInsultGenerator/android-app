import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.android.sdklib.AndroidVersion.VersionCodes

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    signingConfigs {
        register("release") {
            keyAlias = "evilinsultgenerator"
            keyPassword = "evilinsultgenerator"
            storeFile = file("eig.jks")
            storePassword = "evilinsultgenerator"
        }
    }

    compileSdk = VersionCodes.TIRAMISU

    defaultConfig {
        applicationId = "com.evilinsult"
        minSdk = 21
        targetSdk = 34
        versionCode = 42
        versionName = "4.2"
        vectorDrawables.useSupportLibrary = true
    }

    applicationVariants.all {
        outputs.all {
            val output = this as? BaseVariantOutputImpl
            output?.outputFileName =
                "${defaultConfig.applicationId}-v${defaultConfig.versionName}-${output?.name}.apk"
        }
    }

    buildTypes {
        named("release") {
            isDebuggable = false
            isZipAlignEnabled = true
            isMinifyEnabled = true
            isShrinkResources = false
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"))
            signingConfig = signingConfigs.getByName("release")
        }
    }

    sourceSets.getByName("main") {
        java.setSrcDirs(listOf("src/main/kotlin"))
    }

    packaging {
        resources {
            pickFirsts += listOf("META-INF/core_debug.kotlin_module",
                "META-INF/core_release.kotlin_module",
                "META-INF/library_debug.kotlin_module",
                "META-INF/library_release.kotlin_module")
        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = true
        disable += listOf("MissingTranslation", "GoogleAppIndexingWarning")
    }
    namespace = "com.evilinsult"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.2")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}
