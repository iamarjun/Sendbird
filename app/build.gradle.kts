plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    compileSdk = 31
    buildToolsVersion = "30.0.3"

    defaultConfig {
        applicationId = "com.arjun.sendbird"
        minSdk = 21
        targetSdk = 30
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.0.0-beta08"
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
}

dependencies {

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    implementation("com.google.modernstorage:modernstorage-mediastore:1.0.0-alpha01")

    implementation("androidx.core:core-ktx:1.6.0")
    implementation("com.google.android.material:material:1.4.0")

    implementation("androidx.appcompat:appcompat:1.4.0-beta01")

    implementation("androidx.compose.ui:ui:1.1.0-alpha06")
    implementation("androidx.compose.ui:ui-tooling:1.1.0-alpha06")
    implementation("androidx.compose.foundation:foundation:1.1.0-alpha06")
    implementation("androidx.compose.material:material:1.1.0-alpha06")
    implementation("androidx.compose.material:material-icons-core:1.1.0-alpha06")
    implementation("androidx.compose.material:material-icons-extended:1.1.0-alpha06")
    implementation("androidx.compose.runtime:runtime-livedata:1.1.0-alpha06")

    implementation("androidx.activity:activity-compose:1.4.0-rc01")
    implementation("androidx.navigation:navigation-compose:2.4.0-alpha10")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0-alpha03")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.4.0-rc01")

    implementation("androidx.ui:ui-tooling:1.0.0-alpha07")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.google.dagger:hilt-android:2.39.1")
    kapt("com.google.dagger:hilt-compiler:2.39.1")

    implementation("com.google.accompanist:accompanist-glide:0.15.0")
    implementation("com.google.accompanist:accompanist-insets:0.19.0")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.19.0")

    implementation("com.sendbird.sdk:sendbird-android-sdk:3.0.170")

    implementation("com.jakewharton.timber:timber:5.0.1")

    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.0-rc01")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0-rc01")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.4.0-rc01")

    implementation("androidx.datastore:datastore-preferences:1.0.0")

}