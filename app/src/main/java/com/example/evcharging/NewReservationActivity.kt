package com.example.evcharging

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evcharging.network.CreateBookingRequest
import com.example.evcharging.network.NetworkClient
import com.example.evcharging.repository.UserRepository
import com.example.evcharging.session.UserSession
import com.example.evcharging.ui.theme.EvChargingTheme
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Calendar

class NewReservationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val stationId   = intent.getStringExtra("stationId")
        val stationName = intent.getStringExtra("stationName")
        val stationType = intent.getStringExtra("stationType")
        val stationAddr = intent.getStringExtra("stationAddress")

        Log.i("NEW_RESERVATION", "stationId: $stationId")
        Log.i("NEW_RESERVATION", "stationName: $stationName")
        Log.i("NEW_RESERVATION", "stationType: $stationType")
        Log.i("NEW_RESERVATION", "stationAddr: $stationAddr")

        setContent {
            EvChargingTheme {
                NewReservationScreen(
                    initialStationId = stationId,
                    initialStationName = stationName,
                    initialStationType = stationType,
                    initialStationAddress = stationAddr,
                    onBackClick = { finish() },
                    onPreviewSummaryClick = { /* optional preview */ },
                    onConfirmClick = { /* after success you can navigate if you like */ }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReservationScreen(
    initialStationId: String? = null,
    initialStationName: String? = null,
    initialStationType: String? = null,
    initialStationAddress: String? = null,
    onBackClick: () -> Unit,
    onPreviewSummaryClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ---- State ----
    var selectedStationId by remember { mutableStateOf(initialStationId ?: "") }
    var selectedStation by remember { mutableStateOf(initialStationName ?: "") }

    var selectedDate by remember { mutableStateOf("") }          // dd/MM/yyyy
    var selectedStartTime by remember { mutableStateOf("") }     // HH:mm
    var selectedEndTime by remember { mutableStateOf("") }       // HH:mm

    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var ok by remember { mutableStateOf<String?>(null) }

    // Fetch stations from API
    var stations by remember { mutableStateOf<List<com.example.evcharging.network.StationView>>(emptyList()) }
    var isLoadingStations by remember { mutableStateOf(true) }
    var stationError by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        val repository = UserRepository()
        try {
            val result = repository.getAllStations()
            if (result.isSuccess) {
                stations = result.getOrNull() ?: emptyList()
                stationError = null
            } else {
                stationError = result.exceptionOrNull()?.message ?: "Failed to load stations"
            }
        } catch (e: Exception) {
            stationError = "Error loading stations: ${e.message}"
        }
        isLoadingStations = false
    }

    // ---- Pickers ----
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val dd = "%02d".format(dayOfMonth)
            val mm = "%02d".format(month + 1)
            selectedDate = "$dd/$mm/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val startTimePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            selectedStartTime = "%02d:%02d".format(hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    val endTimePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            selectedEndTime = "%02d:%02d".format(hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    // ---- UI ----
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "New Reservation",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Error Message
        if (error != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                )
            ) {
                Text(
                    text = error!!,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // Success Message
        if (ok != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E8)
                )
            ) {
                Text(
                    text = ok!!,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Station dropdown
        var expandedStation by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedStation,
            onExpandedChange = { expandedStation = !expandedStation },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            OutlinedTextField(
                value = selectedStation,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Charging Station") },
                placeholder = { 
                    when {
                        isLoadingStations -> Text("Loading stations...")
                        stationError != null -> Text("Error loading stations")
                        else -> Text("Choose a station")
                    }
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStation) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                enabled = !isLoadingStations && stationError == null
            )
            ExposedDropdownMenu(
                expanded = expandedStation,
                onDismissRequest = { expandedStation = false }
            ) {
                if (isLoadingStations) {
                    DropdownMenuItem(
                        text = { Text("Loading stations...") },
                        onClick = { }
                    )
                } else if (stationError != null) {
                    DropdownMenuItem(
                        text = { Text("Error: $stationError") },
                        onClick = { }
                    )
                } else {
                    stations.forEach { station ->
                        DropdownMenuItem(
                            text = { Text("${station.name} (${station.type}) - ${station.address}") },
                            onClick = {
                                selectedStation = station.name
                                selectedStationId = station.id
                                expandedStation = false
                            }
                        )
                    }
                }
            }
        }

        // Date
        OutlinedTextField(
            value = selectedDate,
            onValueChange = {},
            readOnly = true,
            label = { Text("Reservation Date") },
            placeholder = { Text("Select date (max 7 days ahead)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            trailingIcon = {
                TextButton(onClick = { datePickerDialog.show() }) {
                    Text("Select", color = Color(0xFF4CAF50))
                }
            }
        )

        // Start time
        OutlinedTextField(
            value = selectedStartTime,
            onValueChange = {},
            readOnly = true,
            label = { Text("Start Time") },
            placeholder = { Text("Select start time") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            trailingIcon = {
                TextButton(onClick = { startTimePickerDialog.show() }) {
                    Text("Select", color = Color(0xFF4CAF50))
                }
            }
        )

        // End time
        OutlinedTextField(
            value = selectedEndTime,
            onValueChange = {},
            readOnly = true,
            label = { Text("End Time") },
            placeholder = { Text("Select end time") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            trailingIcon = {
                TextButton(onClick = { endTimePickerDialog.show() }) {
                    Text("Select", color = Color(0xFF4CAF50))
                }
            }
        )

        // Preview (optional)
        Button(
            onClick = onPreviewSummaryClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            enabled = !submitting
        ) {
            Text("Preview Summary", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        // Confirm → CALL BACKEND
        Button(
            onClick = {
                error = null; ok = null

                val nic = UserSession.getUserNIC()
                if (nic.isNullOrBlank()) {
                    error = "No active session. Please log in again."
                    return@Button
                }
                if (selectedStationId.isBlank()) {
                    error = "Please select a station from the map."
                    return@Button
                }
                if (selectedDate.isBlank()) {
                    error = "Please select a reservation date."
                    return@Button
                }
                if (selectedStartTime.isBlank()) {
                    error = "Please select a start time."
                    return@Button
                }
                if (selectedEndTime.isBlank()) {
                    error = "Please select an end time."
                    return@Button
                }

                // Build LocalDateTime from dd/MM/yyyy + HH:mm
                fun parseLocal(date: String, time: String): LocalDateTime {
                    val (d, m, y) = date.split("/").map { it.toInt() }
                    val (hh, mm) = time.split(":").map { it.toInt() }
                    return LocalDateTime.of(y, m, d, hh, mm)
                }
                val startLocal = parseLocal(selectedDate, selectedStartTime)
                val endLocal = parseLocal(selectedDate, selectedEndTime)
                if (!endLocal.isAfter(startLocal)) {
                    error = "End time must be after start time."
                    return@Button
                }

                // Convert to ISO-8601 UTC strings expected by backend
                fun toIsoUtc(ldt: LocalDateTime): String =
                    ldt.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toInstant().toString()

                val startUtc = toIsoUtc(startLocal)
                val endUtc = toIsoUtc(endLocal)

                submitting = true
                scope.launch {
                    try {
                        val resp = NetworkClient.apiService.createBooking(
                            CreateBookingRequest(
                                ownerNic = nic,
                                stationId = selectedStationId,
                                startUtc = startUtc,
                                endUtc = endUtc
                            )
                        )
                        if (resp.isSuccessful) {
                            val id = resp.body()?.id ?: "(unknown)"
                            ok = "Reservation created"
                            Toast.makeText(context, "Reservation created", Toast.LENGTH_SHORT).show()
                            onConfirmClick() // let Activity navigate if desired
                        } else {
                            val msg = resp.errorBody()?.string()?.takeIf { it.isNotBlank() }
                            error = msg ?: "Failed to create reservation (HTTP ${resp.code()})."
                        }
                    } catch (e: Exception) {
                        error = "Network error: ${e.message}"
                    } finally {
                        submitting = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
            enabled = !submitting
        ) {
            Text(
                text = if (submitting) "Confirming..." else "Confirm Reservation",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Back
        TextButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Dashboard", color = Color.Gray, fontSize = 14.sp)
        }
    }
}
