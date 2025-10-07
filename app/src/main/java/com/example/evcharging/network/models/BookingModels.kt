package com.example.evcharging.network.models

import com.google.gson.annotations.SerializedName

data class BookingResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("ownerNic")
    val ownerNic: String,
    
    @SerializedName("ownerName")
    val ownerName: String,
    
    @SerializedName("stationId")
    val stationId: String,
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("startUtc")
    val startUtc: String,
    
    @SerializedName("endUtc")
    val endUtc: String,
    
    @SerializedName("qrToken")
    val qrToken: String? = null
) {
    // Helper methods to format data for display
    fun getFormattedDate(): String {
        return try {
            // Simple date formatting - just return the date part
            if (startUtc.length >= 10) {
                val datePart = startUtc.substring(0, 10)
                val parts = datePart.split("-")
                if (parts.size == 3) {
                    "${parts[2]}/${parts[1]}/${parts[0]}"
                } else {
                    datePart
                }
            } else {
                startUtc
            }
        } catch (e: Exception) {
            startUtc
        }
    }
    
    fun getFormattedTime(): String {
        return try {
            // Simple time formatting - extract time from ISO string
            if (startUtc.length >= 16) {
                val startTime = startUtc.substring(11, 16)
                val endTime = if (endUtc.length >= 16) endUtc.substring(11, 16) else "00:00"
                "$startTime - $endTime"
            } else {
                "Time unavailable"
            }
        } catch (e: Exception) {
            "Time unavailable"
        }
    }
    
    fun getStationDisplayName(): String {
        return "Station $stationId" // You can enhance this with actual station names if available
    }
    
    fun isUpcoming(): Boolean {
        return status in listOf("PENDING", "APPROVED")
    }
}
