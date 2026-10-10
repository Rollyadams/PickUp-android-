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

data class Rider(
    val id: String,
    val name: String,
    val phone: String,
    val joined: String,
    val trips: Int,
    val rating: Double?,
    val payment: String,
    val phoneVerified: Boolean
)

/** One editable price or tax setting. [unit] is "₦" or "%". */
data class PriceSetting(
    val key: String,
    val group: String,
    val label: String,
    val unit: String,
    val value: Double,
    val min: Double,
    val max: Double,
    val hint: String
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

val builtSteps = setOf(1, 2, 3)

val pricingGroups = listOf("Drivers", "Fares", "Tax")

/** "₦1,000", "85%" or "7.5%". */
fun formatPrice(unit: String, value: Double): String {
    if (unit == "₦") return "₦" + "%,d".format(value.toInt())
    return if (value == value.toInt().toDouble()) "${value.toInt()}%" else "%.1f%%".format(value)
}

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

    val riders = listOf(
        Rider("r1", "Ada Nwosu", "0803 555 0111", "12 Sep 2026", 18, 4.9, "Cash", true),
        Rider("r2", "Bayo Salami", "0805 555 0122", "20 Sep 2026", 7, 4.7, "Bank Transfer", true),
        Rider("r3", "Chidi Okafor", "0807 555 0133", "28 Sep 2026", 2, 5.0, "Cash", true),
        Rider("r4", "Fatima Yusuf", "0810 555 0144", "2 Oct 2026", 0, null, "Cash", false),
        Rider("r5", "Grace Idowu", "0813 555 0155", "4 Oct 2026", 4, 4.5, "Bank Transfer", true),
        Rider("r6", "Hassan Musa", "0816 555 0166", "7 Oct 2026", 0, null, "Cash", false),
        Rider("r7", "Ife Adebayo", "0902 555 0177", "8 Oct 2026", 1, 5.0, "Cash", true)
    )

    fun rider(id: String): Rider? = riders.firstOrNull { it.id == id }

    // PLACEHOLDER values, the same ones the rider app uses until pricing is decided.
    val pricing = mutableStateListOf(
        PriceSetting("daily_fee", "Drivers", "Daily driver fee", "₦", 1000.0, 1.0, 100000.0,
            "Charged on days a driver works. Planned range ₦800 to ₦1,500, not locked."),
        PriceSetting("min_fare", "Fares", "Minimum fare", "₦", 1000.0, 100.0, 100000.0,
            "The lowest fare any trip can have."),
        PriceSetting("base_fare", "Fares", "Base fare", "₦", 500.0, 0.0, 100000.0,
            "Added to every trip before the per-km rate."),
        PriceSetting("per_km", "Fares", "Rate per km", "₦", 420.0, 1.0, 10000.0,
            "Multiplied by the trip distance."),
        PriceSetting("min_percent", "Fares", "Lowest price a rider can set", "%", 85.0, 1.0, 100.0,
            "As a share of the recommended fare. Riders can never go below it."),
        PriceSetting("comfort", "Fares", "Comfort price", "%", 130.0, 100.0, 500.0,
            "As a share of the recommended fare."),
        PriceSetting("quick", "Fares", "Quick Accept price", "%", 115.0, 100.0, 500.0,
            "As a share of the recommended fare."),
        PriceSetting("vat", "Tax", "VAT rate", "%", 7.5, 0.0, 100.0,
            "Placeholder. To be confirmed with an accountant.")
    )

    /** Price changes made this session, newest first. With a backend these are saved and logged. */
    val priceChanges = mutableStateListOf<String>()

    fun updatePrice(key: String, newValue: Double) {
        val index = pricing.indexOfFirst { it.key == key }
        if (index < 0) return
        val old = pricing[index]
        pricing[index] = old.copy(value = newValue)
        priceChanges.add(0, "${old.label}: ${formatPrice(old.unit, old.value)} → ${formatPrice(old.unit, newValue)}")
    }

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
