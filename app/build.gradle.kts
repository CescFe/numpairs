import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val appVersionPropertiesFile = rootProject.layout.projectDirectory.file("version.properties")
check(appVersionPropertiesFile.asFile.isFile) {
    "Missing app version contract at ${appVersionPropertiesFile.asFile.path}. " +
        "Add VERSION_NAME and VERSION_CODE."
}

val appVersionProperties = Properties().apply {
    providers.fileContents(appVersionPropertiesFile).asText.get().reader().use { reader ->
        load(reader)
    }
}
val appVersionName = checkNotNull(
    appVersionProperties.getProperty("VERSION_NAME")?.trim()?.takeIf { value -> value.isNotEmpty() }
) {
    "Missing VERSION_NAME in ${appVersionPropertiesFile.asFile.path}. " +
        "Set it to strict numeric SemVer such as 1.0.0."
}
check(Regex("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)$").matches(appVersionName)) {
    "Invalid VERSION_NAME '$appVersionName' in ${appVersionPropertiesFile.asFile.path}. " +
        "Expected strict numeric SemVer in MAJOR.MINOR.PATCH form, such as 1.0.0."
}

val rawAppVersionCode = checkNotNull(
    appVersionProperties.getProperty("VERSION_CODE")?.trim()?.takeIf { value -> value.isNotEmpty() }
) {
    "Missing VERSION_CODE in ${appVersionPropertiesFile.asFile.path}. Set it to a positive integer."
}
val appVersionCode = rawAppVersionCode.toIntOrNull()
    ?: error(
        "Invalid VERSION_CODE '$rawAppVersionCode' in ${appVersionPropertiesFile.asFile.path}. " +
            "Expected a positive 32-bit integer."
    )
check(appVersionCode > 0) {
    "Invalid VERSION_CODE '$rawAppVersionCode' in ${appVersionPropertiesFile.asFile.path}. " +
        "Expected a positive integer greater than zero."
}

android {
    namespace = "org.cescfe.numpairs"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "org.cescfe.numpairs"
        minSdk = 26
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    androidResources {
        generateLocaleConfig = true
    }
    lint {
        warningsAsErrors = true

        // External release timing must not break an otherwise unrelated change.
        informational += setOf(
            "AndroidGradlePluginVersion",
            "NewerVersionAvailable"
        )
    }
}

dependencies {
    // Keep Compose library versions aligned across the app and its instrumented tests.
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Android platform and lifecycle integration
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.compose)

    // Material Design 3 and Compose UI
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)

    // Android Studio Preview support
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Local persistence
    implementation(libs.androidx.datastore.preferences)

    // Unit tests
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    // Instrumented Android and Compose UI tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
