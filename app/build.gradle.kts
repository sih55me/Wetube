
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("kotlin-android")

}



android {
    namespace = "app.wetube"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.wetube"
        minSdk = 22
        targetSdk = 34
        versionCode = 23
        versionName = "14.5"
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
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}


dependencies {
    implementation("com.github.yukuku:ambilwarna:2.0.1")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.cocosw:undobar:1.8.1")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.android.support:support-v4:28.0.0")
    implementation("com.android.support:recyclerview-v7:28.0.0")
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.0.0")
    implementation("com.android.support:cardview-v7:28.0.0")
    implementation("com.github.bumptech.glide:glide:3.3.0")
    implementation("com.github.amlcurran.showcaseview:library:5.4.3")
    implementation("com.android.support:palette-v7:28.0.0")


}