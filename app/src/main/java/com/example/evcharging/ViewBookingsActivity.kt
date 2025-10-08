package com.example.evcharging

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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

        setContent {
            EvChargingTheme {
                ViewBookingsScreen(
                    onBackClick = { finish() },
                    onQRCodeClick = { bookingId ->
                        val intent = Intent(this, QRCodeGenerationActivity::class.java).apply {
                            putExtra("BOOKING_QR", bookingId)
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
    val isUpcoming: Boolean,
    val qrToken: String?
)

@Composable
fun ViewBookingsScreen(
    onBackClick: () -> Unit,
    onQRCodeClick: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    // 0=All, 1=Approved, 2=Pending, 3=Completed, 4=Canceled, 5=Rejected
    var selectedTab by remember { mutableStateOf(0) }
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }
    var showBookingDetail by remember { mutableStateOf(false) }

    // Fetch bookings from API (keeps your existing repository flow)
    LaunchedEffect(Unit) {
        val userInfo = UserSession.getUserInfo()
        if (userInfo != null) {
            val repository = UserRepository()
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val result = repository.getBookingsByOwner(userInfo.nic)
                    if (result.isSuccess) {
                        val apiBookings = result.getOrNull() ?: emptyList()
                        
                        // Fetch station names for each booking
                        val bookingsWithStationNames = mutableListOf<Booking>()
                        for (apiBooking in apiBookings) {
                            val stationResult = repository.getStationById(apiBooking.stationId)
                            val stationName = if (stationResult.isSuccess) {
                                stationResult.getOrNull()?.name ?: "Station ${apiBooking.stationId}"
                            } else {
                                "Station ${apiBooking.stationId}"
                            }
                            
                            bookingsWithStationNames.add(
                                Booking(
                                    id = apiBooking.id,
                                    stationName = stationName,
                                    date = apiBooking.getFormattedDate(),
                                    time = apiBooking.getFormattedTime(),
                                    status = apiBooking.status,
                                    isUpcoming = apiBooking.isUpcoming(),
                                    qrToken = apiBooking.qrToken
                                )
                            )
                        }
                        
                        bookings = bookingsWithStationNames
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

    // Status-only groupings
    val approved = remember(bookings) { bookings.filter { it.status.equals("Approved", true) } }
    val pending = remember(bookings) { bookings.filter { it.status.equals("Pending", true) } }
    val completed = remember(bookings) { bookings.filter { it.status.equals("Completed", true) } }
    val canceled = remember(bookings) {
        bookings.filter { it.status.equals("Canceled", true) || it.status.equals("Cancelled", true) }
    }
    val rejected = remember(bookings) { bookings.filter { it.status.equals("Rejected", true) } }

    val tabs = listOf(
        "All (${bookings.size})",
        "Approved (${approved.size})",
        "Pending (${pending.size})",
        "Completed (${completed.size})",
        "Canceled (${canceled.size})",
        "Rejected (${rejected.size})"
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

        // Tabs by status only
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp
        ) {
            tabs.forEachIndexed { index, label ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content based on selected status tab
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Loading bookings...", color = Color.Gray)
                    }
                }
            }
            errorMessage.isNotEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "⚠️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = errorMessage, color = Color.Red, textAlign = TextAlign.Center)
                    }
                }
            }
            else -> {
                val list = when (selectedTab) {
                    0 -> bookings
                    1 -> approved
                    2 -> pending
                    3 -> completed
                    4 -> canceled
                    5 -> rejected
                    else -> bookings
                }
                if (list.isEmpty()) {
                    EmptyStateMessage("No bookings")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(list) { booking ->
                            BookingCard(
                                booking = booking,
                                showQRButton = booking.status.equals("Approved", true),
                                onQRCodeClick = onQRCodeClick,
                                onBookingClick = {
                                    selectedBooking = booking
                                    showBookingDetail = true
                                }
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
    
    // Booking Detail Dialog
    if (showBookingDetail && selectedBooking != null) {
        BookingDetailDialog(
            booking = selectedBooking!!,
            onDismiss = { 
                showBookingDetail = false
                selectedBooking = null
            },
            onCancelBooking = { booking ->
                scope.launch {
                    val userInfo = UserSession.getUserInfo()
                    if (userInfo != null) {
                        val repository = UserRepository()
                        val result = repository.cancelBooking(booking.id, userInfo.nic)
                        if (result.isSuccess) {
                            // Refresh bookings after successful cancellation
                            val bookingsResult = repository.getBookingsByOwner(userInfo.nic)
                            if (bookingsResult.isSuccess) {
                                bookings = bookingsResult.getOrNull()?.map { apiBooking ->
                                    Booking(
                                        id = apiBooking.id,
                                        stationName = "Loading...", // Will be updated by station fetch
                                        date = apiBooking.getFormattedDate(),
                                        time = apiBooking.getFormattedTime(),
                                        status = apiBooking.status,
                                        isUpcoming = apiBooking.isUpcoming(),
                                        qrToken = apiBooking.qrToken
                                    )
                                } ?: emptyList()
                            }
                            showBookingDetail = false
                            selectedBooking = null
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    showQRButton: Boolean,
    onQRCodeClick: (String) -> Unit,
    onBookingClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBookingClick() },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
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

            Text(text = "Date: ${booking.date}", fontSize = 14.sp, color = Color.Gray)
            Text(text = "Time: ${booking.time}", fontSize = 14.sp, color = Color.Gray)

            if (showQRButton) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { booking.qrToken?.let { onQRCodeClick(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
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
    val (backgroundColor, textColor) = when {
        status.equals("Approved", true) -> Color(0xFF4CAF50) to Color.White
        status.equals("Pending", true) -> Color(0xFFFF9800) to Color.White
        status.equals("Completed", true) -> Color(0xFF2196F3) to Color.White
        status.equals("Canceled", true) || status.equals("Cancelled", true) -> Color(0xFFF44336) to Color.White
        status.equals("Rejected", true) -> Color(0xFF9E9E9E) to Color.White
        else -> Color.Gray to Color.White
    }

    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
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

@Composable
fun BookingDetailDialog(
    booking: Booking,
    onDismiss: () -> Unit,
    onCancelBooking: (Booking) -> Unit
) {
    var showCancelConfirmation by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Booking Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailRow("Booking ID", booking.id)
                DetailRow("Station", booking.stationName)
                DetailRow("Date", booking.date)
                DetailRow("Time", booking.time)
                DetailRow("Status", booking.status)
                
                if (booking.status.equals("Approved", true)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✅ This booking is approved and ready for charging",
                        color = Color(0xFF4CAF50),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            if (booking.status.equals("Pending", true) || booking.status.equals("Approved", true)) {
                Button(
                    onClick = { showCancelConfirmation = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) {
                    Text("Cancel Booking")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
    
    // Cancel Confirmation Dialog
    if (showCancelConfirmation) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmation = false },
            title = {
                Text(
                    text = "Cancel Booking",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to cancel this booking? This action cannot be undone.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelBooking(booking)
                        showCancelConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) {
                    Text("Yes, Cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmation = false }) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}
