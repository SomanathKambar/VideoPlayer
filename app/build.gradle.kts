plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.somanath.videoplayer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.somanath.videoplayer"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
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
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

//    implementation("androidx.compose.ui:ui:1.6.0")
//    implementation("androidx.compose.foundation:foundation:1.6.0")
//    implementation("androidx.compose.material:material:1.6.0")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // ExoPlayer Core
    implementation("androidx.media3:media3-exoplayer:1.5.1")

    // ExoPlayer UI (For default controls, optional)
    implementation("androidx.media3:media3-ui:1.5.1")

    // ExoPlayer HLS/DASH Support
    implementation("androidx.media3:media3-exoplayer-hls:1.5.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.5.1")
    implementation("androidx.media3:media3-database:1.5.1") // Needed for download manager

    implementation("androidx.media3:media3-datasource-okhttp:1.5.1") // DRM & HTTP Requests
    implementation("androidx.media3:media3-datasource-cronet:1.5.1") // DRM with Cronet (optional)
    implementation("androidx.media3:media3-session:1.5.1") // Media session handling
    implementation("androidx.navigation:navigation-compose:2.8.6")
    implementation("com.google.accompanist:accompanist-permissions:0.30.1")
    implementation("com.github.skydoves:landscapist-glide:2.2.10")

    // ExoPlayer DRM Support (Widevine)
//    implementation("androidx.media3:media3-exoplayer-drm:1.0.0")

}