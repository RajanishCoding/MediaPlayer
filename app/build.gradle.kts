plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.mediaplayer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mediaplayer"
        minSdk = 24
        targetSdk = 34
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
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }

}


dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.gridlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.session)

    implementation(libs.gson)

    implementation(libs.glide)
    annotationProcessor(libs.compiler)

    implementation(libs.androidx.swiperefreshlayout)

    implementation(libs.androidx.media.v170)
    implementation(libs.core.v1110)

    implementation(libs.flexbox)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

//    implementation(libs.ffmpeg.kit.full)
//    implementation (libs.mobile.ffmpeg.full)
//    implementation(libs.ffmpeg.kit.full.v51lts)

    implementation("com.arthenica:ffmpeg-kit-full:6.0-2")
//    implementation("com.arthenica:mobile-ffmpeg-full-gpl:4.4.LTS")

//    implementation("com.github.Arthenica:ffmpeg-kit:5.1.LTS")
//    implementation("com.github.Arthenica:ffmpeg-kit-full:4.5.LTS")
//    implementation("com.github.Arthenica:ffmpeg-kit-full-gpl:5.1.LTS")
//    implementation("com.github.Arthenica:ffmpeg-kit-min-gpl:5.1.LTS")


    implementation("org.jellyfin.media3:media3-ffmpeg-decoder:1.3.1+2")

}