package org.dmn.template.admin

import androidx.compose.runtime.mutableStateListOf

data class DriverApplication(
    val id: String,
    val name: String,
    val phone: String,
    val car: String,
    val plate: String,
    val submitted: String,
    val ninOk: Boolean,
    val licenceOk: Boolean,
    val papersOk: Boolean
)

/** One icon on the dashboard. [step] is its number on the build list. */
data class Tile(val step: Int, val glyph: String, val label: String, val fullName: String)

/** The 12 areas, in build order. Only the ones in [builtSteps] open. */
val adminTiles = listOf(
    Tile(1, "🆔", "Drivers", "Driver verification"),
    Tile(2, "🧑", "Riders", "Rider verification"),
    Tile(3, "🏷️", "Pricing", "Pricing control"),
    Tile(4, "⚖️", "Disputes", "Dispute resolution"),
    Tile(5, "💰", "Finance", "Financial reporting"),
    Tile(6, "🛡️", "Safety", "Safety enforcement"),
    Tile(7, "💬", "Support", "Support line"),
    Tile(8, "🚨", "SOS alerts", "SOS alerts"),
    Tile(9, "🚗", "Trips", "Trips list"),
    Tile(10, "👥", "Staff", "Admin accounts and roles"),
    Tile(11, "🎁", "Referrals", "Referral monitoring"),
    Tile(12, "⭐", "Ratings", "Ratings and complaints")
)

val builtSteps = setOf(1)

val rejectionReasons = listOf(
    "Documents unclear",
    "NIN or licence did not match",
    "Vehicle papers missing",
    "Other"
)

/** Demo data until the real backend exists. */
object AdminRepository {
    val pending = mutableStateListOf(
        DriverApplication("a1", "Kunle Adeyemi", "0803 555 0142", "Toyota Corolla 2014", "LND-412-XA", "Today, 9:12", true, true, true),
        DriverApplication("a2", "Ngozi Eze", "0806 555 0177", "Honda Accord 2012", "KJA-209-GH", "Today, 8:40", true, true, false),
        DriverApplication("a3", "Musa Bello", "0809 555 0123", "Kia Rio 2016", "ABC-317-FK", "Yesterday", true, false, true),
        DriverApplication("a4", "Tolu Ajayi", "0812 555 0190", "Toyota Camry 2015", "EKY-581-AA", "Yesterday", true, true, true)
    )

    /** Decisions made this session, newest first. With a backend these are saved and logged. */
    val decisions = mutableStateListOf<String>()

    fun application(id: String): DriverApplication? = pending.firstOrNull { it.id == id }

    fun approve(app: DriverApplication) {
        pending.remove(app)
        decisions.add(0, "Approved: ${app.name}")
    }

    fun reject(app: DriverApplication, reason: String) {
        pending.remove(app)
        decisions.add(0, "Rejected: ${app.name} · $reason")
    }
}
