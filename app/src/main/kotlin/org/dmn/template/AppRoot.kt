package org.dmn.template

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import org.dmn.template.rider.RIDER_LOGGED_IN
import org.dmn.template.rider.RIDER_PREFS
import org.dmn.template.rider.RiderNav

private const val MODE_RIDER = "rider"
private const val MODE_DRIVER = "driver"

/**
 * Pick Up is one app with two modes. The rider side is the default, and the app reopens in
 * whichever mode was used last. Logging in once covers both modes, and logging out does too.
 */
@Composable
fun AppRoot() {
    val context = LocalContext.current
    val appPrefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    val driverPrefs = remember { context.getSharedPreferences("pickup_prefs", Context.MODE_PRIVATE) }
    val riderPrefs = remember { context.getSharedPreferences(RIDER_PREFS, Context.MODE_PRIVATE) }
    var mode by remember { mutableStateOf(appPrefs.getString("mode", MODE_RIDER) ?: MODE_RIDER) }

    fun switchTo(newMode: String) {
        if (newMode == MODE_DRIVER) {
            // Already signed in as a rider: skip the driver's phone screens and go to sign-up.
            val riderIn = riderPrefs.getBoolean(RIDER_LOGGED_IN, false)
            if (riderIn && driverPrefs.getInt("stage", 0) == 0) {
                driverPrefs.edit().putInt("stage", 1).apply()
            }
        } else {
            // Already signed in as a driver: no second login as a rider.
            if (driverPrefs.getInt("stage", 0) >= 1) {
                riderPrefs.edit().putBoolean(RIDER_LOGGED_IN, true).apply()
            }
        }
        appPrefs.edit().putString("mode", newMode).apply()
        mode = newMode
    }

    if (mode == MODE_DRIVER) {
        PickUpNav(
            onSwitchToRider = { switchTo(MODE_RIDER) },
            onLoggedOut = { riderPrefs.edit().putBoolean(RIDER_LOGGED_IN, false).apply() }
        )
    } else {
        RiderNav(
            onSwitchToDriver = { switchTo(MODE_DRIVER) },
            onLoggedOut = { driverPrefs.edit().putInt("stage", 0).apply() }
        )
    }
}
