package com.example.evcharging.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Matches your backend BookingView projection
data class BookingView(
    val id: String,
    val ownerNic: String,
    val stationId: String,
    val status: String,       // "Pending", "Approved", "InProgress", "Completed", "Cancelled", "Rejected"
    val startUtc: String,
    val endUtc: String,
    val qrToken: String?
)

interface BookingsApi {
    @GET("api/bookings/owner/{nic}")
    fun listByOwner(
        @Path("nic") nic: String,
        @Query("skip") skip: Int = 0,
        @Query("take") take: Int = 100
    ): Call<List<BookingView>>
}