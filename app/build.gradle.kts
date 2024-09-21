plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.yinlin.rachel"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yinlin.rachel"
        minSdk = 29
        targetSdk = 34
        versionCode = 122
        versionName = "1.2.2-alpha"

        ndk {
            abiFilters += "arm64-v8a"
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        register("release") {
            keyAlias = "rachel"
            keyPassword = "rachel1211"
            storeFile = file("key.jks")
            storePassword = "rachel1211"
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    viewBinding {
        enable = true
    }
}

dependencies {
    implementation(fileTree(mapOf(
        "dir" to "libs",
        "include" to listOf("*.aar", "*.jar"),
    )))

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.exifinterface:exifinterface:1.3.6")
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation("com.haibin:calendarview:3.7.1")

    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.google.android.material:material:1.10.0")

    implementation("com.github.AbdAlrahmanShammout:UltimateBreadcrumbsView:1.0.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.github.chaychan:BottomBarLayout:3.0.0")
    implementation("com.github.forJrking:KLuban:1.1.0")
    implementation("com.github.xuexiangjys:XUI:1.2.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("io.github.lucksiege:pictureselector:v3.11.2")
    implementation("io.github.lucksiege:ucrop:v3.11.2")
    implementation("io.github.scwang90:refresh-layout-kernel:2.1.0")
    implementation("io.github.scwang90:refresh-header-classics:2.1.0")

    implementation("com.tencent:mmkv:1.3.9")
    implementation("com.tencent.tav:libpag:4.4.8-harfbuzz")

    testImplementation("junit:junit:4.13.2")
}