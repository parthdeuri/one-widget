plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.parth.onewidget"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.parth.onewidget"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }

    // A fixed signing key (supplied by CI from repository secrets) lets new builds install as
    // updates. Without it Gradle falls back to a throwaway debug key and each build differs.
    val keystorePath = System.getenv("KEYSTORE_FILE")
    if (!keystorePath.isNullOrEmpty()) {
        signingConfigs {
            create("shared") {
                storeFile = file(keystorePath)
                storeType = "pkcs12"
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            if (!keystorePath.isNullOrEmpty()) signingConfig = signingConfigs.getByName("shared")
        }
        release {
            isMinifyEnabled = false
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

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
}
