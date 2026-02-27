// Top-level build file (YourProjectName/build.gradle.kts)
plugins {
    // Example 1: Directly specified version
    // id("com.android.application") version "8.2.2" apply false
    // id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    // id("com.google.gms.google-services") version "4.4.1" apply false

    // Example 2: Using aliases (like your project)
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.googleServices) apply false
}