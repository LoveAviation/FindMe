plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.findme"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.findme"
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
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(project(":account_fb"))
    implementation(project(":forms_sup"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")

    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-analytics")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:glide:4.16.0")

    implementation("com.google.dagger:hilt-android:2.52")
    kapt("com.google.dagger:hilt-android-compiler:2.52")

    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation("org.osmdroid:osmdroid-mapsforge:6.1.18")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    implementation("com.google.android.material:material:1.12.0")

    implementation("com.android.support:appcompat-v7:28.0.0")

    implementation("androidx.core:core:1.15.0")

    implementation("androidx.work:work-runtime-ktx:2.10.0")
}