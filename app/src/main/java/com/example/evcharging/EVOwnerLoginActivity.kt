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

class EVOwnerLoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EvChargingTheme {
                EVOwnerLoginScreen(
                    onLoginClick = { nicOrEmail, password ->
                        try {
                            val dbHelper = UserDatabaseHelper(this)
                            val isAuthenticated = dbHelper.authenticateEVOwner(nicOrEmail, password)
                            
                            if (isAuthenticated) {
                                // Login successful, navigate to dashboard
                                startActivity(Intent(this, EVOwnerDashboardActivity::class.java))
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
                    onSignUpClick = { 
                        startActivity(Intent(this, SignUpActivity::class.java))
                    },
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
fun EVOwnerLoginScreen(
    onLoginClick: (String, String) -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var nicOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf("") }
    var isFormValid by remember { mutableStateOf(false) }
    var isLoggingIn by remember { mutableStateOf(false) }

    // Form validation
    LaunchedEffect(nicOrEmail, password) {
        isFormValid = nicOrEmail.isNotEmpty() && password.isNotEmpty()
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

        // EV Owner Icon
        Card(
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 16.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E8)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🚗",
                    fontSize = 32.sp
                )
            }
        }

        Text(
            text = "EV Owner Login",
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
            value = nicOrEmail,
            onValueChange = { nicOrEmail = it },
            label = { Text("NIC or Email") },
            placeholder = { Text("e.g., 123456789V or email@example.com") },
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
                    onLoginClick(nicOrEmail, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            ),
            enabled = isFormValid && !isLoggingIn
        ) {
            Text(
                text = if (isLoggingIn) "Logging In..." else "Log In as EV Owner",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        TextButton(
            onClick = onForgotPasswordClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = "Forgot Password?",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "New EV Owner? ",
                color = Color.Gray,
                fontSize = 14.sp
            )
            TextButton(onClick = onSignUpClick) {
                Text(
                    text = "Sign Up",
                    color = Color(0xFF4CAF50),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // SQLite Integration Note
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E8)
            )
        ) {
            Text(
                text = "💾 Local SQLite Integration:\n• NIC-based authentication\n• User data persistence\n• Account management",
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
fun EVOwnerLoginScreenPreview() {
    EvChargingTheme {
        EVOwnerLoginScreen(
            onLoginClick = {} as (String, String) -> Unit,
            onForgotPasswordClick = {},
            onSignUpClick = {},
            onBackClick = {}
        )
    }
}
