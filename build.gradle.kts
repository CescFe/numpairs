// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.spotless)
}

spotless {
    kotlin {
        target("app/src/**/*.kt")
        ktlint()
            .editorConfigOverride(
                mapOf(
                    "ktlint_code_style" to "android_studio",
                    "max_line_length" to "120",
                    "ktlint_standard_function-naming" to "disabled",
                    "ktlint_standard_no-unused-imports" to "enabled"
                )
            )
    }
    kotlinGradle {
        target("*.gradle.kts", "app/*.gradle.kts")
        ktlint()
            .editorConfigOverride(
                mapOf(
                    "ktlint_code_style" to "android_studio",
                    "max_line_length" to "120",
                    "ktlint_standard_no-unused-imports" to "enabled"
                )
            )
    }
}
