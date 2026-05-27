package com.example.lbo_marketplace.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lbo_marketplace.R
import com.example.lbo_marketplace.ui.navigation.VideoLoader

/**
 * Premium Login/Registration Screen.
 * 
 * FEATURES:
 * - Responsive layout that centers on any screen size.
 * - Looping Video Logo (MP4) for a premium first impression.
 * - Minimalist Design (Black & White) matching modern UI standards.
 * - Handles both Login and Registration flows with a toggle.
 */
@Composable
fun AuthSessionTestScreen(viewModel: AuthViewModel = viewModel()) {

    val context = LocalContext.current
    val state = viewModel.authState.value
    val scrollState = rememberScrollState()

    // UI States
    var isRegisterMode by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp) // Industry standard for responsive mobile forms
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            // 🎬 APP LOGO (Looping MP4)
            Box(
                modifier = Modifier
                    .size(100.dp) // Standard logo size
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5)), // Subtle light gray background
                contentAlignment = Alignment.Center
            ) {
                VideoLoader(
                    videoResId = R.raw.logo,
                    onError = { /* Internal fallback to PNG is already handled in VideoLoader */ }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 📝 HEADERS
            Text(
                text = if (isRegisterMode) "Create an account" else "Welcome Back",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                letterSpacing = (-0.5).sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (isRegisterMode) 
                    "Enter your email to sign up for this app" 
                else 
                    "Enter your credentials to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // 📧 EMAIL FIELD
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("email@domain.com") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 🔑 PASSWORD FIELD
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            if (isRegisterMode) {
                Spacer(modifier = Modifier.height(14.dp))
                
                // 👤 NAME FIELD
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Enter Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 🚀 CONTINUE BUTTON
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank() || (isRegisterMode && name.isBlank())) {
                        Toast.makeText(context, "All fields required", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (isRegisterMode) {
                        viewModel.register(name, email, password)
                    } else {
                        viewModel.login(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                if (state is AuthState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = "Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 🔄 TOGGLE MODE (Login <-> SignUp)
            TextButton(
                onClick = { isRegisterMode = !isRegisterMode },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isRegisterMode) 
                        "Already have an account? Login" 
                    else 
                        "Don't have an account? Sign Up",
                    color = Color.Black,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 📄 FOOTER / COMPLIANCE
            Text(
                text = "By clicking continue, you agree to our Terms of Service and Privacy Policy",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp),
                lineHeight = 16.sp
            )
            
            // Error Feedback
            if (state is AuthState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}