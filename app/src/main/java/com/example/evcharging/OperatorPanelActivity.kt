package com.example.evcharging

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evcharging.ui.theme.EvChargingTheme

class OperatorPanelActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EvChargingTheme {
                OperatorPanelScreen(
                    onBackClick = { finish() },
                    onScanQRClick = { /* Handle QR scan */ },
                    onConfirmBookingClick = { /* Handle booking confirmation */ },
                    onFinalizeOperationClick = { /* Handle operation finalization */ }
                )
            }
        }
    }
}

@Composable
fun OperatorPanelScreen(
    onBackClick: () -> Unit,
    onScanQRClick: () -> Unit,
    onConfirmBookingClick: () -> Unit,
    onFinalizeOperationClick: () -> Unit
) {
    var bookingData by remember { mutableStateOf("") }
    var isBookingConfirmed by remember { mutableStateOf(false) }
    var showQRPlaceholder by remember { mutableStateOf(false) }

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
            text = "Operator Panel",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Scan QR Code Button
        Button(
            onClick = {
                onScanQRClick()
                // Simulate QR scan result
                bookingData = """
                    Booking ID: BK001
                    Customer: John Doe
                    NIC: 123456789V
                    Station: Station A - Colombo
                    Date: 15/12/2024
                    Time: 10:00 - 12:00
                    Status: Approved
                """.trimIndent()
                showQRPlaceholder = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50) // Green color
            )
        ) {
            Text(
                text = "Scan QR Code",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        // QR Scanner Placeholder
        if (showQRPlaceholder) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📱",
                            fontSize = 32.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "QR Scanner",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "(ZXing integration coming soon)",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
        
        // Booking Data Display
        if (bookingData.isNotEmpty()) {
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
                        text = "Retrieved Booking Data:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = bookingData,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        lineHeight = 20.sp
                    )
                }
            }
        }
        
        // Confirm Booking Button
        Button(
            onClick = {
                onConfirmBookingClick()
                isBookingConfirmed = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3) // Blue color
            ),
            enabled = bookingData.isNotEmpty() && !isBookingConfirmed
        ) {
            Text(
                text = if (isBookingConfirmed) "Booking Confirmed" else "Confirm Booking",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Finalize Operation Button
        Button(
            onClick = onFinalizeOperationClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9C27B0) // Purple color
            ),
            enabled = isBookingConfirmed
        ) {
            Text(
                text = "Finalize Operation",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Status Messages
        if (isBookingConfirmed) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E8)
                )
            ) {
                Text(
                    text = "✅ Booking confirmed successfully!",
                    fontSize = 14.sp,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(16.dp)
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
