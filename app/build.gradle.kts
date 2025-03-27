plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") // Apply KSP plugin
}

android {
    namespace = "com.example.listsqre_revamped"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.listsqre_revamped"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3" // Use the latest Compose compiler version
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.9")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Core Compose UI and Material Design 3
    implementation("androidx.compose.ui:ui:1.7.8")

    // Material Design 3
    implementation("androidx.compose.material3:material3:1.3.1")

    // Room database integration
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Tooling for Preview
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.8")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.8")

    // Compose Activity
    implementation("androidx.activity:activity-compose:1.10.1")

    // Optional - Compose ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Optional - Testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.8")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.8")
}