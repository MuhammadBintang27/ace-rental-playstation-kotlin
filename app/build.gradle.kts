import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("plugin.serialization") version "2.1.0" // Sesuaikan dengan versi Kotlin yang digunakan

    id("kotlin-kapt")
}

android {
    namespace = "com.ace.playstation"
    compileSdk = 35 // ✅ Gunakan versi stabil (Compile SDK 35 masih preview)

    defaultConfig {
        applicationId = "com.ace.playstation"
        minSdk = 24
        targetSdk = 35 // ✅ Target SDK disesuaikan dengan compile SDK yang stabil
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties()
        val localPropertiesFile = File(rootProject.rootDir, "local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }

        buildConfigField("String", "SUPABASE_ANON_KEY", properties.getProperty("SUPABASE_ANON_KEY", ""))
        buildConfigField("String", "SUPABASE_URL", properties.getProperty("SUPABASE_URL", ""))
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
        sourceCompatibility = JavaVersion.VERSION_17 // ✅ Gunakan Java 17 yang lebih kompatibel
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.room.runtime.android)
    val ktorVersion = "3.1.0"
    val hiltVersion = "1.1.0"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:3.1.0")
    implementation("io.github.jan-tennert.supabase:storage-kt:3.1.0")
    implementation("io.github.jan-tennert.supabase:auth-kt:3.1.0")
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-utils:$ktorVersion")
//    implementation ("io.github.jan-tennert.supabase:gotrue-kt:2.6.0")
//    implementation ("io.github.jan-tennert.supabase:compose-auth:1.4.3")

//    implementation("com.google.dagger:hilt-android:2.5.0")
//    annotationProcessor("com.google.dagger:hilt-compiler:2.5.0")
//    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")

    // Lainnya
    implementation(libs.mpandroidchart)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.foundation.android)
    implementation(libs.play.services.auth)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
