package com.example.evcharging

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

class ViewBookingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EvChargingTheme {
                ViewBookingsScreen(
                    onBackClick = { finish() },
                    onQRCodeClick = { bookingId ->
                        val intent = Intent(this, QRCodeGenerationActivity::class.java).apply {
                            putExtra("BOOKING_ID", bookingId)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

data class Booking(
    val id: String,
    val stationName: String,
    val date: String,
    val time: String,
    val status: String,
    val isUpcoming: Boolean
)

@Composable
fun ViewBookingsScreen(
    onBackClick: () -> Unit,
    onQRCodeClick: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    // Sample data - in real app, this would come from a data source
    val upcomingBookings = listOf(
        Booking("1", "Station A - Colombo", "15/12/2024", "10:00 - 12:00", "Approved", true),
        Booking("2", "Station B - Kandy", "16/12/2024", "14:00 - 16:00", "Pending", true),
        Booking("3", "Station C - Galle", "17/12/2024", "08:00 - 10:00", "Approved", true)
    )
    
    val pastBookings = listOf(
        Booking("4", "Station A - Colombo", "10/12/2024", "10:00 - 12:00", "Completed", false),
        Booking("5", "Station B - Kandy", "08/12/2024", "14:00 - 16:00", "Completed", false),
        Booking("6", "Station D - Negombo", "05/12/2024", "16:00 - 18:00", "Cancelled", false)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Title
        Text(
            text = "My Bookings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )
        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Upcoming (${upcomingBookings.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Past (${pastBookings.size})") }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> {
                if (upcomingBookings.isEmpty()) {
                    EmptyStateMessage("No upcoming bookings")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(upcomingBookings) { booking ->
                            BookingCard(
                                booking = booking,
                                showQRButton = booking.status == "Approved",
                                onQRCodeClick = onQRCodeClick
                            )
                        }
                    }
                }
            }
            1 -> {
                if (pastBookings.isEmpty()) {
                    EmptyStateMessage("No past bookings")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(pastBookings) { booking ->
                            BookingCard(
                                booking = booking,
                                showQRButton = false,
                                onQRCodeClick = onQRCodeClick
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
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

@Composable
fun BookingCard(
    booking: Booking,
    showQRButton: Boolean,
    onQRCodeClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.stationName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                StatusChip(status = booking.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Date: ${booking.date}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Text(
                text = "Time: ${booking.time}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            if (showQRButton) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onQRCodeClick(booking.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text(
                        text = "Generate QR Code",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (backgroundColor, textColor) = when (status) {
        "Approved" -> Color(0xFF4CAF50) to Color.White
        "Pending" -> Color(0xFFFF9800) to Color.White
        "Completed" -> Color(0xFF2196F3) to Color.White
        "Cancelled" -> Color(0xFFF44336) to Color.White
        else -> Color.Gray to Color.White
    }
    
    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}
