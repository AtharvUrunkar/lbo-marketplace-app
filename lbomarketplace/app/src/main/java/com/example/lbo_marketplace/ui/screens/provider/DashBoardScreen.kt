package com.example.lbo_marketplace.ui.screens.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lbo_marketplace.booking.BookingViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun DashboardScreen(
    bookingViewModel: BookingViewModel = viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            bookingViewModel.loadProviderBookings(currentUser.uid)
        }
    }

    val allBookings = bookingViewModel.providerBookings
    val activeBookings = allBookings.filter { it["status"] == "PENDING" || it["status"] == "CONFIRMED" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (activeBookings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active bookings", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(activeBookings) { booking ->
                    DashboardBookingCard(
                        booking = booking,
                        onAccept = {
                            val bookingId = booking["bookingId"] as? String
                            val providerId = booking["providerId"] as? String
                            val customerId = booking["customerId"] as? String
                            if (bookingId != null && providerId != null) {
                                bookingViewModel.updateBookingStatus(bookingId, "CONFIRMED", providerId, customerId)
                            }
                        },
                        onReject = {
                            val bookingId = booking["bookingId"] as? String
                            val providerId = booking["providerId"] as? String
                            val customerId = booking["customerId"] as? String
                            if (bookingId != null && providerId != null) {
                                bookingViewModel.updateBookingStatus(bookingId, "REJECTED", providerId, customerId)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardBookingCard(
    booking: Map<String, Any>,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val customerName = booking["customerName"] as? String ?: "Unknown"
    val customerPhone = booking["customerPhone"] as? String ?: "N/A"
    val problemTitle = booking["problemTitle"] as? String ?: "No Title"
    val problemDesc = booking["problemDescription"] as? String ?: ""
    val address = booking["address"] as? String ?: "No Address"
    val preferredDate = booking["preferredDate"] as? String ?: "N/A"
    val preferredTime = booking["preferredTime"] as? String ?: "N/A"
    val status = booking["status"] as? String ?: "PENDING"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Customer", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = customerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = customerPhone, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                
                val statusColor = when (status) {
                    "PENDING" -> Color(0xFFFFA000)
                    "CONFIRMED" -> Color(0xFF388E3C)
                    else -> Color.Gray
                }
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = status,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Problem: $problemTitle", fontWeight = FontWeight.SemiBold)
            Text(text = problemDesc, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Address: $address", style = MaterialTheme.typography.bodySmall)
            Text(text = "Preferred Date: $preferredDate at $preferredTime", style = MaterialTheme.typography.bodySmall)
            
            if (status == "PENDING") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onReject,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Reject", color = Color.White)
                    }
                    Button(
                        onClick = onAccept,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Accept", color = Color.White)
                    }
                }
            } else if (status == "CONFIRMED") {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Waiting for customer to mark as completed.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}