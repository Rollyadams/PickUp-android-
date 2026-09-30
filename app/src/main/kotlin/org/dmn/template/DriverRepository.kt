package org.dmn.template

data class RideRequest(
    val id: String,
    val riderName: String,
    val riderRating: Double,
    val riderReviews: Int,
    val fare: Int,
    val distanceKm: Double,
    val etaMinutes: Int,
    val pickup: String,
    val dropoff: String,
    val paymentMethod: String
)

data class IncomeSummary(
    val label: String,
    val fares: Int,
    val orders: Int,
    val km: Int,
    val daysWorked: Int,
    val goal: Int
)

data class PerformanceStats(
    val acceptance: Int,
    val completion: Int,
    val rating: Double,
    val previousRating: Double,
    val hoursOnline: Int
)

data class WalletEntry(val title: String, val note: String, val amount: Int)

data class PayoutAccount(val bank: String, val number: String, val name: String)

// PLACEHOLDER DATA. Later, real ride requests replace this object
// and the screens stay exactly as they are.
object DriverRepository {
    private val sample = listOf(
        RideRequest("r1", "Tunde", 4.79, 96, 5200, 9.4, 2,
            "Allen Avenue, Ikeja", "Yaba Tech Gate", "Bank Transfer"),
        RideRequest("r2", "Amaka", 4.80, 59, 6900, 21.5, 8,
            "Lekki Phase 1 Gate", "Ajah Roundabout", "Cash"),
        RideRequest("r3", "Bisi", 4.45, 67, 2500, 8.1, 4,
            "Bode Thomas Street, Surulere", "Ojuelegba Bus Stop", "Cash"),
        RideRequest("r4", "Chidi", 4.90, 210, 12500, 31.0, 6,
            "Ikorodu Garage", "Ojota Bus Stop", "Bank Transfer"),
        RideRequest("r5", "Dayo", 4.85, 33, 3000, 4.5, 3,
            "Yaba Tech Gate", "Surulere Shoprite", "Cash")
    )

    fun requests(): List<RideRequest> = sample

    fun request(id: String): RideRequest? = sample.firstOrNull { it.id == id }

    // ----- Income (0 = day, 1 = week, 2 = month) -----
    private val incomes = listOf(
        IncomeSummary("today", 24600, 7, 96, 1, 25000),
        IncomeSummary("this week", 138400, 41, 590, 6, 150000),
        IncomeSummary("this month", 561000, 168, 2390, 25, 600000)
    )

    fun income(period: Int): IncomeSummary = incomes[period]

    // ----- Performance (0 = 7 days, 1 = 30 days, 2 = 90 days) -----
    private val performances = listOf(
        PerformanceStats(84, 97, 4.85, 4.80, 41),
        PerformanceStats(82, 96, 4.82, 4.79, 168),
        PerformanceStats(79, 95, 4.79, 4.82, 471)
    )

    fun performance(window: Int): PerformanceStats = performances[window]

    // ----- Wallet -----
    fun walletBalance(): Int = 6200

    fun walletEntries(): List<WalletEntry> = listOf(
        WalletEntry("VAT collected", "Today · 7 rides", -1845),
        WalletEntry("Daily fee", "Today", -1000),
        WalletEntry("Top up", "Bank transfer · Yesterday", 5000),
        WalletEntry("Daily fee", "Yesterday", -1000),
        WalletEntry("VAT collected", "Yesterday · 6 rides", -1620)
    )

    fun payoutAccount(): PayoutAccount = PayoutAccount("GTBank", "0123456789", "Your Name")
}
