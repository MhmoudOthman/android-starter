package com.starter.app.core.ui.component

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.starter.app.core.designsystem.R
import com.starter.app.core.ui.component.dialogs.ErrorDialog

/**
 * Wraps [content] and enforces that GPS is on. The overlay is only shown while [enabled] is true;
 * callers keep it false until the app is past any screen that shows its own blocking dialogs (e.g.
 * the splash update/error dialogs), so the GPS dialog is always the last window and stays on top.
 */
@Composable
fun RequireLocationEnabled(
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isGpsEnabled by remember { mutableStateOf(isGpsProviderEnabled(context)) }

    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) {
        isGpsEnabled = isGpsProviderEnabled(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isGpsEnabled = isGpsProviderEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                isGpsEnabled = isGpsProviderEnabled(context)
            }
        }
        context.registerReceiver(receiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
        onDispose { context.unregisterReceiver(receiver) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        if (enabled && !isGpsEnabled) {
            GpsRequiredOverlay(
                onOpenSettings = { requestEnableLocation(context, settingsLauncher) },
            )
        }
    }
}

private fun requestEnableLocation(
    context: Context,
    launcher: ActivityResultLauncher<IntentSenderRequest>,
) {
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
        .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
        .setWaitForAccurateLocation(true)
        .build()
    val settingsRequest = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)
        .setAlwaysShow(true)
        .setNeedBle(false)
        .build()

    LocationServices.getSettingsClient(context)
        .checkLocationSettings(settingsRequest)
        .addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    launcher.launch(
                        IntentSenderRequest.Builder(exception.resolution.intentSender).build()
                    )
                } catch (_: IntentSender.SendIntentException) {
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            } else {
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }
}

private fun isGpsProviderEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        ?: return false
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

@Composable
private fun GpsRequiredOverlay(onOpenSettings: () -> Unit) {
    ErrorDialog(
        title = stringResource(id = R.string.enable_gps),
        message = stringResource(id = R.string.permission_rationale_location),
        positiveText = stringResource(id = R.string.enable),
        onDismissRequest = {},
        onPositiveClick = onOpenSettings,
        dismissOnBackPress = false,
        dismissOnClickOutside = false,
    )
}

/*
@Composable
private fun GpsRequiredOverlay(onOpenSettings: () -> Unit) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center,
        ) {
            ElevatedCard(modifier = Modifier
                .wrapContentSize()
                .padding(24.sdp),
                shape = MaterialTheme.shapes.large) {
                Column(
                    modifier = Modifier.padding(12.sdp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.sdp),
                ) {
                    Text(
                        text = stringResource(id = R.string.enable_gps),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(id = R.string.permission_rationale_location),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ElevatedSecondaryButton(
                        text = stringResource(id = R.string.enable),
                        onClick = onOpenSettings,
                    )
                }
            }
        }
    }
}*/
