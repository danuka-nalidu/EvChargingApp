package com.example.evcharging

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.auth0.android.jwt.JWT
import com.example.evcharging.database.UserDatabaseHelper
import com.example.evcharging.network.LoginRequest
import com.example.evcharging.network.NetworkClient
import com.example.evcharging.session.OperatorSession
import com.example.evcharging.ui.theme.EvChargingTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OperatorLoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        OperatorSession.initialize(this)
        setContent {
            EvChargingTheme {
                OperatorLoginScreen(
                    onLoginClick = { operatorId, password, callback ->
                        Log.i("OPERATORLOGIN","DATA $operatorId $password")
                        CoroutineScope(Dispatchers.Main).launch {

                            try {
                                val req = LoginRequest(
                                    email = operatorId.trim(),
                                    password=password,
                                    deviceId = "android-operator"
                                )

                                val resp = NetworkClient.apiService.login(req)

                                if (resp.isSuccessful){
                                    val body = resp.body()
                                    val accessToken = body!!.accessToken
                                    val jwt = JWT(accessToken)
                                    val role = jwt.getClaim("http://schemas.microsoft.com/ws/2008/06/identity/claims/role").asString()
                                    val name = jwt.getClaim("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name").asString()
                                    val email = jwt.getClaim("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress").asString()
                                    val userId = jwt.subject     // "sub"

                                    Log.i("OPERATORLOGIN","API RES $role $name $email $userId")
                                    if (!role.equals("Operator", ignoreCase = true)) {
                                        callback(false, "Access denied: this account has role $role. Use an Operator account.")
                                        return@launch
                                    }


                                    OperatorSession.setAuth(
                                        jwt = accessToken,
                                        userId = userId ?: "",
                                        fullName = name ?: "",
                                        email = email ?: "",
                                        role = role ?: ""
                                    )

                                    callback(true,"Login sucessfull! Welcome back.")

                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                        startActivity(Intent(this@OperatorLoginActivity, OperatorDashboardActivity::class.java))
                                        finish()
                                    }, 800)


                                } else{
                                    callback(false, "Login failed (HTTP ${resp.code()}). Check credentials.")

                                }
                            }catch (e: Exception) {
                                callback(false, "Network error: ${e.message}")
                            }

                        }

//                        try {
//                            val dbHelper = UserDatabaseHelper(this)
//                            val isAuthenticated = dbHelper.authenticateOperator(operatorId, password)
//
//                            if (isAuthenticated) {
//                                // Login successful
//                                callback(true, "Login successful! Welcome back.")
//                                // Navigate to dashboard after a short delay to show success message
//                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
//                                    startActivity(Intent(this, OperatorDashboardActivity::class.java))
//                                    finish()
//                                }, 1500)
//                            } else {
//                                // Login failed
//                                callback(false, "Login unsuccessful. Please check your credentials and try again.")
//                            }
//                        } catch (e: Exception) {
//                            // Handle any database errors
//                            callback(false, "Database error: ${e.message}")
//                        }
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
    onLoginClick: (String, String, (Boolean, String?) -> Unit) -> Unit,
    onForgotPasswordClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var operatorId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf("") }
    var loginSuccess by remember { mutableStateOf("") }
    var isFormValid by remember { mutableStateOf(false) }
    var isLoggingIn by remember { mutableStateOf(false) }

    // Form validation
    LaunchedEffect(operatorId, password) {
        isFormValid = operatorId.isNotEmpty() && password.isNotEmpty()
        loginError = ""
        loginSuccess = ""
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

        // Success Message
        if (loginSuccess.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E8)
                )
            ) {
                Text(
                    text = loginSuccess,
                    fontSize = 14.sp,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

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
                    loginSuccess = ""
                    onLoginClick(operatorId, password) { success, message ->
                        isLoggingIn = false
                        if (success) {
                            loginSuccess = message ?: "Login successful!"
                        } else {
                            loginError = message ?: "Login failed. Please try again."
                        }
                    }
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
            onLoginClick = { _, _, _ -> },
            onForgotPasswordClick = {},
            onBackClick = {}
        )
    }
}
