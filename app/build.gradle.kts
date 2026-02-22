plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.aana.aegislink"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.aana.aegislink"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0-alpha"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

val clearUrlsRulesUrl = "https://rules2.clearurls.xyz/data.minify.json"
val clearUrlsRulesAsset = file("src/main/assets/data.minify.json")

tasks.register("downloadClearUrlsRules") {
    inputs.property("rulesUrl", clearUrlsRulesUrl)
    outputs.file(clearUrlsRulesAsset)

    doLast {
        clearUrlsRulesAsset.parentFile.mkdirs()
        val shouldDownload = !clearUrlsRulesAsset.exists() || clearUrlsRulesAsset.length() == 0L
        if (shouldDownload) {
            java.net.URL(clearUrlsRulesUrl).openStream().use { input ->
                clearUrlsRulesAsset.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}

tasks.matching { it.name == "mergeDebugAssets" || it.name == "mergeReleaseAssets" }.configureEach {
    dependsOn("downloadClearUrlsRules")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.material)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.security.crypto)
    implementation(libs.okhttp)
}
