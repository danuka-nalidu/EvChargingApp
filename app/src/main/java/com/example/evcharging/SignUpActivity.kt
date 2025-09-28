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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evcharging.database.UserDatabaseHelper
import com.example.evcharging.ui.theme.EvChargingTheme

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EvChargingTheme {
                SignUpScreen(
                    onSignUpClick = { nic, fullName, email, password, callback ->
                        try {
                            val dbHelper = UserDatabaseHelper(this)
                            val success = dbHelper.createEVOwner(nic, fullName, email, password)
                            
                            if (success) {
                                // Registration successful, navigate to dashboard
                                startActivity(Intent(this, EVOwnerDashboardActivity::class.java))
                                finish()
                                callback(true, null)
                            } else {
                                // Registration failed - show error message
                                callback(false, "Registration failed. NIC or email may already exist.")
                            }
                        } catch (e: Exception) {
                            // Handle any database errors
                            callback(false, "Database error: ${e.message}")
                        }
                    },
                    onLoginClick = { 
                        startActivity(Intent(this, EVOwnerLoginActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUpClick: (String, String, String, String, (Boolean, String?) -> Unit) -> Unit,
    onLoginClick: () -> Unit
) {
    var nic by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    // Validation states
    var nicError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }
    var isFormValid by remember { mutableStateOf(false) }
    
    // Registration states
    var registrationError by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    
    // Validation functions
    fun validateNIC(nic: String): String {
        return when {
            nic.isEmpty() -> "NIC is required"
            !nic.matches(Regex("^\\d{9}[VvXx]$")) -> "Invalid NIC format (e.g., 123456789V)"
            else -> "" // In real app, check SQLite for uniqueness
        }
    }
    
    fun validateEmail(email: String): String {
        return when {
            email.isEmpty() -> "Email is required"
            !email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) -> "Invalid email format"
            else -> "" // In real app, check SQLite for uniqueness
        }
    }
    
    fun validatePassword(password: String): String {
        return when {
            password.isEmpty() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> ""
        }
    }
    
    fun validateConfirmPassword(password: String, confirmPassword: String): String {
        return when {
            confirmPassword.isEmpty() -> "Please confirm your password"
            password != confirmPassword -> "Passwords do not match"
            else -> ""
        }
    }
    
    // Real-time validation
    LaunchedEffect(nic, email, password, confirmPassword) {
        nicError = validateNIC(nic)
        emailError = validateEmail(email)
        passwordError = validatePassword(password)
        confirmPasswordError = validateConfirmPassword(password, confirmPassword)
        
        isFormValid = nicError.isEmpty() && emailError.isEmpty() && 
                     passwordError.isEmpty() && confirmPasswordError.isEmpty() &&
                     nic.isNotEmpty() && email.isNotEmpty() && 
                     password.isNotEmpty() && confirmPassword.isNotEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // NIC Field
        OutlinedTextField(
            value = nic,
            onValueChange = { nic = it },
            label = { Text("NIC") },
            placeholder = { Text("Enter your NIC (e.g., 123456789V)") },
            isError = nicError.isNotEmpty(),
            supportingText = if (nicError.isNotEmpty()) { { Text(nicError) } } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true
        )
        
        // Full Name Field
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true
        )
        
        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            placeholder = { Text("Enter your email address") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = emailError.isNotEmpty(),
            supportingText = if (emailError.isNotEmpty()) { { Text(emailError) } } else null,
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
            placeholder = { Text("Enter your password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = passwordError.isNotEmpty(),
            supportingText = if (passwordError.isNotEmpty()) { { Text(passwordError) } } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true
        )
        
        // Confirm Password Field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            placeholder = { Text("Confirm your password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = confirmPasswordError.isNotEmpty(),
            supportingText = if (confirmPasswordError.isNotEmpty()) { { Text(confirmPasswordError) } } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )
        
        // Registration Error Display
        if (registrationError.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                )
            ) {
                Text(
                    text = registrationError,
                    fontSize = 14.sp,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        
        // Sign Up Button
        Button(
            onClick = {
                if (isFormValid && !isRegistering) {
                    isRegistering = true
                    registrationError = ""
                    onSignUpClick(nic, fullName, email, password) { success, errorMessage ->
                        isRegistering = false
                        if (success) {
                            // Success will be handled by navigation in the activity
                        } else {
                            registrationError = errorMessage ?: "Registration failed. Please try again."
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50) // Green color
            ),
            enabled = isFormValid && !isRegistering
        ) {
            Text(
                text = if (isRegistering) "Creating Account..." else "Sign Up",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        // SQLite Integration Note
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3F2FD)
            )
        ) {
            Text(
                text = "💾 Ready for SQLite integration:\n• NIC uniqueness validation\n• Account creation/updates\n• User data persistence",
                fontSize = 12.sp,
                color = Color(0xFF1976D2),
                modifier = Modifier.padding(12.dp),
                lineHeight = 16.sp
            )
        }
        
        // Login Link
        TextButton(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Already have an account? Log In",
                color = Color.Black,
                fontSize = 14.sp
            )
        }
    }
}
