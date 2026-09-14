package com.starter.app.core.ui.permission

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Opens this app's own entry in system settings — the page with the Permissions list on it.
 *
 * It exists for one message: **"this needs precise location"**. That refusal is unlike every other
 * one in the app, because the driver cannot act on it from inside the app at all. Re-requesting the
 * permission does nothing once location has already been granted as *approximate* — the system does
 * not offer the precision toggle a second time — so a dialog with only an OK on it tells the driver
 * they are stuck without telling them where to go.
 *
 * Deliberately just a navigation. Granting the permission performs no write and resumes nothing:
 * the driver comes back and taps the control again, which is what keeps a settings round trip from
 * silently changing an order's status or a duty state behind them.
 *
 * `ACTION_APPLICATION_DETAILS_SETTINGS` is present on every handset this fleet runs, but a missing
 * activity would otherwise crash the app from an error dialog, so the launch is guarded.
 */
@Composable
fun rememberAppSettingsOpener(): () -> Unit {
    val context = LocalContext.current
    return remember(context) {
        {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            try {
                context.startActivity(intent)
            } catch (e: android.content.ActivityNotFoundException) {
                Log.e("AppSettings", "no activity handles the app-details settings intent", e)
            }
        }
    }
}
