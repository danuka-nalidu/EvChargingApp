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

class CancelReservationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EvChargingTheme {
                CancelReservationScreen(
                    onBackClick = { finish() },
                    onCancelReservationClick = { /* Handle cancellation */ }
                )
            }
        }
    }
}

data class CancellableBooking(
    val id: String,
    val stationName: String,
    val date: String,
    val time: String,
    val status: String,
    val canCancel: Boolean
)

@Composable
fun CancelReservationScreen(
    onBackClick: () -> Unit,
    onCancelReservationClick: () -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    var selectedBooking by remember { mutableStateOf<CancellableBooking?>(null) }
    
    // Sample data - in real app, this would come from a data source
    val cancellableBookings = listOf(
        CancellableBooking("1", "Station A - Colombo", "15/12/2024", "10:00 - 12:00", "Approved", true),
        CancellableBooking("2", "Station B - Kandy", "16/12/2024", "14:00 - 16:00", "Pending", true),
        CancellableBooking("3", "Station C - Galle", "17/12/2024", "08:00 - 10:00", "Approved", false) // Less than 12 hours
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Title
        Text(
            text = "Cancel Reservation",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )
        
        // Cancellation Notice
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFEBEE)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = "⚠️ Cancellations allowed only if ≥12 hours before reservation time",
                fontSize = 14.sp,
                color = Color(0xFFD32F2F),
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Bookings List
        if (cancellableBookings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No cancellable bookings found",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cancellableBookings) { booking ->
                    CancellableBookingCard(
                        booking = booking,
                        onCancelClick = {
                            selectedBooking = booking
                            showCancelDialog = true
                        }
                    )
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
    
    // Cancel Confirmation Dialog
    if (showCancelDialog && selectedBooking != null) {
        AlertDialog(
            onDismissRequest = { 
                showCancelDialog = false
                selectedBooking = null
            },
            title = { Text("Cancel Reservation") },
            text = { 
                Text("Are you sure you want to cancel this reservation?\n\nStation: ${selectedBooking!!.stationName}\nDate: ${selectedBooking!!.date}\nTime: ${selectedBooking!!.time}\n\nThis action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCancelReservationClick()
                        showCancelDialog = false
                        selectedBooking = null
                    }
                ) {
                    Text("Yes, Cancel", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showCancelDialog = false
                    selectedBooking = null
                }) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun CancellableBookingCard(
    booking: CancellableBooking,
    onCancelClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (booking.canCancel) Color(0xFFF5F5F5) else Color(0xFFF0F0F0)
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
            
            if (booking.canCancel) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onCancelClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text(
                        text = "Cancel Reservation",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Cannot cancel - Less than 12 hours remaining",
                    fontSize = 12.sp,
                    color = Color.Red,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

