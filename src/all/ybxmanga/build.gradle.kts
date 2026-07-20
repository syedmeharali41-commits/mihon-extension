plugins {
    id("com.android.application")
    id("kotlin-android")
}

ext {
    set("appName", "Tachiyomi: YBX Manga")
    set("pkgVersionCode", 1)
    set("libVersion", "1.4")
}

android {
    namespace = "eu.kanade.tachiyomi.extension.en.ybxmanga"
    compileSdk = 34

    defaultConfig {
        applicationId = "eu.kanade.tachiyomi.extension.en.ybxmanga"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
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
    compileOnly(project(":core"))
    compileOnly("org.jsoup:jsoup:1.17.2")
    compileOnly("com.squareup.okhttp3:okhttp:4.12.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
}
