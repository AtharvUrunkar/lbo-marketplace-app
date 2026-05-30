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
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.lbo_marketplace.utils.fetchProviderLocation

// =====================================================================================
// 🩺 REGEX VALIDATION LAYER (Prevents dummy entries like "   .", "///", etc.)
// =====================================================================================
fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
    return email.trim().matches(emailRegex)
}

fun isValidName(name: String): Boolean {
    val trimmed = name.trim()
    if (trimmed.length < 2) return false
    // Letters, spaces, dots, apostrophes only
    val nameRegex = "^[a-zA-Z\\s.']+$".toRegex()
    return trimmed.matches(nameRegex)
}

fun isValidPhone(phone: String): Boolean {
    val trimmed = phone.trim()
    if (trimmed.isEmpty()) return true
    // Valid standard numeric phone format (between 10 to 15 digits)
    val phoneRegex = "^[0-9]{10,15}$".toRegex()
    return trimmed.matches(phoneRegex)
}

fun isValidPart(part: String): Boolean {
    val trimmed = part.trim()
    if (trimmed.isEmpty()) return false
    // Blocks inputs like "     .", "///", etc. by ensuring at least one letter or digit exists
    return trimmed.any { it.isLetterOrDigit() }
}

fun isValidPincode(pincode: String): Boolean {
    val trimmed = pincode.trim()
    // Indian standard pincodes are 6 digits
    val pinRegex = "^[0-9]{6}$".toRegex()
    return trimmed.matches(pinRegex)
}

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

    // Address & Phone for registration completeness
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var manualAddressInput by remember { mutableStateOf("") }
    var showLocationRationaleDialog by remember { mutableStateOf(false) }
    var isRegisteringInProgress by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state is AuthState.Authenticated && isRegisteringInProgress) {
            context.getSharedPreferences("lbo_prefs", android.content.Context.MODE_PRIVATE)
                .edit()
                .putBoolean("newly_registered_${state.uid}", true)
                .apply()
            isRegisteringInProgress = false
        }
    }

    fun getAddressFromLocation(context: android.content.Context, lat: Double, lng: Double) {
        try {
            val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                city = addr.locality ?: addr.subAdminArea ?: ""
                area = addr.subLocality ?: addr.thoroughfare ?: city
                address = addr.getAddressLine(0) ?: ""
                pincode = addr.postalCode ?: ""
                manualAddressInput = "$city, $area, $address, $pincode"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun geocodeAddress(context: android.content.Context, city: String, area: String, fullAddress: String): Pair<Double, Double> {
        val addressQuery = listOf(fullAddress, area, city).filter { it.isNotBlank() }.joinToString(", ")
        if (addressQuery.isBlank()) return Pair(0.0, 0.0)
        return try {
            val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
            val addresses = geocoder.getFromLocationName(addressQuery, 1)
            if (!addresses.isNullOrEmpty()) {
                Pair(addresses[0].latitude, addresses[0].longitude)
            } else {
                Pair(0.0, 0.0)
            }
        } catch (e: Exception) {
            Pair(0.0, 0.0)
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fetchProviderLocation(context) { lat, lng ->
                latitude = lat
                longitude = lng
                getAddressFromLocation(context, lat, lng)
            }
        } else {
            showLocationRationaleDialog = true
            Toast.makeText(context, "For better service allow it, you can remove permission after time.", Toast.LENGTH_LONG).show()
        }
    }

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
                    .size(80.dp) // Compact logo size
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5)), // Subtle light gray background
                contentAlignment = Alignment.Center
            ) {
                VideoLoader(
                    videoResId = R.raw.logo,
                    onError = { /* Internal fallback to PNG is already handled in VideoLoader */ }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 📝 HEADERS
            Text(
                text = if (isRegisterMode) "Create an account" else "Welcome Back",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                letterSpacing = (-0.5).sp
            )

            if (!isRegisterMode) {
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Enter your credentials to continue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

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

            Spacer(modifier = Modifier.height(8.dp))

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
                Spacer(modifier = Modifier.height(8.dp))
                
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

                Spacer(modifier = Modifier.height(8.dp))

                // 📞 PHONE FIELD
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = { Text("Enter Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 📍 AUTO DETECT LOCATION BUTTON
                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            fetchProviderLocation(context) { lat, lng ->
                                latitude = lat
                                longitude = lng
                                getAddressFromLocation(context, lat, lng)
                            }
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                ) {
                    Text("Auto-detect Location", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 📍 SINGLE LOCATION FIELD (FALLBACK & MANUAL LOCATION INPUT)
                OutlinedTextField(
                    value = manualAddressInput,
                    onValueChange = { manualAddressInput = it },
                    placeholder = { Text("City, Area, Full Address, Pincode") },
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

            Spacer(modifier = Modifier.height(16.dp))

            // 🚀 CONTINUE BUTTON
            Button(
                onClick = {
                    val trimmedEmail = email.trim()
                    val trimmedPassword = password.trim()
                    val trimmedName = name.trim()
                    val trimmedPhone = phone.trim()
                    val trimmedLocation = manualAddressInput.trim()

                    if (trimmedEmail.isBlank() || trimmedPassword.isBlank() || (isRegisterMode && (trimmedName.isBlank() || trimmedLocation.isBlank()))) {
                        Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (!isValidEmail(trimmedEmail)) {
                        Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (trimmedPassword.length < 6) {
                        Toast.makeText(context, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (isRegisterMode) {
                        if (!isValidName(trimmedName)) {
                            Toast.makeText(context, "Please enter a valid name (letters & spaces only, min 2 chars)", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        if (trimmedPhone.isNotEmpty() && !isValidPhone(trimmedPhone)) {
                            Toast.makeText(context, "Please enter a valid phone number (10-15 digits)", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        if (trimmedLocation.isEmpty()) {
                            Toast.makeText(context, "Location input cannot be empty", Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        var resolvedCity = city
                        var resolvedArea = area
                        var resolvedAddress = address
                        var resolvedPincode = pincode

                        val expectedAutoDetected = "$city, $area, $address, $pincode".trim()
                        if (trimmedLocation != expectedAutoDetected) {
                            val parts = trimmedLocation.split(",").map { it.trim() }
                            resolvedCity = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: trimmedLocation
                            resolvedArea = parts.getOrNull(1)?.takeIf { it.isNotBlank() } ?: trimmedLocation
                            resolvedAddress = parts.getOrNull(2)?.takeIf { it.isNotBlank() } ?: trimmedLocation
                            resolvedPincode = parts.getOrNull(3)?.takeIf { it.isNotBlank() } ?: ""
                        }

                        var finalLat = latitude
                        var finalLng = longitude
                        if (finalLat == 0.0 && finalLng == 0.0) {
                            val geocoded = geocodeAddress(context, resolvedCity, resolvedArea, resolvedAddress)
                            finalLat = geocoded.first
                            finalLng = geocoded.second
                        }
                        isRegisteringInProgress = true
                        viewModel.register(
                            name = trimmedName, 
                            email = trimmedEmail, 
                            password = trimmedPassword,
                            phone = trimmedPhone,
                            address = resolvedAddress,
                            city = resolvedCity,
                            area = resolvedArea,
                            pincode = resolvedPincode,
                            latitude = finalLat,
                            longitude = finalLng
                        )
                    } else {
                        viewModel.login(trimmedEmail, trimmedPassword)
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

        if (showLocationRationaleDialog) {
            AlertDialog(
                onDismissRequest = { showLocationRationaleDialog = false },
                title = {
                    Text(
                        text = "Location Access Needed",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                text = {
                    Text(
                        text = "For a better service experience, please allow location access. You can easily remove this permission later in system settings.",
                        color = Color.DarkGray
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLocationRationaleDialog = false
                            try {
                                val intent = android.content.Intent(
                                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    android.net.Uri.fromParts("package", context.packageName, null)
                                )
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Failed to open settings.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                    ) {
                        Text("Open Settings", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showLocationRationaleDialog = false }
                    ) {
                        Text("Cancel", color = Color.Black, fontWeight = FontWeight.Medium)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}