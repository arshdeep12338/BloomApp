import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// ---- AdMob IDs ------------------------------------------------------------
// Google's official sample/test IDs. They are the default so the project always
// builds with safe IDs. Override per environment through local.properties or
// gradle.properties (keys: ADMOB_APP_ID, ADMOB_APP_OPEN_AD_UNIT_ID,
// ADMOB_BANNER_AD_UNIT_ID). Debug builds ALWAYS use the test IDs.
object TestAdIds {
    const val APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val APP_OPEN = "ca-app-pub-3940256099942544/9257395921"
    const val BANNER = "ca-app-pub-3940256099942544/9214589741" // adaptive banner
}

val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun adProp(key: String, default: String): String =
    localProps.getProperty(key) ?: providers.gradleProperty(key).orNull ?: default

android {
    namespace = "com.bloom.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bloom.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        debug {
            manifestPlaceholders["admobAppId"] = TestAdIds.APP_ID
            buildConfigField("String", "ADMOB_APP_ID", "\"${TestAdIds.APP_ID}\"")
            buildConfigField("String", "APP_OPEN_AD_UNIT_ID", "\"${TestAdIds.APP_OPEN}\"")
            buildConfigField("String", "BANNER_AD_UNIT_ID", "\"${TestAdIds.BANNER}\"")
        }
        release {
            val appId = adProp("ADMOB_APP_ID", TestAdIds.APP_ID)
            val appOpen = adProp("ADMOB_APP_OPEN_AD_UNIT_ID", TestAdIds.APP_OPEN)
            val banner = adProp("ADMOB_BANNER_AD_UNIT_ID", TestAdIds.BANNER)
            manifestPlaceholders["admobAppId"] = appId
            buildConfigField("String", "ADMOB_APP_ID", "\"$appId\"")
            buildConfigField("String", "APP_OPEN_AD_UNIT_ID", "\"$appOpen\"")
            buildConfigField("String", "BANNER_AD_UNIT_ID", "\"$banner\"")

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Demo only: signed with the debug key so the release APK is installable.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.8.5")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")

    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    implementation("com.google.android.gms:play-services-ads:23.6.0")
}
