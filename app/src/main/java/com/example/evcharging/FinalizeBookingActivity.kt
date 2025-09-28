package com.example.evcharging

import android.content.Intent
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
import com.example.evcharging.ui.theme.EvChargingTheme

class FinalizeBookingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EvChargingTheme {
                FinalizeBookingScreen(
                    onBackClick = { finish() },
                    onFinalizeOperationClick = { /* Handle finalization */ }
                )
            }
        }
    }
}

@Composable
fun FinalizeBookingScreen(
    onBackClick: () -> Unit,
    onFinalizeOperationClick: () -> Unit
) {
    var isFinalized by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Title
        Text(
            text = "Finalize Booking",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Booking Details Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Booking Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                BookingDetailRow("Booking ID", "BK001")
                BookingDetailRow("Customer", "John Doe")
                BookingDetailRow("NIC", "123456789V")
                BookingDetailRow("Station", "Station A - Colombo")
                BookingDetailRow("Date", "15/12/2024")
                BookingDetailRow("Time", "10:00 - 12:00")
                BookingDetailRow("Status", "Confirmed")
                BookingDetailRow("Start Time", "10:05 AM")
                BookingDetailRow("End Time", "11:45 AM")
                BookingDetailRow("Duration", "1h 40m")
                BookingDetailRow("Energy Used", "45 kWh")
                BookingDetailRow("Cost", "Rs. 1,350")
            }
        }
        
        // Session Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3F2FD)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Session Summary",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "• Charging session completed successfully\n• Customer was present and verified\n• Payment processed\n• Station is ready for next customer",
                    fontSize = 14.sp,
                    color = Color(0xFF1976D2),
                    lineHeight = 20.sp
                )
            }
        }
        
        // Finalize Operation Button
        Button(
            onClick = {
                onFinalizeOperationClick()
                isFinalized = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50) // Green color
            ),
            enabled = !isFinalized
        ) {
            Text(
                text = if (isFinalized) "Operation Finalized" else "Finalize Operation",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Success Message
        if (isFinalized) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E8)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "✅ Operation finalized successfully!\n\nCharging session completed and recorded.",
                    fontSize = 14.sp,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(16.dp),
                    lineHeight = 20.sp
                )
            }
        }
        
        // Back Button
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

