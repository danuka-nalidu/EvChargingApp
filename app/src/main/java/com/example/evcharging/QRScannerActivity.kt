package com.example.evcharging

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.evcharging.network.NetworkClient
import com.example.evcharging.network.StartByQrRequest
import com.example.evcharging.ui.theme.EvChargingTheme
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QRScannerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EvChargingTheme {
                QRScannerScreen(
                    onBackClick = { finish() },
                    onNavigateToFinalize = { bookingId ->
                        startActivity(
                            Intent(this, FinalizeBookingActivity::class.java)
                                .putExtra("BOOKING_ID", bookingId)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun QRScannerScreen(
    onBackClick: () -> Unit,
    onNavigateToFinalize: (String) -> Unit
) {
    val context = LocalContext.current

    var isScanning by remember { mutableStateOf(false) }
    var scannedData by remember { mutableStateOf("") }

    var qrToken by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var submitSuccess by remember { mutableStateOf<String?>(null) }

    // ZXing launcher (camera-based QR)
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        // result.contents is the text encoded in the QR
        if (result.contents != null) {
            val token = result.contents.trim()
            qrToken = token
            scannedData = "Scanned QR Token:\n$token"
        }
        isScanning = false
    }

    // Runtime camera permission launcher
    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val options = ScanOptions()
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setPrompt("Align the QR inside the frame")
                .setBeepEnabled(true)
                .setOrientationLocked(true)
            isScanning = true
            scanLauncher.launch(options)
        } else {
            submitError = "Camera permission denied. Please enable it to scan."
        }
    }

    fun startScan() {
        submitError = null
        submitSuccess = null
        val granted = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.CAMERA
        ) == PermissionChecker.PERMISSION_GRANTED

        if (granted) {
            val options = ScanOptions()
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setPrompt("Align the QR inside the frame")
                .setBeepEnabled(true)
                .setOrientationLocked(true)
            isScanning = true
            scanLauncher.launch(options)
        } else {
            cameraPermLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Scan QR Code",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Scanner placeholder (kept)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📱", fontSize = 48.sp, modifier = Modifier.padding(bottom = 16.dp))
                    Text(
                        text = "QR Scanner",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Tap the button to scan a QR",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "(ZXing camera is used)",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // Start Scan button -> opens camera
        Button(
            onClick = { startScan() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text(
                text = if (isScanning) "Scanning..." else "Start Scan",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Show scanned token (and allow manual edit/paste if needed)
        if (scannedData.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Scanned Booking Data:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = scannedData,
                        fontSize = 14.sp,
                        color = Color(0xFF2E7D32),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        OutlinedTextField(
            value = qrToken,
            onValueChange = { qrToken = it },
            label = { Text("QR Token") },
            placeholder = { Text("Scanned token will appear here") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            singleLine = true
        )

        if (submitError != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Text(
                    text = submitError!!,
                    fontSize = 14.sp,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        if (submitSuccess != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
            ) {
                Text(
                    text = submitSuccess!!,
                    fontSize = 14.sp,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Confirm -> call start-by-qr (you already have this working)
        Button(
            onClick = {
                submitError = null
                submitSuccess = null
                if (qrToken.isBlank()) {
                    submitError = "Please scan or paste a QR token."
                    return@Button
                }
                isSubmitting = true
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val resp = NetworkClient.apiService.startByQr(StartByQrRequest(qrToken))
                        if (resp.isSuccessful) {
                            val booking = resp.body()!!
                            submitSuccess = "Session started!"
                            onNavigateToFinalize(booking.id)
                        } else {
                            submitError = "Failed: HTTP ${resp.code()}"
                        }
                    } catch (e: Exception) {
                        submitError = e.message ?: "Unexpected error"
                    } finally {
                        isSubmitting = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
            enabled = !isSubmitting
        ) {
            Text(
                text = if (isSubmitting) "Starting..." else "Confirm Booking",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

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
