package org.dmn.template

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

fun hasForegroundLocation(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

/** True only when the phone's location permission is set to "Allow all the time". */
fun hasAlwaysLocation(context: Context): Boolean =
    hasForegroundLocation(context) &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

fun openAppSettings(context: Context) {
    try {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
        )
    } catch (e: ActivityNotFoundException) {
        // nothing else to try
    }
}

/** Live answer to "is location set to all the time?". Re-checks every second, so it updates when the driver returns from settings. */
@Composable
fun rememberAlwaysLocationGranted(): Boolean {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(hasAlwaysLocation(context)) }
    LaunchedEffect(Unit) {
        while (true) {
            granted = hasAlwaysLocation(context)
            delay(1000)
        }
    }
    return granted
}

/** Returns a function that asks for location, then for "all the time". */
@Composable
fun rememberLocationRequester(): () -> Unit {
    val context = LocalContext.current
    val background = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val foreground = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) background.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }
    return {
        if (!hasForegroundLocation(context)) {
            foreground.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        } else if (!hasAlwaysLocation(context)) {
            background.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }
}

/** Shown when a driver tries to go online without "Allow all the time". */
@Composable
fun LocationGateDialog(onAllow: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onDismiss(); onAllow() }) { Text("Continue", color = PuAmber) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Not now", color = PuMuted) }
        },
        title = { Text("Allow location all the time") },
        text = {
            Text(
                "To go online, Pick Up needs your location all the time. Riders find you, help can " +
                    "reach you fast, and every trip is recorded for safety. On the next screen, choose " +
                    "\"Allow all the time\"."
            )
        },
        containerColor = PuCard,
        titleContentColor = PuInk,
        textContentColor = PuMuted
    )
}
