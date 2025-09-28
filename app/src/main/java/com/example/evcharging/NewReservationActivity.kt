package com.example.evcharging

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
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
import com.example.evcharging.ui.theme.EvChargingTheme
import java.util.*

class NewReservationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EvChargingTheme {
                NewReservationScreen(
                    onBackClick = { finish() },
                    onPreviewSummaryClick = { /* Handle preview */ },
                    onConfirmClick = { /* Handle confirmation */ }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReservationScreen(
    onBackClick: () -> Unit,
    onPreviewSummaryClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    val context = LocalContext.current
    
    // State variables
    var selectedStation by remember { mutableStateOf("") }
    var selectedSlot by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    
    // Sample data - in real app, this would come from web service
    val stations = listOf("Station A - Colombo", "Station B - Kandy", "Station C - Galle", "Station D - Negombo")
    val timeSlots = listOf("08:00 - 10:00", "10:00 - 12:00", "12:00 - 14:00", "14:00 - 16:00", "16:00 - 18:00", "18:00 - 20:00")
    
    // Date picker
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDate = "$dayOfMonth/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    
    // Time picker
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            selectedTime = String.format("%02d:%02d", hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

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
            text = "New Reservation",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Station Selection Dropdown
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
                placeholder = { Text("Choose a station") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStation) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedStation,
                onDismissRequest = { expandedStation = false }
            ) {
                stations.forEach { station ->
                    DropdownMenuItem(
                        text = { Text(station) },
                        onClick = {
                            selectedStation = station
                            expandedStation = false
                        }
                    )
                }
            }
        }
        
        // Time Slot Selection Dropdown
        var expandedSlot by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedSlot,
            onExpandedChange = { expandedSlot = !expandedSlot },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            OutlinedTextField(
                value = selectedSlot,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Time Slot") },
                placeholder = { Text("Choose a time slot") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSlot) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedSlot,
                onDismissRequest = { expandedSlot = false }
            ) {
                timeSlots.forEach { slot ->
                    DropdownMenuItem(
                        text = { Text(slot) },
                        onClick = {
                            selectedSlot = slot
                            expandedSlot = false
                        }
                    )
                }
            }
        }
        
        // Date Selection
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
        
        // Time Selection
        OutlinedTextField(
            value = selectedTime,
            onValueChange = {},
            readOnly = true,
            label = { Text("Reservation Time") },
            placeholder = { Text("Select time") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            trailingIcon = {
                TextButton(onClick = { timePickerDialog.show() }) {
                    Text("Select", color = Color(0xFF4CAF50))
                }
            }
        )
        
        // Preview Summary Button
        Button(
            onClick = onPreviewSummaryClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50) // Green color
            )
        ) {
            Text(
                text = "Preview Summary",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Confirm Button
        Button(
            onClick = onConfirmClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3) // Blue color
            )
        ) {
            Text(
                text = "Confirm Reservation",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
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
