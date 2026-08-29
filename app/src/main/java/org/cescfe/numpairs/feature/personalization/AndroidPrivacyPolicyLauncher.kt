package org.cescfe.numpairs.feature.personalization

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

sealed interface PrivacyPolicyLaunchResult {
    data object Launched : PrivacyPolicyLaunchResult

    data object Unavailable : PrivacyPolicyLaunchResult
}

fun interface PrivacyPolicyLauncher {
    fun launch(): PrivacyPolicyLaunchResult
}

class AndroidPrivacyPolicyLauncher(private val context: Context) : PrivacyPolicyLauncher {
    override fun launch(): PrivacyPolicyLaunchResult = try {
        val intent = PrivacyPolicyIntentFactory.create().apply {
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(intent)
        PrivacyPolicyLaunchResult.Launched
    } catch (_: ActivityNotFoundException) {
        PrivacyPolicyLaunchResult.Unavailable
    }
}

object PrivacyPolicyIntentFactory {
    fun create(): Intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(PRIVACY_POLICY_URL)
    )
}

const val PRIVACY_POLICY_URL = "https://francescfe.github.io/numpairs/privacy-policy/"
