import java.io.File
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { load(it) }
}

fun secretProp(name: String): String? =
    (project.findProperty(name) as String?)?.takeIf { it.isNotBlank() }

val storeFilePath = secretProp("KEYSTORE_FILE") ?: keystoreProps.getProperty("storeFile")
val storePass = secretProp("KEYSTORE_PASSWORD") ?: keystoreProps.getProperty("storePassword")
val keyAliasName = secretProp("KEY_ALIAS") ?: keystoreProps.getProperty("keyAlias")
val keyPass = secretProp("KEY_PASSWORD") ?: keystoreProps.getProperty("keyPassword")

val resolvedKeystore: File? = storeFilePath?.let {
    val f = File(it)
    if (f.isAbsolute) f else rootProject.file(it)
}
val canSignRelease = resolvedKeystore?.exists() == true &&
    !storePass.isNullOrBlank() && !keyAliasName.isNullOrBlank() && !keyPass.isNullOrBlank()

val testAdmobAppId = "ca-app-pub-3940256099942544~3347511713"
val testRewardedId = "ca-app-pub-3940256099942544/5224354917"
val admobAppId = (project.findProperty("ADMOB_APP_ID") as String?) ?: testAdmobAppId
val rewardedId = (project.findProperty("ADMOB_REWARDED_ID") as String?) ?: testRewardedId

if (project.hasProperty("requireRelease")) {
    if (!canSignRelease) {
        throw GradleException(
            "requireRelease is set but no signing key was supplied. This build " +
                "would be signed with the debug key and Play would reject it."
        )
    }
    if (admobAppId == testAdmobAppId || rewardedId == testRewardedId) {
        throw GradleException(
            "requireRelease is set but the AdMob ids are still Google's test ids. " +
                "This build would ship test ads to production and earn nothing."
        )
    }
}

android {
    namespace = "com.mikmy.emberdepth"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mikmy.emberdepth"
        minSdk = 26
        targetSdk = 36
        versionCode = secretProp("VERSION_CODE")?.toIntOrNull() ?: 2
        versionName = secretProp("VERSION_NAME") ?: "1.0.0"
        resourceConfigurations += setOf("en")

        manifestPlaceholders["ADMOB_APP_ID"] = admobAppId
        buildConfigField("String", "AD_REWARDED_ID", "\"$rewardedId\"")
    }

    signingConfigs {
        if (canSignRelease) {
            create("release") {
                storeFile = resolvedKeystore
                storePassword = storePass
                keyAlias = keyAliasName
                keyPassword = keyPass
                val n = resolvedKeystore!!.name.lowercase()
                if (n.endsWith(".p12") || n.endsWith(".pfx")) storeType = "PKCS12"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (canSignRelease) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
        debug {
            isMinifyEnabled = false
            manifestPlaceholders["ADMOB_APP_ID"] = testAdmobAppId
            buildConfigField("String", "AD_REWARDED_ID", "\"$testRewardedId\"")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = true
        htmlReport = true
        textReport = true
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
    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.3")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Ads
    implementation("com.google.android.gms:play-services-ads:23.3.0")

    // Billing
    implementation("com.android.billingclient:billing-ktx:8.0.0")

    // Test
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}
