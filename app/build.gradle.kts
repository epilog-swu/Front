plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services") // Google services Gradle plugin 추가
}

android {
    namespace = "com.epi.epilog"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.epi.epilog"
        minSdk = 24
        targetSdk = 34
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
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        viewBinding = true
        dataBinding = false
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

    implementation ("com.google.android.gms:play-services-location:21.0.1")


    //메인 달력 위해서 추가
    implementation("com.kizitonwose.calendar:view:2.5.1")
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.recyclerview) // View
    coreLibraryDesugaring ("com.android.tools:desugar_jdk_libs:2.0.4")

    //바텀네비게이션추가
    implementation ("com.github.ismaeldivita:chip-navigation-bar:1.4.0")

    implementation ("androidx.core:core:1.10.1")
    implementation ("androidx.appcompat:appcompat:1.7.0")
    implementation ("cz.msebera.android:httpclient:4.4.1.2")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation (libs.androidx.viewpager2)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.viewpager2)
    implementation(libs.dotsindicator)
    implementation(libs.circleindicator)
    implementation(libs.prolificinteractive.material.calendarview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Firebase 메시징 서비스
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging:23.0.5")
}

// Google 서비스 플러그인을 적용합니다
apply(plugin = "com.google.gms.google-services")