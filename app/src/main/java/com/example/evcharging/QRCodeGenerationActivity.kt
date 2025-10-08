package com.example.evcharging

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evcharging.ui.theme.EvChargingTheme
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

class QRCodeGenerationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EvChargingTheme {
                val bookingId = intent.getStringExtra("BOOKING_QR") ?: "RES001"
                QRCodeGenerationScreen(
                    bookingId = bookingId,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@Composable
fun QRCodeGenerationScreen(
    bookingId: String,
    onBackClick: () -> Unit
) {

    val qrBitmap by remember(bookingId) { mutableStateOf(generateQrBitmap(bookingId, 720)) }
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
            text = "QR Code",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // QR Code Placeholder
        Card(
            modifier = Modifier
                .size(260.dp)
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap!!.asImageBitmap(),
                        contentDescription = "Booking QR"
                    )
                } else {
                    Text("Failed to generate QR", color = Color.Red)
                }
            }
        }

        // Booking Details Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
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
                    text = "Booking Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                BookingDetailRow("Booking ID", bookingId)
                BookingDetailRow("Station", "Station A - Colombo")
                BookingDetailRow("Date", "15/12/2024")
                BookingDetailRow("Time", "10:00 - 12:00")
                BookingDetailRow("Status", "Approved")
                BookingDetailRow("Customer", "John Doe")
                BookingDetailRow("NIC", "123456789V")
            }
        }

        // Instructions Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3F2FD)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Instructions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "• Present this QR code to the station operator\n• Arrive 5 minutes before your scheduled time\n• Keep your phone charged for the QR code display\n• Contact support if you have any issues",
                    fontSize = 14.sp,
                    color = Color(0xFF1976D2),
                    lineHeight = 20.sp
                )
            }
        }

        // Back Button
        TextButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Back to Bookings",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun BookingDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun QRCodeGenerationScreenPreview() {
    EvChargingTheme {
        QRCodeGenerationScreen(
            bookingId = "RES001",
            onBackClick = {}
        )
    }
}

/** Create a QR code bitmap from content using ZXing. */
private fun generateQrBitmap(content: String, size: Int): Bitmap? = try {
    val hints = mapOf(
        EncodeHintType.MARGIN to 1,                       // small quiet zone
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
    )
    val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    bmp
} catch (_: Exception) { null }