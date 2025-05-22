plugins {
    alias(libs.plugins.android.application)
    //alias(libs.plugins.firebase.services)
    id("com.google.gms.google-services")
    //alias(libs.plugins.google.services)
   // alias(libs.plugins.firebase.services)  // Applies the plugin
}

android {
    namespace = "com.example.rapidrestore"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.rapidrestore"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.firebase:firebase-auth")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("com.github.bumptech.glide:glide:4.14.2")
    implementation ("com.google.android.material:material:1.12.0")
    implementation ("androidx.work:work-runtime:2.8.1")
    implementation(libs.firebase.firestore)
    implementation ("com.firebaseui:firebase-ui-firestore:8.0.2")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation(libs.firebase.database)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.14.2")
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}