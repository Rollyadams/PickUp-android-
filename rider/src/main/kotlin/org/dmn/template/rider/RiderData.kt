package org.dmn.template.rider

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.dmn.template.roundTo100

/** Where the rider's login is remembered. The app root reads these to keep both modes in step. */
const val RIDER_PREFS = "rider_prefs"
const val RIDER_LOGGED_IN = "logged_in"

const val CASH = "Cash"
const val BANK = "Bank Transfer"

/** A rider can add stops after the destination, up to this many in total. */
const val MAX_STOPS = 3

/** Notifications shown as unread in the menu (demo). */
const val RIDER_UNREAD = 2

// PLACEHOLDER: lowest fare Pick Up will ever accept for any trip.
const val MIN_FARE_FLOOR = 1000

data class Place(val name: String, val area: String, val lat: Double, val lng: Double)

data class RideType(
    val id: Int,
    val name: String,
    val glyph: String,
    val note: String,
    val etaMinutes: Int
)

data class DriverOffer(
    val name: String,
    val rating: Double,
    val trips: Int,
    val car: String,
    val plate: String,
    val etaMinutes: Int,
    val fare: Int
)

data class PastTrip(
    val date: String,
    val daysAgo: Int,
    val pickup: String,
    val dropoff: String,
    val fare: Int,
    val payment: String,
    val driver: String
)

/** Straight-line distance stretched by 35% to guess the road distance, until real routing is added. */
fun roadKm(a: Place, b: Place): Double {
    val out = FloatArray(1)
    Location.distanceBetween(a.lat, a.lng, b.lat, b.lng, out)
    return maxOf(1.0, out[0] / 1000.0 * 1.35)
}

/** Everything the rider has chosen for the ride in progress. */
class RideDraft {
    var pickup by mutableStateOf<Place?>(null)

    /** True when the rider picked the pickup by hand instead of using the detected location. */
    var pickupIsManual by mutableStateOf(false)

    /** Destination first, then any extra stops. */
    val stops = mutableStateListOf<Place>()

    /** What the search screen is editing: 0 = pickup, 1 = a stop. */
    var searchTarget by mutableIntStateOf(1)
    var searchStopIndex by mutableIntStateOf(0)

    var rideType by mutableIntStateOf(0)
    var autoAccept by mutableStateOf(false)
    var fare by mutableIntStateOf(0)
    var payment by mutableStateOf(CASH)
    var childSeat by mutableStateOf(false)
    var extraPassengers by mutableStateOf(false)
    var comment by mutableStateOf("")
    var driver by mutableStateOf<DriverOffer?>(null)
    var rating by mutableIntStateOf(0)

    val destination: Place?
        get() = stops.lastOrNull()

    /** Distance from the pickup through every stop. */
    val km: Double
        get() {
            var from = pickup ?: return 0.0
            var total = 0.0
            for (stop in stops) {
                total += roadKm(from, stop)
                from = stop
            }
            return total
        }

    /** Clears the trip but keeps the pickup, ride type and payment method. */
    fun reset() {
        stops.clear()
        fare = 0
        autoAccept = false
        childSeat = false
        extraPassengers = false
        comment = ""
        driver = null
        rating = 0
    }
}

/** Demo data until the real backend exists. */
object RiderDemo {
    val places = listOf(
        Place("Yaba Tech Gate", "Yaba", 6.5170, 3.3715),
        Place("Ojuelegba Bus Stop", "Surulere", 6.5064, 3.3667),
        Place("Surulere Shoprite", "Surulere", 6.4989, 3.3563),
        Place("Allen Avenue", "Ikeja", 6.6018, 3.3515),
        Place("Ikeja City Mall", "Alausa, Ikeja", 6.6130, 3.3553),
        Place("Murtala Muhammed Airport", "Ikeja", 6.5774, 3.3212),
        Place("National Theatre", "Iganmu", 6.4772, 3.3656),
        Place("Lekki Phase 1 Gate", "Lekki", 6.4474, 3.4700)
    )

    val rideTypes = listOf(
        RideType(0, "Ride", "🚗", "Everyday fares", 4),
        RideType(1, "Comfort", "🚙", "Newer cars", 6),
        RideType(2, "Quick Accept", "⚡", "In a hurry? Skip the offers", 3)
    )

    // PLACEHOLDER fare rules until real pricing is decided. The rider cannot go below the minimum.
    private fun algorithmFare(km: Double): Int = roundTo100(500 + (km * 420).toInt())

    fun recommendedFare(km: Double): Int = maxOf(MIN_FARE_FLOOR, algorithmFare(km))

    fun minimumFare(km: Double): Int =
        maxOf(MIN_FARE_FLOOR, roundTo100(algorithmFare(km) * 85 / 100))

    /** Price for each ride type. Never below the recommended fare, so never below the minimum. */
    fun fareFor(typeId: Int, recommended: Int): Int = when (typeId) {
        1 -> roundTo100(recommended * 130 / 100)
        2 -> roundTo100(recommended * 115 / 100)
        else -> recommended
    }

    fun offersFor(fare: Int): List<DriverOffer> = listOf(
        DriverOffer("Emeka", 4.92, 214, "Toyota Corolla", "LND-412-XA", 8, fare),
        DriverOffer("Sade", 4.85, 131, "Honda Accord", "KJA-209-GH", 12, roundTo100(fare * 108 / 100)),
        DriverOffer("Ibrahim", 4.78, 96, "Kia Rio", "ABC-317-FK", 17, roundTo100(fare * 115 / 100))
    )

    val trips = listOf(
        PastTrip("Today", 0, "Yaba Tech Gate", "Surulere Shoprite", 3000, CASH, "Dayo"),
        PastTrip("Yesterday", 1, "Allen Avenue", "Ikeja City Mall", 2200, BANK, "Emeka"),
        PastTrip("3 days ago", 3, "Ojuelegba Bus Stop", "National Theatre", 2600, CASH, "Sade"),
        PastTrip("6 days ago", 6, "Yaba Tech Gate", "Allen Avenue", 5200, BANK, "Tunde"),
        PastTrip("12 days ago", 12, "Surulere Shoprite", "Yaba Tech Gate", 3000, CASH, "Ibrahim"),
        PastTrip("20 days ago", 20, "Ikeja City Mall", "Murtala Muhammed Airport", 3400, CASH, "Emeka"),
        PastTrip("35 days ago", 35, "Lekki Phase 1 Gate", "National Theatre", 7800, BANK, "Sade"),
        PastTrip("50 days ago", 50, "Yaba Tech Gate", "Ojuelegba Bus Stop", 2000, CASH, "Dayo")
    )
}
