package com.example.evcharging.repository

import android.util.Log
import com.example.evcharging.database.UserDatabaseHelper
import com.example.evcharging.network.NetworkClient
import com.example.evcharging.network.models.RegistrationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository {
    
    private val apiService = NetworkClient.apiService
    
    suspend fun testNetworkConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d("UserRepository", "Testing network connection...")
            val request = RegistrationRequest(
                nic = "test",
                fullName = "test",
                email = "test@test.com",
                phone = "1234567890"
            )
            
            val response = apiService.registerEVOwner(request)
            Log.d("UserRepository", "Network test response code: ${response.code()}")
            
            if (response.isSuccessful) {
                Result.success("Network connection successful")
            } else {
                Result.failure(Exception("Network connection failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Network test failed: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun registerEVOwner(
        nic: String,
        fullName: String,
        email: String,
        phone: String,
        password: String,
        dbHelper: UserDatabaseHelper
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d("UserRepository", "Starting registration for NIC: $nic")
            
            // Test database connection
            val connectionTest = dbHelper.testDatabaseConnection()
            Log.d("UserRepository", "Database connection test: $connectionTest")
            
            if (!connectionTest) {
                Log.e("UserRepository", "Database connection test failed")
                return@withContext Result.failure(Exception("Database connection failed"))
            }
            
            // Check database info
            val dbInfo = dbHelper.getDatabaseInfo()
            Log.d("UserRepository", "Database info: $dbInfo")
            
            // Check database file
            val fileInfo = dbHelper.checkDatabaseFile()
            Log.d("UserRepository", "Database file info: $fileInfo")
            
            // Check database schema
            val schema = dbHelper.checkTableSchema()
            Log.d("UserRepository", "Database schema: $schema")
            
            // First, save to SQLite database
            var sqliteSuccess = dbHelper.createEVOwner(nic, fullName, email, phone, password)
            Log.d("UserRepository", "SQLite save result: $sqliteSuccess")
            
            // If SQLite save fails, try to recreate database and retry
            if (!sqliteSuccess) {
                Log.e("UserRepository", "First SQLite save failed, attempting to recreate database...")
                val recreateSuccess = dbHelper.forceRecreateDatabase()
                if (recreateSuccess) {
                    Log.d("UserRepository", "Database recreated, retrying save...")
                    sqliteSuccess = dbHelper.createEVOwner(nic, fullName, email, phone, password)
                    Log.d("UserRepository", "Retry SQLite save result: $sqliteSuccess")
                }
                
                // If still failing, try complete reset
                if (!sqliteSuccess) {
                    Log.e("UserRepository", "Retry failed, attempting complete database reset...")
                    val resetSuccess = dbHelper.resetDatabase()
                    if (resetSuccess) {
                        Log.d("UserRepository", "Database reset, retrying save...")
                        sqliteSuccess = dbHelper.createEVOwner(nic, fullName, email, phone, password)
                        Log.d("UserRepository", "Final retry SQLite save result: $sqliteSuccess")
                    }
                }
            }
            
            if (!sqliteSuccess) {
                Log.e("UserRepository", "Failed to save to SQLite database after retry")
                return@withContext Result.failure(Exception("Failed to save to local database. Please try again."))
            }
            
            // Then, send to backend API (optional - don't fail if this doesn't work)
            try {
                val request = RegistrationRequest(
                    nic = nic,
                    fullName = fullName,
                    email = email,
                    phone = phone
                )
                
                Log.d("UserRepository", "Sending API request: $request")
                val response = apiService.registerEVOwner(request)
                Log.d("UserRepository", "API response code: ${response.code()}")
                Log.d("UserRepository", "API response body: ${response.body()}")
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        Log.d("UserRepository", "Registration successful in both databases")
                        return@withContext Result.success("Registration successful! Data saved to both local and cloud database.")
                    } else {
                        Log.w("UserRepository", "Backend registration failed: ${apiResponse?.message}")
                        return@withContext Result.success("Registration successful! Data saved to local database. Backend sync failed: ${apiResponse?.message}")
                    }
                } else {
                    Log.w("UserRepository", "Backend API error: ${response.code()} - ${response.message()}")
                    return@withContext Result.success("Registration successful! Data saved to local database. Backend sync failed: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.w("UserRepository", "Backend API call failed: ${e.message}")
                return@withContext Result.success("Registration successful! Data saved to local database. Backend sync failed: ${e.message}")
            }
            
        } catch (e: Exception) {
            Log.e("UserRepository", "Exception during registration: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getBookingsByOwner(nic: String, skip: Int = 0, take: Int = 50): Result<List<com.example.evcharging.network.models.BookingResponse>> = withContext(Dispatchers.IO) {
        try {
            Log.d("UserRepository", "Fetching bookings for NIC: $nic")
            
            val response = apiService.getBookingsByOwner(nic, skip, take)
            Log.d("UserRepository", "Bookings API response code: ${response.code()}")
            Log.d("UserRepository", "Bookings API response body: ${response.body()}")
            
            if (response.isSuccessful) {
                val bookings = response.body()
                if (bookings != null) {
                    Log.d("UserRepository", "Successfully fetched ${bookings.size} bookings")
                    Result.success(bookings)
                } else {
                    Log.w("UserRepository", "Bookings API returned null response")
                    Result.success(emptyList())
                }
            } else {
                Log.e("UserRepository", "Bookings API error: ${response.code()} - ${response.message()}")
                Result.failure(Exception("API error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Exception during bookings fetch: ${e.message}", e)
            Log.e("UserRepository", "Exception type: ${e.javaClass.simpleName}")
            Result.failure(e)
        }
    }
    
    suspend fun getStationById(stationId: String): Result<com.example.evcharging.network.StationView> = withContext(Dispatchers.IO) {
        try {
            Log.d("UserRepository", "Fetching station details for ID: $stationId")
            
            val response = apiService.getStationById(stationId)
            Log.d("UserRepository", "Station API response code: ${response.code()}")
            Log.d("UserRepository", "Station API response body: ${response.body()}")
            
            if (response.isSuccessful) {
                val station = response.body()
                if (station != null) {
                    Log.d("UserRepository", "Successfully fetched station: ${station.name}")
                    Result.success(station)
                } else {
                    Log.w("UserRepository", "Station API returned null response")
                    Result.failure(Exception("Station not found"))
                }
            } else {
                Log.e("UserRepository", "Station API error: ${response.code()} - ${response.message()}")
                Result.failure(Exception("API error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Exception during station fetch: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getAllStations(): Result<List<com.example.evcharging.network.StationView>> = withContext(Dispatchers.IO) {
        try {
            Log.d("UserRepository", "Fetching all stations")
            
            val response = apiService.getAllStations()
            Log.d("UserRepository", "Stations API response code: ${response.code()}")
            Log.d("UserRepository", "Stations API response body: ${response.body()}")
            
            if (response.isSuccessful) {
                val stations = response.body()
                if (stations != null) {
                    Log.d("UserRepository", "Successfully fetched ${stations.size} stations")
                    Result.success(stations)
                } else {
                    Log.w("UserRepository", "Stations API returned null response")
                    Result.success(emptyList())
                }
            } else {
                Log.e("UserRepository", "Stations API error: ${response.code()} - ${response.message()}")
                Result.failure(Exception("API error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Exception during stations fetch: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun cancelBooking(bookingId: String, ownerNic: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d("UserRepository", "Cancelling booking: $bookingId for owner: $ownerNic")
            
            val response = apiService.cancelBooking(bookingId, ownerNic)
            Log.d("UserRepository", "Cancel booking API response code: ${response.code()}")
            Log.d("UserRepository", "Cancel booking API response body: ${response.body()}")
            
            if (response.isSuccessful) {
                val result = response.body()
                if (result != null) {
                    Log.d("UserRepository", "Successfully cancelled booking")
                    Result.success("Booking cancelled successfully")
                } else {
                    Log.w("UserRepository", "Cancel booking API returned null response")
                    Result.success("Booking cancelled successfully")
                }
            } else {
                Log.e("UserRepository", "Cancel booking API error: ${response.code()} - ${response.message()}")
                Result.failure(Exception("API error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Exception during booking cancellation: ${e.message}", e)
            Result.failure(e)
        }
    }
}
