import java.security.KeyStore

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
        val password = System.getenv("KEYSTORE_PASSWORD") ?: ""
        // A keystore exported by Windows does not keep the alias it was given, so fall back to
        // whichever alias the store really contains.
        val store = KeyStore.getInstance("pkcs12").apply {
            file(keystorePath).inputStream().use { load(it, password.toCharArray()) }
        }
        val wantedAlias = System.getenv("KEY_ALIAS") ?: ""
        val realAlias = if (store.containsAlias(wantedAlias)) wantedAlias else store.aliases().toList().first()
        signingConfigs {
            create("shared") {
                storeFile = file(keystorePath)
                storeType = "pkcs12"
                storePassword = password
                keyAlias = realAlias
                keyPassword = password
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
