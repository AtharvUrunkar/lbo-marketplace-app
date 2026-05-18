package com.example.lbo_marketplace.ui.screens.user

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Booking Screen where users provide problem details and address.
 * 
 * FIXES:
 * - Set Submit Request button to FULL BLACK (#000000).
 * - Guaranteed PURE WHITE (#FFFFFF) background.
 * - Ensures status bar padding and back navigation are correct.
 */
@Composable
fun BookingScreen(
    providerId: String,
    onBack: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    // Intercept device back button
    BackHandler(onBack = onBack)

    var problem by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // ✅ PURE WHITE
            .statusBarsPadding()
            .padding(16.dp)
    ) {

        Text(
            text = "Book Service",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = problem,
            onValueChange = { problem = it },
            label = { Text("Describe your problem") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Enter your address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (problem.isNotEmpty() && address.isNotEmpty()) {
                    onSubmit(problem, address)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black) // ✅ FULL BLACK
        ) {
            Text("Submit Request", style = MaterialTheme.typography.titleMedium, color = Color.White)
        }
    }
}