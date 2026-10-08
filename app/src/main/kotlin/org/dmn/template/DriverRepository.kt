package org.dmn.template

import androidx.compose.runtime.mutableStateMapOf

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
    val paymentMethod: String,
    val startCode: String
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

data class TripRecord(
    val day: String,
    val pickup: String,
    val dropoff: String,
    val fare: Int,
    val payment: String,
    val status: String,
    val km: Double
)

data class NotificationItem(val title: String, val body: String, val time: String, val unread: Boolean)

// PLACEHOLDER DATA. Later, real ride requests replace this object
// and the screens stay exactly as they are.
object DriverRepository {
    private val sample = listOf(
        RideRequest("r1", "Tunde", 4.79, 96, 5200, 9.4, 2,
            "Allen Avenue, Ikeja", "Yaba Tech Gate", "Bank Transfer", "4821"),
        RideRequest("r2", "Amaka", 4.80, 59, 6900, 21.5, 8,
            "Lekki Phase 1 Gate", "Ajah Roundabout", "Cash", "7305"),
        RideRequest("r3", "Bisi", 4.45, 67, 2500, 8.1, 4,
            "Bode Thomas Street, Surulere", "Ojuelegba Bus Stop", "Cash", "1949"),
        RideRequest("r4", "Chidi", 4.90, 210, 12500, 31.0, 6,
            "Ikorodu Garage", "Ojota Bus Stop", "Bank Transfer", "6628"),
        RideRequest("r5", "Dayo", 4.85, 33, 3000, 4.5, 3,
            "Yaba Tech Gate", "Surulere Shoprite", "Cash", "3057")
    )

    fun requests(): List<RideRequest> = sample

    // Rides the driver swiped away. A hidden ride stays hidden until the rider changes the fare
    // or the destination. A rebooked ride is a new request, so it appears on its own.
    private val hidden = mutableStateMapOf<String, String>()

    private fun signature(r: RideRequest): String = "${r.fare}|${r.dropoff}"

    fun hide(r: RideRequest) {
        hidden[r.id] = signature(r)
    }

    fun isHidden(r: RideRequest): Boolean {
        val current = request(r.id) ?: r
        return hidden[r.id] == signature(current)
    }

    // A trip whose drop-off or fare changed mid-trip. Real data will live on the server.
    private val tripOverrides = mutableMapOf<String, RideRequest>()

    fun request(id: String): RideRequest? = tripOverrides[id] ?: sample.firstOrNull { it.id == id }

    fun updateTrip(updated: RideRequest) {
        tripOverrides[updated.id] = updated
    }

    fun clearTrip(id: String) {
        tripOverrides.remove(id)
    }

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

    // ----- Notifications -----
    private val notifications = listOf(
        NotificationItem("Insurance expires in 12 days", "Renew early so you can stay online.", "2h ago", true),
        NotificationItem("Your rating went up", "Riders rated you 4.85 over the last 7 days.", "5h ago", true),
        NotificationItem("Wallet top-up received", "₦5,000 was added to your wallet.", "Yesterday", true),
        NotificationItem("Safety tip", "Add an SOS contact so help can reach you faster.", "Yesterday", true),
        NotificationItem("Welcome to Pick Up", "Your account has been approved.", "3 days ago", false),
        NotificationItem("Daily fee reminder", "Your flat fee is taken from your wallet each day you drive.", "3 days ago", false)
    )

    fun notifications(): List<NotificationItem> = notifications

    fun unreadCount(): Int = notifications.count { it.unread }

    // ----- Trip history -----
    private val trips = listOf(
        TripRecord("Today", "Allen Avenue, Ikeja", "Yaba Tech Gate", 5200, "Bank Transfer", "Completed", 9.4),
        TripRecord("Today", "Lekki Phase 1 Gate", "Ajah Roundabout", 6900, "Cash", "Completed", 21.5),
        TripRecord("Today", "Bode Thomas Street, Surulere", "Ojuelegba Bus Stop", 0, "Cash", "Cancelled", 0.0),
        TripRecord("Yesterday", "Ikorodu Garage", "Ojota Bus Stop", 12500, "Bank Transfer", "Completed", 31.0),
        TripRecord("Yesterday", "Yaba Tech Gate", "Surulere Shoprite", 3000, "Cash", "Completed", 4.5)
    )

    fun trips(): List<TripRecord> = trips
}
