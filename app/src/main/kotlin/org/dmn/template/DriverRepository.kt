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
}
