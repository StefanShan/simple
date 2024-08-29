plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.kotlinKapt)
}

android {
    namespace = "com.stefan.splitapkdemo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.stefan.splitapkdemo"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        flavorDimensions.apply {
            add("type")
            add("channel")
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    productFlavors {
        create("free") {
            dimension = "type"
            applicationIdSuffix = ".free"
        }
        create("vip") {
            dimension = "type"
            applicationIdSuffix = ".vip"
        }
        create("huawei"){
            applicationIdSuffix = ".huawei"
            dimension = "channel"
        }
        create("xiaomi"){
            applicationIdSuffix = ".xiaomi"
            dimension = "channel"
        }
    }

    sourceSets {
        getByName("free") {
            java.srcDirs("src/free/java","src/main/java")
        }
        getByName("vip") {
            java.srcDirs("src/vip/java","src/main/java")
        }
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.google.autoservice)
    implementation(libs.google.autoservice)
    kapt(libs.google.autoservice)
    "huaweiImplementation"(project(":huawei"))
    "xiaomiImplementation"(project(":xiaomi"))
}