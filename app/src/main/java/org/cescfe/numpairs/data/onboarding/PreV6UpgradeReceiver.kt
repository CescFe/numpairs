package org.cescfe.numpairs.data.onboarding

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class PreV6UpgradeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED &&
            context.currentVersionCode() == V6_APPLICATION_VERSION_CODE
        ) {
            context.preV6UpgradeMarker().mark()
        }
    }
}

internal fun Context.preV6UpgradeMarker(): PreV6UpgradeMarker = FilePreV6UpgradeMarker(
    markerFile = noBackupFilesDir.resolve(PRE_V6_UPGRADE_MARKER_FILE_NAME)
)

@Suppress("DEPRECATION")
private fun Context.currentVersionCode(): Long {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo.longVersionCode
    } else {
        packageInfo.versionCode.toLong()
    }
}

private const val V6_APPLICATION_VERSION_CODE = 2L
private const val PRE_V6_UPGRADE_MARKER_FILE_NAME = "pre_v6_upgrade"
