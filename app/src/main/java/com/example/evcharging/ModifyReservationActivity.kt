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

class ModifyReservationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EvChargingTheme {
                ModifyReservationScreen(
                    onBackClick = { finish() },
                    onSaveChangesClick = { /* Handle save changes */ }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifyReservationScreen(
    onBackClick: () -> Unit,
    onSaveChangesClick: () -> Unit
) {
    val context = LocalContext.current
    
    // State variables
    var selectedSlot by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    // Sample data - in real app, this would come from web service
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
            text = "Modify Reservation",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Current Reservation Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
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
                    text = "Current Reservation",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Station: Station A - Colombo",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Date: 15/12/2024",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Time: 10:00 - 12:00",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Status: Approved",
                    fontSize = 14.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Modification Notice
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFF3E0)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = "⚠️ Modifications allowed only if ≥12 hours before reservation time",
                fontSize = 14.sp,
                color = Color(0xFFE65100),
                modifier = Modifier.padding(16.dp)
            )
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
                label = { Text("Update Time Slot") },
                placeholder = { Text("Choose a new time slot") },
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
            label = { Text("Update Date") },
            placeholder = { Text("Select new date") },
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
            label = { Text("Update Time") },
            placeholder = { Text("Select new time") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            trailingIcon = {
                TextButton(onClick = { timePickerDialog.show() }) {
                    Text("Select", color = Color(0xFF4CAF50))
                }
            }
        )
        
        // Save Changes Button
        Button(
            onClick = { showConfirmDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50) // Green color
            )
        ) {
            Text(
                text = "Save Changes",
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
    
    // Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Changes") },
            text = { Text("Are you sure you want to modify this reservation? The changes will be applied immediately.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSaveChangesClick()
                        showConfirmDialog = false
                    }
                ) {
                    Text("Yes, Save Changes", color = Color(0xFF4CAF50))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
