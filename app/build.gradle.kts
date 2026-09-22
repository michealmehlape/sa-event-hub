plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    buildFeatures {
        viewBinding = true
    }
    namespace = "com.example.sa_event_hub"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.sa_event_hub"
        minSdk = 26
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.glide)

}

// Some library asked for a newer androidx.core than our project supports.
// This line tells Gradle to use version 1.17.0 instead.
// Use androidx.core 1.18.0 (newest version that works with our project)
configurations.all {
    resolutionStrategy {
        force("androidx.core:core:1.18.0")
        force("androidx.core:core-ktx:1.18.0")
    }
}