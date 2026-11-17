plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val stableKeystorePath = providers.environmentVariable("ANDROID_KEYSTORE_PATH").orNull
val stableStorePassword = providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD").orNull
val stableKeyAlias = providers.environmentVariable("ANDROID_KEY_ALIAS").orNull
val stableKeyPassword = providers.environmentVariable("ANDROID_KEY_PASSWORD").orNull
val stableSigningConfigured = listOf(stableKeystorePath, stableStorePassword, stableKeyAlias, stableKeyPassword).all { !it.isNullOrBlank() }

android {
    namespace = "ir.alishams.hardwareshenas"
    compileSdk = 36

    defaultConfig {
        applicationId = "ir.alishams.hardwareshenas"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        if (stableSigningConfigured) {
            create("stableDebug") {
                storeFile = file(stableKeystorePath!!)
                storePassword = stableStorePassword
                keyAlias = stableKeyAlias
                keyPassword = stableKeyPassword
                storeType = "PKCS12"
            }
        }
    }

    buildTypes {
        getByName("debug") {
            if (stableSigningConfigured) {
                signingConfig = signingConfigs.getByName("stableDebug")
            }
        }
    }

    buildFeatures { compose = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.10.01"))
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    implementation("androidx.camera:camera-core:1.5.1")
    implementation("androidx.camera:camera-camera2:1.5.1")
    implementation("androidx.camera:camera-lifecycle:1.5.1")
    implementation("androidx.camera:camera-view:1.5.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
