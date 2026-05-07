plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.lbo_marketplace"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.lbo_marketplace"
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(project(":shared-kmp"))

    implementation(platform("androidx.compose:compose-bom:2024.02.01"))

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // ✅ THIS IS THE MISSING ONE (CRITICAL)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")

    // StateFlow support
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")

    // Activity Compose
    implementation("androidx.activity:activity-compose:1.9.0")

    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

        // Firebase BOM
        implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

        // Firebase Auth
        implementation("com.google.firebase:firebase-auth-ktx")

        // Firestore
        implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("androidx.compose.foundation:foundation:1.5.4")
    implementation("com.google.android.gms:play-services-location:21.0.1")
}