plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.dnsswitch"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.dnsswitch"
        minSdk = 28
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"
    }

    // Chave fixa, para que atualizações instalem por cima sem apagar o app (e as permissões).
    signingConfigs {
        create("shared") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("shared")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
