package com.example.lbo_marketplace.ui.screens.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lbo_marketplace.booking.BookingViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * Updates Screen (renamed from History).
 *
 * Shows ALL bookings for the provider:
 *  • PENDING   – accept / reject buttons
 *  • CONFIRMED – "Waiting for customer" chip
 *  • COMPLETED – star rating + feedback review
 *  • REJECTED  – dimmed rejection chip
 */
@Composable
fun UpdatesScreen(
    header: @Composable () -> Unit,
    bookingViewModel: BookingViewModel = viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { bookingViewModel.loadProviderBookings(it) }
    }

    val allBookings = bookingViewModel.providerBookings
    // Sort newest first by createdAt timestamp
    val sorted = allBookings.sortedByDescending { (it["createdAt"] as? Number)?.toLong() ?: 0L }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Render custom header at the top
        header()

        // ── Page Header ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Text(
                text = "Updates",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
            Text(
                text = "All your booking requests & job history.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        HorizontalDivider(color = Color(0xFFEEEEEE))

        if (sorted.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No booking updates yet.",
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sorted, key = { it["bookingId"] as? String ?: it.hashCode().toString() }) { booking ->
                    UpdateBookingItem(
                        booking = booking,
                        onAccept = {
                            val bId = booking["bookingId"] as? String
                            val pId = booking["providerId"] as? String
                            val cId = booking["customerId"] as? String
                            if (bId != null && pId != null) {
                                bookingViewModel.updateBookingStatus(bId, "CONFIRMED", pId, cId)
                            }
                        },
                        onReject = {
                            val bId = booking["bookingId"] as? String
                            val pId = booking["providerId"] as? String
                            val cId = booking["customerId"] as? String
                            if (bId != null && pId != null) {
                                bookingViewModel.updateBookingStatus(bId, "REJECTED", pId, cId)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun UpdateBookingItem(
    booking: Map<String, Any>,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val customerName = booking["customerName"] as? String ?: "Client"
    val customerPhone = booking["customerPhone"] as? String ?: "N/A"
    val problemTitle = booking["problemTitle"] as? String ?: "General Service"
    val problemDesc = booking["problemDescription"] as? String ?: ""
    val address = booking["address"] as? String ?: "No Address Details"
    val preferredDate = booking["preferredDate"] as? String ?: "N/A"
    val preferredTime = booking["preferredTime"] as? String ?: "N/A"
    val status = booking["status"] as? String ?: "PENDING"

    val rating = (booking["rating"] as? Number)?.toFloat()
    val feedback = booking["feedback"] as? String

    val statusBg = when (status) {
        "PENDING" -> Color(0xFFFFF9C4)     // Soft yellow
        "CONFIRMED" -> Color(0xFFC8E6C9)   // Soft green
        "COMPLETED" -> Color(0xFFBBDEFB)   // Soft blue
        else -> Color(0xFFFFCDD2)          // Soft red
    }
    val statusText = when (status) {
        "PENDING" -> Color(0xFFFBC02D)
        "CONFIRMED" -> Color(0xFF388E3C)
        "COMPLETED" -> Color(0xFF1976D2)
        else -> Color(0xFFD32F2F)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Customer Info + Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = customerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Phone: $customerPhone",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = status,
                        color = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body: Service Requested details
            Text(
                text = problemTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            if (problemDesc.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = problemDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(12.dp))

            // Footer Details: Address and Timing
            Text(
                text = "📍 $address",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "📅 Timing: $preferredDate at $preferredTime",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            // Dynamic Action Bottom Row based on booking status
            when (status) {
                "PENDING" -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onReject,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCDD2), contentColor = Color(0xFFD32F2F))
                        ) {
                            Text("Reject", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onAccept,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                        ) {
                            Text("Accept", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                "CONFIRMED" -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Waiting for customer to mark job completed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                "COMPLETED" -> {
                    if (rating != null || feedback != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${rating ?: 0.0}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                                if (!feedback.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "\"$feedback\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.DarkGray,
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}