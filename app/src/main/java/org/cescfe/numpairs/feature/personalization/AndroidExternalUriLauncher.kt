package org.cescfe.numpairs.feature.personalization

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

sealed interface ExternalUriLaunchResult {
    data object Launched : ExternalUriLaunchResult

    data object Unavailable : ExternalUriLaunchResult
}

fun interface ExternalUriLauncher {
    fun launch(uri: String): ExternalUriLaunchResult
}

class AndroidExternalUriLauncher(private val context: Context) : ExternalUriLauncher {
    override fun launch(uri: String): ExternalUriLaunchResult = try {
        val intent = ExternalUriIntentFactory.create(uri).apply {
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(intent)
        ExternalUriLaunchResult.Launched
    } catch (_: ActivityNotFoundException) {
        ExternalUriLaunchResult.Unavailable
    }
}

object ExternalUriIntentFactory {
    fun create(uri: String): Intent = Intent(
        Intent.ACTION_VIEW,
        uri.toUri()
    )
}

const val PRIVACY_POLICY_URL = "https://francescfe.github.io/numpairs/privacy-policy/"
const val OPEN_SOURCE_REPOSITORY_URL = "https://github.com/CescFe/numpairs"
