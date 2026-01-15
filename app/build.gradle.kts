plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)

}

android {
    namespace = "com.wall.fakelyze"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.wall.fakelyze"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21

    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
        mlModelBinding = true
        buildConfig = true
    }
    lint {
        disable += "UnsafeOptInUsageError"
        checkReleaseBuilds = false
        abortOnError = false
    }
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.database.ktx)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.camera2)
    //noinspection GradleDependency
    implementation(libs.androidx.lifecycle.runtime.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.coil.compose)
    implementation(libs.material.icons.extended)

    //  Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    //  datastore
    implementation (libs.androidx.datastore.preferences)

    //   Navigation
    implementation (libs.androidx.navigation.compose)

//    Permission Accompanist
    implementation (libs.accompanist.permissions)

    // Koin
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // TENSORFLOW LITE (PASTIKAN HANYA INI YANG DIGUNAKAN)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.task.vision)
    // TensorFlow Lite Metadata
    implementation(libs.tensorflow.lite.metadata.v043)

    //Font Google sans
    implementation(libs.androidx.ui.text.google.fonts)

    // Kotlin Serialization
    implementation(libs.kotlinx.serialization.json)

    // CRITICAL: Add missing camera extensions for stability
    implementation(libs.androidx.camera.extensions)

    // CRITICAL: Add missing compose runtime for stability
    implementation(libs.androidx.runtime)

    // CRITICAL: Add missing activity ktx for proper lifecycle
    implementation(libs.androidx.activity.ktx)

    // CRITICAL: Add missing viewmodel compose integration
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // CRITICAL: Add coroutines for Android
    implementation(libs.kotlinx.coroutines.android)

    // CRITICAL: Add bitmap processing support for camera
    implementation(libs.androidx.core.ktx.v1120)
}
