package com.example.evcharging.network

import android.util.Log
import com.example.evcharging.session.OperatorSession
import com.example.evcharging.session.UserSession
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {
    private const val BASE_URL = "http://192.168.1.2:5128/"
    private const val TAG = "NetworkClient"

    // Decide auth based on PATH
    private fun needsAuth(path: String, method: String): Boolean {
        val p = path.lowercase()

        // POST /api/bookings/start-by-qr
        if (method.equals("POST", true) && p == "/api/bookings/start-by-qr") return true

        // POST /api/bookings/{id}/approve
        if (method.equals("POST", true) &&
            p.startsWith("/api/bookings/") && p.endsWith("/approve")
        ) return true

        // POST /api/bookings/{id}/finalize
        if (method.equals("POST", true) &&
            p.startsWith("/api/bookings/") && p.endsWith("/finalize")
        ) return true

        return false
    }

    private val authInterceptor = Interceptor { chain ->
        val req = chain.request()
        val url = req.url
        val path = url.encodedPath
        val method = req.method

        val builder = req.newBuilder()
            .header("Accept", "application/json")

        if (needsAuth(path, method)) {
            val token = OperatorSession.getJwt()
            if (!token.isNullOrBlank()) {
                builder.header("Authorization", "Bearer $token")
                val shortToken = if (token.length > 20)
                    "${token.take(10)}...${token.takeLast(10)}" else token
                Log.d(TAG, "🔐 $method $path → Bearer $shortToken")
            } else {
                Log.w(TAG, "⚠️ $method $path requires auth, but no token found.")
            }
        } else {
            Log.d(TAG, "ⓘ $method $path (no auth)")
        }

        chain.proceed(builder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d(TAG, message)
    }.apply { level = HttpLoggingInterceptor.Level.BODY }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)        // add BEFORE body logger
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
