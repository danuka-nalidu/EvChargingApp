package com.example.evcharging.network

import com.example.evcharging.network.models.ApiResponse
import com.example.evcharging.network.models.BookingResponse
import com.example.evcharging.network.models.RegistrationRequest
import com.example.evcharging.network.models.RegistrationResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


data class StationSchedule(
    val dayOfWeek: Int,
    val open: String,
    val close: String
)

data class StationView(
    val id: String,
    val name: String,
    val type: String,
    val parallelSlots: Int,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val isActive: Boolean,
    val schedule: List<StationSchedule>
)

data class CreateBookingRequest(
    val ownerNic: String,
    val stationId: String,
    val startUtc: String,
    val endUtc: String
)

data class CreateBookingResponse(
    val id: String
)
interface ApiService {
    
    @POST("api/owners/register")
    suspend fun registerEVOwner(
        @Body request: RegistrationRequest
    ): Response<ApiResponse<RegistrationResponse>>
    
    @GET("api/bookings/owner/{nic}")
    suspend fun getBookingsByOwner(
        @Path("nic") nic: String,
        @Query("skip") skip: Int = 0,
        @Query("take") take: Int = 50
    ): Response<List<BookingResponse>>


    @GET("api/stations/nearby")
    suspend fun nearby(                         // <-- keep suspend
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("km") km: Double = 5.0,
        @Query("take") take: Int = 50
    ): Response<List<StationView>>

    @POST("api/bookings")
    suspend fun createBooking(
        @Body req: CreateBookingRequest
    ): Response<CreateBookingResponse>

    @GET("api/stations/{id}")
    suspend fun getStationById(
        @Path("id") id: String
    ): Response<StationView>

    @GET("api/stations")
    suspend fun getAllStations(): Response<List<StationView>>

    @POST("api/bookings/{id}/cancel")
    suspend fun cancelBooking(
        @Path("id") id: String,
        @Query("ownerNic") ownerNic: String
    ): Response<ApiResponse<Any>>

    @PUT("api/bookings/{id}")
    suspend fun updateBooking(
        @Path("id") id: String,
        @Body request: UpdateBookingRequest
    ): Response<ApiResponse<Any>>

}

data class UpdateBookingRequest(
    val startUtc: String,
    val endUtc: String
)
