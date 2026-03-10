plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.qtrobot"
    compileSdk = 36 // Set to 36 to support the latest androidx libraries

    defaultConfig {
        applicationId = "com.example.qtrobot"
        minSdk = 24
        targetSdk = 35 // Keep target at 35 for stability
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
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.cardview:cardview:1.0.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Room DB
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    
    // Lifecycle & LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-core:2.8.7")

    // ZXing for QR Code generation
    implementation(libs.zxing)

    // Google Play Services for Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Retrofit for HTTP requests
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp for HTTP client
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
}
