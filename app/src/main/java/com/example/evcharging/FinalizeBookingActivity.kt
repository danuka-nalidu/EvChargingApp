package com.example.evcharging

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evcharging.network.FinalizeBookingRequest
import com.example.evcharging.network.NetworkClient
import com.example.evcharging.ui.theme.EvChargingTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FinalizeBookingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val bookingId = intent.getStringExtra("BOOKING_ID") ?: ""

        setContent {
            EvChargingTheme {
                FinalizeBookingScreen(
                    bookingId = bookingId,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@Composable
fun FinalizeBookingScreen(
    bookingId: String,
    onBackClick: () -> Unit
) {
    var kwhText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var successMsg by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Finalize Booking",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Booking Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                FinalizeDetailRow("Booking ID", bookingId.ifBlank { "N/A" })
            }
        }

        OutlinedTextField(
            value = kwhText,
            onValueChange = { kwhText = it },
            label = { Text("Energy Used (kWh)") },
            placeholder = { Text("e.g., 42.5") },
            singleLine = true,
            enabled = !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        if (!errorMsg.isNullOrBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Text(
                    text = errorMsg!!,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        if (!successMsg.isNullOrBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
            ) {
                Text(
                    text = successMsg!!,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Button(
            onClick = {
                errorMsg = null
                successMsg = null

                if (bookingId.isBlank()) {
                    errorMsg = "Missing booking id."
                    return@Button
                }
                val kwh = kwhText.toDoubleOrNull()
                if (kwh == null || kwh < 0) {
                    errorMsg = "Please enter a valid kWh value (e.g., 45.0)."
                    return@Button
                }

                isSubmitting = true
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val resp = NetworkClient.apiService.finalizeBooking(
                            bookingId,
                            FinalizeBookingRequest(kWhDelivered = kwh)
                        )
                        if (resp.isSuccessful) {
                            successMsg = "✅ Session finalized successfully."
                        } else {
                            errorMsg = "Failed: HTTP ${resp.code()}"
                        }
                    } catch (e: Exception) {
                        errorMsg = e.message ?: "Unexpected error"
                    } finally {
                        isSubmitting = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            enabled = !isSubmitting
        ) {
            Text(
                text = if (isSubmitting) "Finalizing..." else "Finalize Operation",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        TextButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Back to Dashboard",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun FinalizeDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Medium)
    }
}
