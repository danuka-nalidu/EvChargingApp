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
import com.example.evcharging.repository.UserRepository
import com.example.evcharging.session.UserSession
import com.example.evcharging.ui.theme.EvChargingTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewBookingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize user session
        UserSession.initialize(this)
        
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
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Fetch bookings from API
    LaunchedEffect(Unit) {
        val userInfo = UserSession.getUserInfo()
        if (userInfo != null) {
            val repository = UserRepository()
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val result = repository.getBookingsByOwner(userInfo.nic)
                    if (result.isSuccess) {
                        val apiBookings = result.getOrNull() ?: emptyList()
                        bookings = apiBookings.map { apiBooking ->
                            Booking(
                                id = apiBooking.id,
                                stationName = apiBooking.getStationDisplayName(),
                                date = apiBooking.getFormattedDate(),
                                time = apiBooking.getFormattedTime(),
                                status = apiBooking.status,
                                isUpcoming = apiBooking.isUpcoming()
                            )
                        }
                        errorMessage = ""
                    } else {
                        val exception = result.exceptionOrNull()
                        errorMessage = exception?.message ?: "Failed to load bookings"
                    }
                } catch (e: Exception) {
                    errorMessage = "Error loading bookings: ${e.message}"
                }
                isLoading = false
            }
        } else {
            errorMessage = "User not logged in"
            isLoading = false
        }
    }
    
    // Separate upcoming and past bookings
    val upcomingBookings = bookings.filter { it.isUpcoming }
    val pastBookings = bookings.filter { !it.isUpcoming }

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
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading bookings...",
                            color = Color.Gray
                        )
                    }
                }
            }
            errorMessage.isNotEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            selectedTab == 0 -> {
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
                                showQRButton = booking.status == "APPROVED",
                                onQRCodeClick = onQRCodeClick
                            )
                        }
                    }
                }
            }
            selectedTab == 1 -> {
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
