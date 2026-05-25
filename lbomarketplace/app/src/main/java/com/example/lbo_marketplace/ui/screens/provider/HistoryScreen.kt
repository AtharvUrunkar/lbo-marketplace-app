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
            .statusBarsPadding()
    ) {
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

        Divider(color = Color(0xFFEEEEEE))

        if (sorted.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No bookings yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Accepted and completed jobs will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sorted) { booking ->
                    UpdatesBookingCard(
                        booking = booking,
                        onAccept = {
                            val bookingId = booking["bookingId"] as? String
                            val providerId = booking["providerId"] as? String
                            val customerId = booking["customerId"] as? String
                            if (bookingId != null && providerId != null) {
                                bookingViewModel.updateBookingStatus(
                                    bookingId, "CONFIRMED", providerId, customerId
                                )
                            }
                        },
                        onReject = {
                            val bookingId = booking["bookingId"] as? String
                            val providerId = booking["providerId"] as? String
                            val customerId = booking["customerId"] as? String
                            if (bookingId != null && providerId != null) {
                                bookingViewModel.updateBookingStatus(
                                    bookingId, "REJECTED", providerId, customerId
                                )
                            }
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun UpdatesBookingCard(
    booking: Map<String, Any>,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val customerName  = booking["customerName"]       as? String ?: "Unknown Customer"
    val customerPhone = booking["customerPhone"]      as? String ?: "N/A"
    val problemTitle  = booking["problemTitle"]       as? String ?: "Service Request"
    val problemDesc   = booking["problemDescription"] as? String ?: ""
    val address       = booking["address"]            as? String ?: "No Address"
    val preferredDate = booking["preferredDate"]      as? String ?: "N/A"
    val preferredTime = booking["preferredTime"]      as? String ?: "N/A"
    val status        = booking["status"]             as? String ?: "PENDING"
    val rating        = (booking["rating"] as? Number)?.toFloat()
    val feedback      = booking["feedback"] as? String

    val statusColor = when (status) {
        "PENDING"   -> Color(0xFFFFA000)  // Amber
        "CONFIRMED" -> Color(0xFF388E3C)  // Green
        "COMPLETED" -> Color(0xFF1976D2)  // Blue
        "REJECTED"  -> Color(0xFFD32F2F)  // Red
        else        -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Customer row ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEFEFEF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Customer",
                        tint = Color.DarkGray
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = customerPhone,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = status,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(12.dp))

            // ── Service details ──
            Text(
                text = problemTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            if (problemDesc.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = problemDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "📍 $address",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )
            Text(
                text = "📅 $preferredDate at $preferredTime",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )

            // ── Action buttons for PENDING ──
            if (status == "PENDING") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Reject
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFD32F2F)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp, Color(0xFFD32F2F)
                        )
                    ) {
                        Text("Reject", fontWeight = FontWeight.Bold)
                    }
                    // Accept
                    Button(
                        onClick = onAccept,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Accept", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ── Info chip for CONFIRMED ──
            if (status == "CONFIRMED") {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✅ Accepted – waiting for customer to mark as completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF388E3C),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // ── Review for COMPLETED ──
            if (status == "COMPLETED" && rating != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color(0xFFEEEEEE))
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { idx ->
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = if (idx < rating) Color(0xFFFFC107) else Color(0xFFE0E0E0),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$rating / 5.0",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                if (!feedback.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"$feedback\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}