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

    buildFeatures {
        buildConfig = true
    }
    buildFeatures {
        dataBinding = true
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation("androidx.core:core:1.13.1")
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("nl.dionsegijn:konfetti-xml:2.0.4")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Room DB
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

    // Lifecycle & LiveData
    val lifecycleVersion = "2.8.7"
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel:${lifecycleVersion}")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata:${lifecycleVersion}")

    // ZXing for QR Code generation
    implementation(libs.zxing)

    // Google Play Services for Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Retrofit (HTTP client to talk to AWS)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    // logs HTTP calls
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // OkHttp for HTTP client
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
}
