package com.example.evcharging.network

import com.example.evcharging.network.models.ApiResponse
import com.example.evcharging.network.models.RegistrationRequest
import com.example.evcharging.network.models.RegistrationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    
    @POST("api/owners/register")
    suspend fun registerEVOwner(
        @Body request: RegistrationRequest
    ): Response<ApiResponse<RegistrationResponse>>
}
