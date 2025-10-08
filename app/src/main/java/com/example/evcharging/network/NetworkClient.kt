package com.example.evcharging.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {
    
    // Note: Change this to your actual IP address when testing on a physical device
    // For emulator: use "http://10.0.2.2:5128/"
    // For physical device: use "http://YOUR_COMPUTER_IP:5128/"
    // Make sure your .NET backend CORS allows this origin
//    private const val BASE_URL = "http://192.168.1.2:8044/"
    private const val BASE_URL = "http://10.0.2.2:5128/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
    val bookings: BookingsApi by lazy { retrofit.create(BookingsApi::class.java) }
}
