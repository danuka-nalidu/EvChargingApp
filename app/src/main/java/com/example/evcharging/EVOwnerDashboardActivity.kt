package com.example.evcharging

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.unit.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evcharging.network.NetworkClient
import com.example.evcharging.network.StationView
import com.example.evcharging.repository.UserRepository
import com.example.evcharging.session.UserSession
import com.example.evcharging.ui.theme.EvChargingTheme
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import retrofit2.awaitResponse

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition

class EVOwnerDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        UserSession.initialize(this)


        setContent {
            EvChargingTheme {
                EVOwnerDashboardScreen(
                    onMakeReservationClick = { 
                        startActivity(Intent(this, NewReservationActivity::class.java))
                    },
                    onMyBookingsClick = { 
                        startActivity(Intent(this, ViewBookingsActivity::class.java))
                    },
                    onDeactivateAccountClick = {
                        // This will be handled by the confirmation dialog in the composable
                    },
                    onLogoutClick = {
                        UserSession.logout()
                        startActivity(Intent(this, SplashActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun EVOwnerDashboardScreen(
    onMakeReservationClick: () -> Unit,
    onMyBookingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDeactivateAccountClick: () -> Unit
) {
    // Sample data - in real app, this would come from a data source
    // ---- state ----
    var pendingReservations by remember { mutableStateOf(0) }
    var approvedReservations by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Deactivate account state
    var showDeactivateDialog by remember { mutableStateOf(false) }
    var isDeactivating by remember { mutableStateOf(false) }
    var deactivateMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()


    var stations by remember { mutableStateOf<List<StationView>>(emptyList()) }
    val defaultCenter = LatLng(7.2906, 80.6337) // Kandy (pick any default)
    var center by remember { mutableStateOf(defaultCenter) }

    LaunchedEffect(Unit) {
        try {
            val respStations = NetworkClient.apiService
                .nearby(lat = center.latitude, lng = center.longitude, km = 10000.0, take = 50)
            if (respStations.isSuccessful) {
                stations = respStations.body().orEmpty()
            } else {
                Log.w("DASHBOARD", "Nearby stations failed: HTTP ${respStations.code()}")
            }
        } catch (e: Exception) {
            Log.e("DASHBOARD", "Nearby stations error: ${e.message}", e)
        }

        try {
            val nic = UserSession.getUserNIC()
            Log.i("DASHBORADDDD","NICCCC $nic")
            if (nic.isNullOrBlank()) {
                error = "No active session. Please log in."
                loading = false
                return@LaunchedEffect
            }

            val resp = NetworkClient.bookings.listByOwner(nic).awaitResponse()

            Log.i("BOOKINGS","DATA $resp")
            if (resp.isSuccessful) {
                val items = resp.body().orEmpty()
                pendingReservations = items.count { it.status.equals("Pending", ignoreCase = true) }
                approvedReservations = items.count { it.status.equals("Approved", ignoreCase = true) }
            } else {
                error = "Failed to load: HTTP ${resp.code()}"
            }

        }catch (e: Exception) {
            error = e.message ?: "Unexpected error"
        } finally {
            loading = false
        }

    }




    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Dashboard Title
        Text(
            text = "EV Owner Dashboard",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Pending Reservations Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Pending Reservations",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Pending: $pendingReservations",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800) // Orange color for pending
                )
            }
        }
        
        // Approved Reservations Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Approved Reservations",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Approved: $approvedReservations",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50) // Green color for approved
                )
            }
        }
        
        // Nearby Stations Map Placeholder
        StationsMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .padding(bottom = 16.dp),
            center = center,
            stations = stations
        )
        
        // Make Reservation Button
        Button(
            onClick = onMakeReservationClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50) // Green color
            )
        ) {
            Text(
                text = "Make Reservation",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        // My Bookings Button
        Button(
            onClick = onMyBookingsClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3) // Blue color
            )
        ) {
            Text(
                text = "My Bookings",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Deactivate Account Button
        TextButton(
            onClick = { showDeactivateDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Deactivate Account",
                color = Color(0xFFF44336), // Red color for deactivation
                fontSize = 14.sp
            )
        }
        
        // Logout Button
        TextButton(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Logout",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
    
    // Deactivate Account Confirmation Dialog
    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!isDeactivating) {
                    showDeactivateDialog = false 
                }
            },
            title = {
                Text(
                    text = "Deactivate Account",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF44336)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to deactivate your account?",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "This action will:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "• Prevent you from making new reservations",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                    )
                    Text(
                        text = "• Cancel all pending bookings",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                    )
                    Text(
                        text = "• Require admin approval to reactivate",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                    )
                    Text(
                        text = "This action cannot be undone!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF44336)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isDeactivating = true
                            val userInfo = UserSession.getUserInfo()
                            if (userInfo != null) {
                                val repository = UserRepository()
                                val result = repository.deactivateAccount(userInfo.nic)
                                if (result.isSuccess) {
                                    deactivateMessage = "Account deactivated successfully"
                                    // Logout and redirect to splash screen
                                    UserSession.logout()
                                    onLogoutClick()
                                } else {
                                    deactivateMessage = result.exceptionOrNull()?.message ?: "Failed to deactivate account"
                                    isDeactivating = false
                                }
                            } else {
                                deactivateMessage = "User session not found"
                                isDeactivating = false
                            }
                        }
                    },
                    enabled = !isDeactivating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    )
                ) {
                    if (isDeactivating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Deactivate", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        if (!isDeactivating) {
                            showDeactivateDialog = false 
                        }
                    },
                    enabled = !isDeactivating
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Show deactivation message if any
    if (deactivateMessage != null) {
        LaunchedEffect(deactivateMessage) {
            kotlinx.coroutines.delay(3000)
            deactivateMessage = null
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (deactivateMessage!!.contains("success")) {
                    Color(0xFFE8F5E8)
                } else {
                    Color(0xFFFFEBEE)
                }
            )
        ) {
            Text(
                text = deactivateMessage!!,
                color = if (deactivateMessage!!.contains("success")) {
                    Color(0xFF2E7D32)
                } else {
                    Color(0xFFD32F2F)
                },
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp
            )
        }
    }
}
@Composable
private fun StationsMap(
    modifier: Modifier = Modifier,
    center: LatLng,
    stations: List<StationView>
) {
    val context = LocalContext.current

    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, 13f)
    }

    // 1) Ensure Maps is initialized
    LaunchedEffect(Unit) {
        try {
            com.google.android.gms.maps.MapsInitializer.initialize(context.applicationContext)
        } catch (e: Exception) {
            android.util.Log.e("MAPS", "MapsInitializer failed: ${e.message}", e)
        }
    }

    // 2) Wait for map to be ready before creating BitmapDescriptors
    var mapLoaded by remember { mutableStateOf(false) }

    // Helper to load bitmap from drawable (MUST be a PNG/JPG, not a vector XML)
    fun bitmapDescriptor(resId: Int, width: Int = 80, height: Int = 80): BitmapDescriptor? = try {
        val original = BitmapFactory.decodeResource(context.resources, resId)
        val scaled = Bitmap.createScaledBitmap(original, width, height, false)
        BitmapDescriptorFactory.fromBitmap(scaled)
    } catch (e: Exception) {
        Log.e("MAPS", "Icon load failed: ${e.message}")
        null
    }

    // Build icons only after the map is loaded
    val acIcon: BitmapDescriptor? = remember(mapLoaded) {
        if (mapLoaded) bitmapDescriptor(R.drawable.station, 80, 80) else null
    }
    val dcIcon: BitmapDescriptor? = remember(mapLoaded) {
        if (mapLoaded) bitmapDescriptor(R.drawable.station, 80, 80) else null
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = camera,
            onMapLoaded = { mapLoaded = true }  // <- signal map is ready
        ) {
            val ctx = context
            stations.forEach { s ->
                Marker(
                    state = MarkerState(LatLng(s.latitude, s.longitude)),
                    title = s.name,
                    snippet = "${s.type} • Slots: ${s.parallelSlots}",
                    // If custom icon not ready yet, GoogleMap will use the default pin
                    icon = when {
                        s.type.equals("AC", true) -> acIcon
                        else -> dcIcon
                    },
                    onClick = {
                        val i = Intent(ctx, NewReservationActivity::class.java).apply {
                            putExtra("stationId", s.id)
                            putExtra("stationName", s.name)
                            putExtra("stationType", s.type)
                            putExtra("stationAddress", s.address)
                        }
                        ctx.startActivity(i)
                        true // consume click (don’t auto-center)
                    }

                )
            }
        }
    }
}

