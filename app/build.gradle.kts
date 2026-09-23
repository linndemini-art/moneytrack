plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.moneytrack.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.moneytrack.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "1.2"
    }

    signingConfigs {
        create("release") {
            storeFile = file(
                System.getenv("MONEYTRACK_KEYSTORE_PATH")
                    ?: "moneytrack-release.jks"
            )
            storePassword =
                System.getenv("MONEYTRACK_KEYSTORE_PASSWORD")
            keyAlias =
                System.getenv("MONEYTRACK_KEY_ALIAS")
            keyPassword =
                System.getenv("MONEYTRACK_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
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
}
