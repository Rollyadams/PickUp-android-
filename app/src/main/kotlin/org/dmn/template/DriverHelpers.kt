package org.dmn.template

// Driver-only rules. The shared colours, buttons and cards live in the :core module.

fun perKm(r: RideRequest): Int = (r.fare / r.distanceKm).toInt()

// PLACEHOLDERS until the real fee and tax rules are confirmed.
const val FLAT_FEE = 1000

/** 7.5% VAT on a fare (placeholder rule, to be confirmed with an accountant). */
fun vatOn(fare: Int): Int = (fare * 75 + 500) / 1000

/** "Fair fare" tag shows only on rides paying a high rate per km. */
fun isFairRate(r: RideRequest): Boolean = perKm(r) >= 500

/** The two counter prices a driver can ask for: 10% and 20% above the offer. */
fun counterOffers(fare: Int): List<Int> =
    listOf(110, 120).map { roundTo100(fare * it / 100) }.distinct().filter { it != fare }
