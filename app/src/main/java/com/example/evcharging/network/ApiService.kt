package com.example.evcharging.network

import com.example.evcharging.network.models.ApiResponse
import com.example.evcharging.network.models.BookingResponse
import com.example.evcharging.network.models.RegistrationRequest
import com.example.evcharging.network.models.RegistrationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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
}
