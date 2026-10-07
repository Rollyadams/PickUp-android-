package org.dmn.template.rider

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

data class GeoPoint(val lat: Double, val lng: Double)

fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

/** The phone's current position, or null when permission or a GPS fix is missing. */
@SuppressLint("MissingPermission")
suspend fun currentPoint(context: Context): GeoPoint? {
    if (!hasLocationPermission(context)) return null
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    return try {
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { manager.isProviderEnabled(it) }
        val last = providers.mapNotNull { manager.getLastKnownLocation(it) }.maxByOrNull { it.time }
        if (last != null && System.currentTimeMillis() - last.time < 2 * 60 * 1000) {
            return GeoPoint(last.latitude, last.longitude)
        }
        val provider = providers.firstOrNull()
        val fresh: Location? = if (provider == null) {
            null
        } else {
            withTimeoutOrNull(8000) {
                suspendCancellableCoroutine<Location?> { continuation ->
                    val signal = CancellationSignal()
                    continuation.invokeOnCancellation { signal.cancel() }
                    manager.getCurrentLocation(
                        provider,
                        signal,
                        ContextCompat.getMainExecutor(context)
                    ) { location ->
                        if (continuation.isActive) continuation.resume(location)
                    }
                }
            }
        }
        (fresh ?: last)?.let { GeoPoint(it.latitude, it.longitude) }
    } catch (e: SecurityException) {
        null
    }
}

private fun Address.toPlace(): Place {
    val line = getAddressLine(0).orEmpty()
    val street = listOfNotNull(subThoroughfare, thoroughfare).joinToString(" ")
    val name = street.ifBlank { featureName ?: line.substringBefore(",") }
    val area = listOfNotNull(subLocality, locality).joinToString(", ").ifBlank { adminArea ?: "" }
    return Place(name, area, latitude, longitude)
}

/** Turns coordinates into a street address using the phone's own geocoder. */
@Suppress("DEPRECATION")
suspend fun addressFor(context: Context, point: GeoPoint): Place? = withContext(Dispatchers.IO) {
    try {
        if (!Geocoder.isPresent()) {
            null
        } else {
            Geocoder(context, Locale.getDefault())
                .getFromLocation(point.lat, point.lng, 1)
                ?.firstOrNull()
                ?.toPlace()
        }
    } catch (e: Exception) {
        null
    }
}

/** Searches for places by typed text, preferring results within about 50 km of [near]. */
@Suppress("DEPRECATION")
suspend fun searchPlaces(context: Context, query: String, near: GeoPoint?): List<Place> =
    withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) {
                emptyList()
            } else {
                val geocoder = Geocoder(context, Locale.getDefault())
                val found: List<Address>? = if (near != null) {
                    geocoder.getFromLocationName(
                        query,
                        6,
                        near.lat - 0.5,
                        near.lng - 0.5,
                        near.lat + 0.5,
                        near.lng + 0.5
                    )
                } else {
                    geocoder.getFromLocationName(query, 6)
                }
                found.orEmpty()
                    .filter { it.hasLatitude() && it.hasLongitude() }
                    .map { it.toPlace() }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
