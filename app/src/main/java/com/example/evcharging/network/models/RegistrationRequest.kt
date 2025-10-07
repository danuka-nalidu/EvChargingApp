package com.example.evcharging.network.models

import com.google.gson.annotations.SerializedName

data class RegistrationRequest(
    @SerializedName("nic")
    val nic: String,
    
    @SerializedName("fullName")
    val fullName: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("phone")
    val phone: String
)
