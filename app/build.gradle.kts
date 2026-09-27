plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    kotlin("kapt")
}

android {
    namespace = "com.example.farmer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.farmer"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Add this block to enable generation of the BuildConfig class
    buildFeatures {
        buildConfig = true
    }

    // ✅ ADDED: Define API_BASE_URL for different build types
    buildTypes {
        debug {
            isMinifyEnabled = false
            // LAN IP of this PC (Intel Wi-Fi 6E), so physical phones on the same
            // Wi-Fi can reach the Spring Boot backend. For the emulator use
            // http://10.0.2.2:8080/ instead.
            buildConfigField("String", "API_BASE_URL", "\"http://10.205.155.102:8080/\"")
            // Replace with your Google OAuth *Web* client ID from the Google Cloud console.
            buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"REPLACE_WITH_YOUR_GOOGLE_WEB_CLIENT_ID.apps.googleusercontent.com\"")
        }
        release {
            isMinifyEnabled = false
            // Sign the release APK with the debug key so the resulting file can be
            // downloaded and installed on any device for testing/distribution.
            // Replace with a real upload keystore before publishing to Google Play.
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // For physical device - CHANGE YOUR_IP to your actual server IP
            buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.100:8080/\"")
            // Replace with your Google OAuth *Web* client ID from the Google Cloud console.
            buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"REPLACE_WITH_YOUR_GOOGLE_WEB_CLIENT_ID.apps.googleusercontent.com\"")
        }
    }

    // DECISION: Upgrade to Java 21 to support SDK 35 and Kotlin 2.0
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }
}

dependencies {
    // Core Android libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)

    // Networking (Retrofit)
    implementation(libs.retrofit2)
    implementation(libs.retrofit2.converter.gson)

    // Image Loading (Glide)
    implementation(libs.glide)
    implementation(libs.play.services.cast.framework)
    // Using kapt for Glide compiler as requested
    kapt(libs.glide.compiler)

    // Google Sign-In via Credential Manager
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}