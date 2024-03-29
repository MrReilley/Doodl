plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.doodl"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.doodl"
        minSdk = 27
        targetSdk = 33
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
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-runtime-ktx:2.7.5")
    implementation("androidx.tv:tv-material:1.0.0-alpha10")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // For Compose
    implementation ("androidx.compose.ui:ui:1.5.4")
    implementation ("androidx.compose.ui:ui-tooling:1.5.4")
    implementation ("androidx.compose.runtime:runtime:1.5.4")
    implementation ("androidx.compose.foundation:foundation:1.5.4")
    implementation ("androidx.compose.material:material:1.5.4")
    implementation ("androidx.compose.material:material-icons-core:1.5.4")
    implementation ("androidx.compose.material:material-icons-extended:1.5.4")

    // For Navigation Compose
    implementation ("androidx.navigation:navigation-compose:2.7.5")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation ("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime:2.6.2")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.6.2")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    //Firebase
    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")
    implementation("com.google.firebase:firebase-database-ktx:20.3.0")
    implementation("com.google.firebase:firebase-firestore-ktx:24.9.1")
    implementation("com.google.firebase:firebase-auth-ktx:22.2.0")
    //LiveData Compose dependency
    implementation("androidx.compose.runtime:runtime-livedata:1.5.4")
    //Navigation compose
    implementation ("androidx.navigation:navigation-compose:2.7.5")
    implementation ("com.google.android.material:material:1.10.0")
    implementation ("androidx.compose.material:material:1.5.4")

    // Color Picker
    implementation("com.github.skydoves:colorpicker-compose:1.0.5")
    //Coil
    implementation("io.coil-kt:coil-compose:2.4.0")

    implementation("com.google.accompanist:accompanist-swiperefresh:0.27.0")

}