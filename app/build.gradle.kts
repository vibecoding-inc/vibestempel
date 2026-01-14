plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20"
}

android {
    namespace = "com.vibestempel.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.vibestempel.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        debug {
            // Load Supabase credentials from local.properties for debug builds
            val properties = org.jetbrains.kotlin.konan.properties.Properties()
            val localPropertiesFile = rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                properties.load(localPropertiesFile.inputStream())
            }
            
            buildConfigField("String", "SUPABASE_URL", "\"${properties.getProperty("supabase.url", "")}\"")
            buildConfigField("String", "SUPABASE_KEY", "\"${properties.getProperty("supabase.key", "")}\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // For release builds, use environment variables or build parameters
            buildConfigField("String", "SUPABASE_URL", "\"${System.getenv("SUPABASE_URL") ?: ""}\"")
            buildConfigField("String", "SUPABASE_KEY", "\"${System.getenv("SUPABASE_KEY") ?: ""}\"")
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
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.8.2")
    
    // QR Code Generation
    implementation("com.google.zxing:core:3.5.2")
    
    // QR Code Scanning
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    
    // JSON Serialization
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Supabase
    implementation(platform("io.github.jan-tennert.supabase:bom:2.0.0"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    implementation("io.ktor:ktor-client-android:2.3.7")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}
