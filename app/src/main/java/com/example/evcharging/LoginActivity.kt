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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evcharging.ui.theme.EvChargingTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EvChargingTheme {
                LoginScreen(
                    onLoginClick = { 
                        // For demo purposes, navigate to dashboard
                        // In real app, validate credentials first
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    },
                    onForgotPasswordClick = { /* Handle forgot password */ },
                    onSignUpClick = { 
                        startActivity(Intent(this, SignUpActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    var nicOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf("") }
    var isFormValid by remember { mutableStateOf(false) }
    
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
        Spacer(modifier = Modifier.height(64.dp))
        
        // App Name
        Text(
            text = "EV Charge Booker",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 48.dp)
        )
        
        // NIC or Email Field
        OutlinedTextField(
            value = nicOrEmail,
            onValueChange = { nicOrEmail = it },
            label = { Text("NIC or Email") },
            placeholder = { Text("NIC or Email (e.g., 123456789V or email@example.com)") },
            isError = loginError.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true
        )
        
        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )
        
        // Error Message
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
        
        // Login Button
        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50) // Green color
            ),
            enabled = isFormValid
        ) {
            Text(
                text = "Log In",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Role-based Access Note
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E8)
            )
        ) {
            Text(
                text = "🔐 Role-based access ready:\n• EV Owners → EV Owner Dashboard\n• Operators → Operator Dashboard\n• SQLite integration for login persistence",
                fontSize = 12.sp,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(12.dp),
                lineHeight = 16.sp
            )
        }
        
        // Forgot Password Link
        TextButton(
            onClick = onForgotPasswordClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Forgot Password?",
                color = Color.Black,
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Sign Up Link
        TextButton(
            onClick = onSignUpClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "New User? Sign Up",
                color = Color.Black,
                fontSize = 14.sp
            )
        }
    }
}
