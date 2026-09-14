package com.starter.app.core.ui.permission

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

/** True when every one of [permissions] is already granted. */
fun Context.hasPermissions(permissions: Collection<String>): Boolean =
    permissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }

/**
 * Remembers a runtime-permission requester for [permissions].
 *
 * Call [PermissionRequester.launch] from the exact spot where the feature needs the permission
 * (a button click, a `LaunchedEffect`, …) so the system prompt appears in context rather than
 * up-front. [onResult] receives the per-permission grant map; permissions that are already
 * granted resolve immediately without showing a prompt.
 *
 * Pass a stable [permissions] list (e.g. a top-level `val`) so the requester isn't rebuilt on
 * every recomposition.
 */
@Composable
fun rememberPermissionRequester(
    permissions: List<String>,
    onResult: (Map<String, Boolean>) -> Unit = {},
): PermissionRequester {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { onResult(it) }
    return remember(permissions, launcher) {
        PermissionRequester(permissions.toTypedArray(), launcher)
    }
}

/** Handle returned by [rememberPermissionRequester]; call [launch] to trigger the request. */
@Stable
class PermissionRequester internal constructor(
    private val permissions: Array<String>,
    private val launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
) {
    fun launch() = launcher.launch(permissions)
}
