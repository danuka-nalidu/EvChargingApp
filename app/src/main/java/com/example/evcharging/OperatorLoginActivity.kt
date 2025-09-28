package com.example.evcharging

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evcharging.database.UserDatabaseHelper
import com.example.evcharging.ui.theme.EvChargingTheme

class OperatorLoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EvChargingTheme {
                OperatorLoginScreen(
                    onLoginClick = { operatorId, password ->
                        try {
                            val dbHelper = UserDatabaseHelper(this)
                            val isAuthenticated = dbHelper.authenticateOperator(operatorId, password)
                            
                            if (isAuthenticated) {
                                // Login successful, navigate to dashboard
                                startActivity(Intent(this, OperatorDashboardActivity::class.java))
                                finish()
                            } else {
                                // Login failed - this will be handled in the UI
                            }
                        } catch (e: Exception) {
                            // Handle any database errors
                            // This will be handled in the UI
                        }
                    },
                    onForgotPasswordClick = { /* Handle forgot password */ },
                    onBackClick = {
                        startActivity(Intent(this, SplashActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorLoginScreen(
    onLoginClick: (String, String) -> Unit,
    onForgotPasswordClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var operatorId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf("") }
    var isFormValid by remember { mutableStateOf(false) }
    var isLoggingIn by remember { mutableStateOf(false) }

    // Form validation
    LaunchedEffect(operatorId, password) {
        isFormValid = operatorId.isNotEmpty() && password.isNotEmpty()
        loginError = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBackClick) {
                Text(
                    text = "← Back",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        // Operator Icon
        Card(
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 16.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3F2FD)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔧",
                    fontSize = 32.sp
                )
            }
        }

        Text(
            text = "Operator Login",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (loginError.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                )
            ) {
                Text(
                    text = loginError,
                    fontSize = 14.sp,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        OutlinedTextField(
            value = operatorId,
            onValueChange = { operatorId = it },
            label = { Text("Operator ID") },
            placeholder = { Text("e.g., OP001 or operator@station.com") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            ),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        Button(
            onClick = {
                if (isFormValid && !isLoggingIn) {
                    isLoggingIn = true
                    loginError = ""
                    onLoginClick(operatorId, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            ),
            enabled = isFormValid && !isLoggingIn
        ) {
            Text(
                text = if (isLoggingIn) "Logging In..." else "Log In as Operator",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        TextButton(
            onClick = onForgotPasswordClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Forgot Password?",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        // Operator Features Info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3F2FD)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "🔧 Operator Features:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "• Scan QR codes from EV owners\n• Confirm charging sessions\n• Finalize operations\n• View station statistics",
                    fontSize = 12.sp,
                    color = Color(0xFF1976D2),
                    lineHeight = 16.sp
                )
            }
        }

        // SQLite Integration Note
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E8)
            )
        ) {
            Text(
                text = "💾 Local SQLite Integration:\n• Operator authentication\n• Session data persistence\n• Station management",
                fontSize = 12.sp,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(12.dp),
                lineHeight = 16.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OperatorLoginScreenPreview() {
    EvChargingTheme {
        OperatorLoginScreen(
            onLoginClick = {} as (String, String) -> Unit,
            onForgotPasswordClick = {},
            onBackClick = {}
        )
    }
}
