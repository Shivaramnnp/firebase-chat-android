plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.googleServices) // Apply the google-services plugin
}

android {
    namespace = "com.shivasruthi.onlyss" // Make sure this matches your package name
    compileSdk = 34 // Or your target SDK, ensure it's high enough for latest Compose/Firebase features

    defaultConfig {
        applicationId = "com.shivasruthi.onlyss"
        minSdk = 26 // As decided earlier
        targetSdk = 34 // Match compileSdk
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true // Set to true for production release builds
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8 // Or higher like 11 or 17 if preferred
        targetCompatibility = JavaVersion.VERSION_1_8 // Or higher
    }
    kotlinOptions {
        jvmTarget = "1.8" // Or higher
    }
    buildFeatures {
        compose = true // Enable Jetpack Compose
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get() // Use version from TOML
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose (using BOM)
    implementation(platform(libs.androidx.compose.bom)) // Import the Compose BOM
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview) // For @Preview annotations
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.wear:wear:1.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // In app/build.gradle.kts -> dependencies
    implementation("io.agora.rtc:full-sdk:4.3.0") // Check for the latest version on Agora's site
    implementation(platform(libs.firebase.bom)) // Import the Firebase BOM
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.android.material)
    // implementation(libs.firebase.storage.ktx) // Uncomment when needed
    // implementation(libs.firebase.messaging.ktx) // Uncomment when needed

    // ... other Firebase dependencies (auth, database, analytics)
    implementation(platform(libs.firebase.bom)) // Ensure Firebase BOM is there
    implementation(libs.firebase.messaging.ktx) // For FCM (Kotlin extensions)

    // Testing (Optional for now)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom)) // For Compose UI tests
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)// For Compose UI tests
    debugImplementation(libs.androidx.compose.ui.tooling) // For Compose tools like Layout Inspector
    debugImplementation(libs.androidx.compose.ui.test.manifest) // For Compose UI tests

}