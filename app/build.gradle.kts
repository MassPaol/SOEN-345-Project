plugins {
    alias(libs.plugins.android.application)

    // Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.team_one.soen_345_project"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.team_one.soen_345_project"
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Import the BoM using the catalog reference
    implementation(platform(libs.firebase.bom))
}