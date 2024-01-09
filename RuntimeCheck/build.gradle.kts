plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
//    id("uploadGithub")
}

//upload {
//    groupId = "com.github.MRwangqi"
//    artifactId = "runtimecheck"
//    version = "1.0.0"
//    sourceJar = false
//    hasPomDepend = true
//}

android {
    namespace = "com.codelang.runtimecheck"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        targetSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("top.canyie.pine:core:0.2.8")
    implementation("com.google.code.gson:gson:2.10")
}