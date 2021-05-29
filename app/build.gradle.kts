plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    id("just-gradle-plugin")
    id("quick-fix")
}

android {
    compileSdkVersion(Versions.compileSdkVersion)
    buildToolsVersion = Versions.buildToolsVersion

    defaultConfig {
        applicationId = "com.billkalin.justapp"
        minSdkVersion(Versions.minSdkVersion)
        targetSdkVersion(Versions.targetSdkVersion)
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
        }
        getByName("release") {
            isDebuggable = false
            proguardFiles(getDefaultProguardFile ("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    packagingOptions {
        exclude("*/kotlin/**")
    }

    lintOptions {
        isAbortOnError = false
    }
    dynamicFeatures = mutableSetOf(":feature_device")
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(fileTree("dir" to "libs", "include" to "*.jar"))
    implementation(Depend.Kotlin.stdlib)
    implementation(Depend.Support.appcompat)
    implementation(Depend.Support.androidxCore)
    implementation(Depend.Support.constraintLayout)
//    implementation rootProject.ext.androidx.coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
    api("com.google.android.play:core:1.10.0")

    implementation(project(path = ":performance"))
    implementation(project(path = ":openapi"))
    implementation(project(path = ":hook"))
    implementation(project(path = ":io_monitor"))
    implementation(project(path = ":qq-hot-fix-tool"))
    implementation(project(path = ":breakpad"))
    implementation(project(path = ":quickfix"))
    implementation(project(path = ":quickfix-common"))
}
