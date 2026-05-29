package com.example.lbo_marketplace.ui.screens.user

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
 * - Beautiful Material 3 Calendar Date Picker dialog for easy selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    providerId: String,
    onBack: () -> Unit,
    onSubmit: (String, String, String, String) -> Unit
) {
    // Intercept device back button
    BackHandler(onBack = onBack)

    var problem by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var preferredDate by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                            preferredDate = sdf.format(java.util.Date(selectedMillis))
                        }
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
                ) {
                    Text("Cancel")
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = Color.White,
                titleContentColor = Color.Black,
                headlineContentColor = Color.Black,
                navigationContentColor = Color.Black
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    headlineContentColor = Color.Black,
                    weekdayContentColor = Color.DarkGray,
                    navigationContentColor = Color.Black,
                    yearContentColor = Color.Black,
                    selectedYearContentColor = Color.White,
                    selectedYearContainerColor = Color.Black,
                    dayContentColor = Color.Black,
                    selectedDayContainerColor = Color.Black,
                    selectedDayContentColor = Color.White,
                    todayDateBorderColor = Color.Black,
                    todayContentColor = Color.Black
                )
            )
        }
    }

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

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
        ) {
            OutlinedTextField(
                value = preferredDate,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text("Preferred Date") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select Date",
                        tint = Color.Black
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = Color.Gray,
                    disabledLabelColor = Color.Black,
                    disabledTrailingIconColor = Color.Black
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = contactPhone,
            onValueChange = { contactPhone = it },
            label = { Text("Your Contact Number") },
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
                if (problem.isNotEmpty() && address.isNotEmpty() && preferredDate.isNotEmpty() && contactPhone.isNotEmpty()) {
                    onSubmit(problem, address, preferredDate, contactPhone)
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