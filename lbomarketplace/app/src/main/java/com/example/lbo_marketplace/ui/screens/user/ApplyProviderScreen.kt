package com.example.lbo_marketplace.ui.screens.user

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices

/**
 * Screen to apply as a service provider.
 * 
 * FIXES:
 * - Standardized all buttons to FULL BLACK (#000000).
 * - Enforced PURE WHITE (#FFFFFF) background.
 * - Added statusBarsPadding() for safe UI rendering.
 */
@Composable
fun ApplyProviderScreen(
    onSubmit: (
        String, // name
        String, // serviceType
        String, // description
        String, // experience
        Double, // latitude
        Double, // longitude
        Uri     // verification document uri
    ) -> Unit
) {

    val context = LocalContext.current

    // =========================================================
    // 🔥 FORM STATES
    // =========================================================

    var name by remember { mutableStateOf("") }

    var serviceType by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    var experience by remember {
        mutableStateOf("")
    }

    // =========================================================
    // 🔥 LOCATION
    // =========================================================

    var latitude by remember {
        mutableStateOf(0.0)
    }

    var longitude by remember {
        mutableStateOf(0.0)
    }

    // =========================================================
    // 🔥 DOCUMENT STATE
    // =========================================================

    var verificationDocUri by remember {
        mutableStateOf<Uri?>(null)
    }

    // =========================================================
    // 🔥 SUBMIT STATE
    // =========================================================

    var isSubmitting by remember {
        mutableStateOf(false)
    }

    // =========================================================
    // 🔥 DOCUMENT PICKER
    // =========================================================

    val documentPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            uri?.let {

                val mimeType =
                    context.contentResolver
                        .getType(uri)

                if (mimeType != "application/pdf") {

                    Toast.makeText(
                        context,
                        "Only PDF files allowed",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@rememberLauncherForActivityResult
                }

                verificationDocUri = uri
            }
        }

    // =========================================================
    // 🔥 LOCATION PERMISSION
    // =========================================================

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->

            if (isGranted) {

                getLocation(context) { lat, lng ->

                    latitude = lat
                    longitude = lng
                }

            } else {

                Toast.makeText(
                    context,
                    "Location permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    // =========================================================
    // 🔥 UI
    // =========================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .background(Color.White) // ✅ PURE WHITE
            .statusBarsPadding()
            .padding(16.dp)
    ) {

        Text(
            text = "Apply as Service Provider",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(20.dp))

        // =====================================================
        // 🔥 DOCUMENT SECTION
        // =====================================================

        Text("Verification Document (PDF only)")

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {

                documentPickerLauncher.launch(
                    "application/pdf"
                )
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {

            Text("Upload Verification Document", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        verificationDocUri?.let { uri ->

            val docSize =
                getFileSizeInMB(
                    context,
                    uri
                )

            Text("✅ PDF Selected")

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text =
                    "Document Size: %.2f MB"
                        .format(docSize)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // =====================================================
        // 🔥 FORM FIELDS
        // =====================================================

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = {
                Text("Full Name")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Black, focusedLabelColor = Color.Black)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = serviceType,
            onValueChange = {
                serviceType = it
            },
            label = {
                Text("Service Type")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Black, focusedLabelColor = Color.Black)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = experience,
            onValueChange = {
                experience = it
            },
            label = {
                Text("Experience")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Black, focusedLabelColor = Color.Black)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = description,
            onValueChange = {
                description = it
            },
            label = {
                Text("Description")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Black, focusedLabelColor = Color.Black)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // =====================================================
        // 📍 LOCATION
        // =====================================================

        Button(
            onClick = {

                if (
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    getLocation(context) { lat, lng ->

                        latitude = lat
                        longitude = lng
                    }

                } else {

                    locationPermissionLauncher.launch(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black) // ✅ FULL BLACK
        ) {

            Text("Get Current Location", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("📍 Latitude: $latitude")

        Text("📍 Longitude: $longitude")

        Spacer(modifier = Modifier.height(30.dp))

        // =====================================================
        // 🚀 SUBMIT BUTTON
        // =====================================================

        Button(
            onClick = {

                when {

                    name.isBlank() ||
                            serviceType.isBlank() ||
                            experience.isBlank() -> {

                        Toast.makeText(
                            context,
                            "Please fill all required fields",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    verificationDocUri == null -> {

                        Toast.makeText(
                            context,
                            "Please upload PDF document",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> {

                        val docSize =
                            getFileSizeInMB(
                                context,
                                verificationDocUri!!
                            )

                        // 🔥 4MB LIMIT
                        if (docSize > 4) {

                            Toast.makeText(
                                context,
                                "PDF must be below 4 MB",
                                Toast.LENGTH_SHORT
                            ).show()

                            return@Button
                        }

                        isSubmitting = true

                        onSubmit(
                            name,
                            serviceType,
                            description,
                            experience,
                            latitude,
                            longitude,
                            verificationDocUri!!
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            enabled = !isSubmitting
        ) {

            if (isSubmitting) {

                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )

            } else {

                Text("Submit Application", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

// =============================================================
// 🔥 LOCATION FUNCTION
// =============================================================

@SuppressLint("MissingPermission")
fun getLocation(
    context: Context,
    onResult: (Double, Double) -> Unit
) {

    val fusedLocationClient =
        LocationServices
            .getFusedLocationProviderClient(
                context
            )

    fusedLocationClient.lastLocation
        .addOnSuccessListener { location: Location? ->

            if (location != null) {

                onResult(
                    location.latitude,
                    location.longitude
                )

            } else {

                onResult(0.0, 0.0)
            }
        }
        .addOnFailureListener {

            onResult(0.0, 0.0)
        }
}

// =============================================================
// 🔥 FILE SIZE
// =============================================================

fun getFileSizeInMB(
    context: Context,
    uri: Uri
): Double {

    val cursor =
        context.contentResolver
            .openFileDescriptor(
                uri,
                "r"
            )

    val size =
        cursor?.statSize ?: 0L

    cursor?.close()

    return size.toDouble() /
            (1024 * 1024)
}