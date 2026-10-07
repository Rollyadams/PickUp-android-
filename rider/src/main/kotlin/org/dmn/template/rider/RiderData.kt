package org.dmn.template.rider

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.dmn.template.roundTo100
import kotlin.math.abs

const val CASH = "Cash"
const val BANK = "Bank Transfer"

/** A landmark the rider can pick. [km] is a demo position, only used to fake distances. */
data class Place(val name: String, val area: String, val km: Double)

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

/** Everything the rider has chosen for the ride in progress. */
class RideDraft {
    var pickup by mutableStateOf(RiderDemo.places[0])
    var dropoff by mutableStateOf<Place?>(null)
    var fare by mutableIntStateOf(0)
    var payment by mutableStateOf(CASH)
    var childSeat by mutableStateOf(false)
    var extraPassengers by mutableStateOf(false)
    var comment by mutableStateOf("")
    var driver by mutableStateOf<DriverOffer?>(null)
    var rating by mutableIntStateOf(0)

    val km: Double
        get() = dropoff?.let { RiderDemo.distanceKm(pickup, it) } ?: 0.0

    /** Clears the trip but keeps the rider's chosen payment method. */
    fun reset() {
        dropoff = null
        fare = 0
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
        Place("Yaba Tech Gate", "Yaba", 0.0),
        Place("Surulere Shoprite", "Surulere", 4.5),
        Place("Ojuelegba Bus Stop", "Surulere", 3.0),
        Place("Allen Avenue", "Ikeja", 9.4),
        Place("Ikeja City Mall", "Alausa, Ikeja", 12.0),
        Place("Murtala Muhammed Airport", "Ikeja", 15.5),
        Place("National Theatre", "Iganmu", 8.0),
        Place("Lekki Phase 1 Gate", "Lekki", 22.0)
    )

    fun distanceKm(a: Place, b: Place): Double = maxOf(2.0, abs(a.km - b.km))

    // PLACEHOLDER fare rules until real pricing is decided.
    fun recommendedFare(km: Double): Int = roundTo100(500 + (km * 420).toInt())
    fun minFare(recommended: Int): Int = roundTo100(recommended * 80 / 100)
    fun maxFare(recommended: Int): Int = roundTo100(recommended * 150 / 100)
    fun quickFare(recommended: Int): Int = roundTo100(recommended * 115 / 100)

    fun offersFor(fare: Int): List<DriverOffer> = listOf(
        DriverOffer("Emeka", 4.92, 214, "Toyota Corolla, grey", "LND-412-XA", 3, fare),
        DriverOffer("Sade", 4.85, 131, "Honda Accord, black", "KJA-209-GH", 5, roundTo100(fare * 108 / 100)),
        DriverOffer("Ibrahim", 4.78, 96, "Kia Rio, white", "ABC-317-FK", 7, roundTo100(fare * 115 / 100))
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
