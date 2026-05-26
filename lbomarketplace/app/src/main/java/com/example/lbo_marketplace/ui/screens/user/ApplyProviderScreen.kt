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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.example.lbo_marketplace.utils.getAddressFromLocation

@Composable
fun ApplyProviderScreen(

    onSubmit: (

        String, // name

        String, // service type

        String, // description

        String, // experience

        Double, // latitude

        Double, // longitude

        Uri // verification document

    ) -> Unit
) {

    val context =
        LocalContext.current

    // =====================================================
    // 🔥 FORM STATES
    // =====================================================

    var name by remember {
        mutableStateOf("")
    }

    var serviceType by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    var experience by remember {
        mutableStateOf("")
    }

    // =====================================================
    // 🔥 LOCATION
    // =====================================================

    var latitude by remember {
        mutableStateOf(0.0)
    }

    var longitude by remember {
        mutableStateOf(0.0)
    }

    var city by remember {
        mutableStateOf("")
    }

    var area by remember {
        mutableStateOf("")
    }

    var fullAddress by remember {
        mutableStateOf("")
    }

    // =====================================================
    // 🔥 DOCUMENT
    // =====================================================

    var verificationDocUri by remember {
        mutableStateOf<Uri?>(null)
    }

    // =====================================================
    // 🔥 LOADING
    // =====================================================

    var isSubmitting by remember {
        mutableStateOf(false)
    }

    // =====================================================
    // 🔥 DOCUMENT PICKER
    // =====================================================

    val documentPickerLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .GetContent()
        ) { uri: Uri? ->

            uri?.let {

                val mimeType =
                    context.contentResolver
                        .getType(uri)

                if (
                    mimeType !=
                    "application/pdf"
                ) {

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

    // =====================================================
    // 🔥 LOCATION PERMISSION
    // =====================================================

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(

            contract =
                ActivityResultContracts
                    .RequestPermission()

        ) { isGranted ->

            if (isGranted) {

                fetchProviderLocation(
                    context
                ) { lat, lng ->

                    latitude = lat
                    longitude = lng

                    // =================================
                    // 🔥 GEOCODER
                    // =================================

                    val locationData =

                        getAddressFromLocation(

                            context,

                            lat,

                            lng
                        )

                    city =
                        locationData.city

                    area =
                        locationData.area

                    fullAddress =
                        locationData.fullAddress
                }

            } else {

                Toast.makeText(

                    context,

                    "Location permission denied",

                    Toast.LENGTH_SHORT

                ).show()
            }
        }

    // =====================================================
    // 🔥 MAIN UI
    // =====================================================

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .background(Color.White)
                .statusBarsPadding()
                .padding(16.dp)
    ) {

        Text(

            text =
                "Apply as Service Provider",

            style =
                MaterialTheme
                    .typography
                    .headlineMedium,

            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        // =================================================
        // 🔥 DOCUMENT
        // =================================================

        Text(
            "Verification Document (PDF only)"
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Button(

            onClick = {

                documentPickerLauncher.launch(
                    "application/pdf"
                )
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(50.dp),

            shape =
                RoundedCornerShape(12.dp),

            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                )
        ) {

            Text(
                "Upload Verification Document"
            )
        }

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        verificationDocUri?.let {

            Text("✅ PDF Selected")
        }

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        // =================================================
        // 🔥 INPUT FIELDS
        // =================================================

        OutlinedTextField(

            value = name,

            onValueChange = {
                name = it
            },

            label = {
                Text("Full Name")
            },

            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        OutlinedTextField(

            value = serviceType,

            onValueChange = {
                serviceType = it
            },

            label = {
                Text("Service Type")
            },

            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        OutlinedTextField(

            value = experience,

            onValueChange = {
                experience = it
            },

            label = {
                Text("Experience")
            },

            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        OutlinedTextField(

            value = description,

            onValueChange = {
                description = it
            },

            label = {
                Text("Description")
            },

            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        // =================================================
        // 📍 LOCATION BUTTON
        // =================================================

        Button(

            onClick = {

                if (

                    ContextCompat.checkSelfPermission(

                        context,

                        Manifest.permission
                            .ACCESS_FINE_LOCATION

                    ) == PackageManager.PERMISSION_GRANTED

                ) {

                    fetchProviderLocation(
                        context
                    ) { lat, lng ->

                        latitude = lat
                        longitude = lng

                        val locationData =

                            getAddressFromLocation(

                                context,

                                lat,

                                lng
                            )

                        city =
                            locationData.city

                        area =
                            locationData.area

                        fullAddress =
                            locationData.fullAddress
                    }

                } else {

                    locationPermissionLauncher.launch(
                        Manifest.permission
                            .ACCESS_FINE_LOCATION
                    )
                }
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(50.dp),

            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                )
        ) {

            Text(
                "Get Current Location"
            )
        }

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        Text("📍 City: $city")

        Text("📍 Area: $area")

        Text("📍 Address: $fullAddress")

        Spacer(
            modifier =
                Modifier.height(30.dp)
        )

        // =================================================
        // 🚀 SUBMIT BUTTON
        // =================================================

        Button(

            onClick = {

                if (

                    name.isBlank() ||

                    serviceType.isBlank() ||

                    experience.isBlank()

                ) {

                    Toast.makeText(

                        context,

                        "Fill all required fields",

                        Toast.LENGTH_SHORT

                    ).show()

                    return@Button
                }

                if (
                    verificationDocUri == null
                ) {

                    Toast.makeText(

                        context,

                        "Upload PDF document",

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
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),

            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                )
        ) {

            if (isSubmitting) {

                CircularProgressIndicator(
                    color = Color.White
                )

            } else {

                Text(
                    "Submit Application"
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(40.dp)
        )
    }
}

// =========================================================
// 🔥 LOCATION FETCHER
// =========================================================

@SuppressLint("MissingPermission")
fun fetchProviderLocation(

    context: Context,

    onResult: (
        Double,
        Double
    ) -> Unit
) {

    val fusedLocationClient =

        LocationServices
            .getFusedLocationProviderClient(
                context
            )

    fusedLocationClient
        .lastLocation

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