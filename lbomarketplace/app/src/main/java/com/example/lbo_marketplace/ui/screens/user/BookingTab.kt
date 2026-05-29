package com.example.lbo_marketplace.ui.screens.user

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lbo_marketplace.booking.BookingViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun BookingTab(
    bookingViewModel: BookingViewModel,
    header: @Composable () -> Unit = {}
) {

    val currentUser =
        FirebaseAuth.getInstance().currentUser

    var selectedBookingForFeedback by remember {
        mutableStateOf<Map<String, Any>?>(null)
    }

    LaunchedEffect(currentUser) {

        if (currentUser != null) {

            bookingViewModel.loadCustomerBookings(
                currentUser.uid
            )
        }
    }

    val bookings =
        bookingViewModel.customerBookings

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.White)

    ) {

        // Sticky/Movable Header integration
        header()

        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)

        ) {

            Text(

                text = "My Bookings",

                style =
                    MaterialTheme
                        .typography
                        .titleLarge,

                fontWeight =
                    FontWeight.Bold,

                color = Color.Black
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            if (bookings.isEmpty()) {

                Box(

                    modifier =
                        Modifier.fillMaxSize(),

                    contentAlignment =
                        Alignment.Center

                ) {

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(

                            text = "📅",

                            fontSize = 60.sp
                        )

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        Text(

                            text = "No bookings yet",

                            style =
                                MaterialTheme
                                    .typography
                                    .titleMedium,

                            fontWeight =
                                FontWeight.Bold,

                            color = Color.Black
                        )

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        Text(

                            text =
                                "Search and book local experts from the Home tab.",

                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium,

                            color = Color.Gray,

                            textAlign =
                                TextAlign.Center,

                            modifier =
                                Modifier.padding(
                                    horizontal = 24.dp
                                )
                        )
                    }
                }

            } else {

                LazyColumn(

                    verticalArrangement =
                        Arrangement.spacedBy(16.dp),

                    modifier =
                        Modifier.fillMaxSize()

                ) {

                    items(bookings) { booking ->

                        CustomerBookingCard(

                            booking = booking,

                            onCompleteClick = {
                                selectedBookingForFeedback = booking
                            },

                            onWithdrawClick = {

                                val bId =
                                    booking["bookingId"] as? String

                                val pId =
                                    booking["providerId"] as? String ?: ""

                                if (
                                    bId != null &&
                                    currentUser != null
                                ) {

                                    bookingViewModel.updateBookingStatus(
                                        bId,
                                        "WITHDRAWN",
                                        pId,
                                        currentUser.uid
                                    )
                                }
                            }
                        )
                    }

                    item {

                        Spacer(
                            modifier =
                                Modifier.height(40.dp)
                        )
                    }
                }
            }
        }
    }

    // Feedback rating dialog
    if (selectedBookingForFeedback != null) {

        FeedbackRatingDialog(

            booking =
                selectedBookingForFeedback!!,

            onDismiss = {
                selectedBookingForFeedback = null
            },

            onSubmit = { rating, feedback ->

                val bookingId =
                    selectedBookingForFeedback!!["bookingId"] as? String

                val providerId =
                    selectedBookingForFeedback!!["providerId"] as? String

                if (
                    bookingId != null &&
                    providerId != null &&
                    currentUser != null
                ) {

                    bookingViewModel.completeBookingWithFeedback(
                        bookingId = bookingId,
                        providerId = providerId,
                        customerId = currentUser.uid,
                        rating = rating,
                        feedback = feedback
                    )
                }

                selectedBookingForFeedback = null
            }
        )
    }
}

@Composable
fun CustomerBookingCard(
    booking: Map<String, Any>,
    onCompleteClick: () -> Unit,
    onWithdrawClick: () -> Unit
) {

    val providerName =
        booking["providerName"] as? String ?: "Expert Provider"

    val problemTitle =
        booking["problemTitle"] as? String ?: "Service Request"

    val problemDesc =
        booking["problemDescription"] as? String ?: ""

    val address =
        booking["address"] as? String ?: "N/A"

    val date =
        booking["preferredDate"] as? String ?: "TBD"

    val status =
        booking["status"] as? String ?: "PENDING"

    val rating =
        (booking["rating"] as? Number)?.toFloat()

    val feedback =
        booking["feedback"] as? String

    val statusColor =
        when (status) {

            "PENDING" -> Color(0xFFFFA000)      // Amber/Orange

            "CONFIRMED" -> Color(0xFF388E3C)    // Green

            "COMPLETED" -> Color(0xFF1976D2)    // Blue

            "REJECTED" -> Color(0xFFD32F2F)     // Red

            "WITHDRAWN" -> Color(0xFF757575)    // Gray (dimmed)

            else -> Color.Gray
        }

    var resolvedImageUrl by remember {
        mutableStateOf("")
    }

    LaunchedEffect(
        booking["providerId"],
        booking["providerUid"]
    ) {

        val pId =
            booking["providerId"] as? String ?: ""

        val pUid =
            booking["providerUid"] as? String ?: ""

        // 1. Check local session memory cache first
        val cachedUrl =
            if (pUid.isNotEmpty()) {
                com.example.lbo_marketplace.utils.UserProfileCache.getProfileImage(pUid)
            } else if (pId.isNotEmpty()) {
                com.example.lbo_marketplace.utils.UserProfileCache.getProfileImage(pId)
            } else {
                null
            }

        if (cachedUrl != null) {

            resolvedImageUrl = cachedUrl

        } else {

            // 2. Fetch if not in memory cache
            if (pUid.isNotEmpty()) {

                FirebaseFirestore.getInstance().collection("users").document(pUid).get()

                    .addOnSuccessListener { doc ->

                        val url =
                            doc.getString("profileImageUrl") ?: ""

                        if (url.isNotEmpty()) {

                            resolvedImageUrl = url

                            com.example.lbo_marketplace.utils.UserProfileCache.putProfileImage(pUid, url)
                        }
                    }
            }

            if (
                resolvedImageUrl.isEmpty() &&
                pId.isNotEmpty()
            ) {

                FirebaseFirestore.getInstance().collection("users").document(pId).get()

                    .addOnSuccessListener { doc ->

                        val url =
                            doc.getString("profileImageUrl") ?: ""

                        if (url.isNotEmpty()) {

                            resolvedImageUrl = url

                            com.example.lbo_marketplace.utils.UserProfileCache.putProfileImage(pId, url)

                        } else {

                            // Fallback to provider_requests
                            FirebaseFirestore.getInstance().collection("provider_requests").document(pId).get()

                                .addOnSuccessListener { doc2 ->

                                    val url2 =
                                        doc2.getString("profileImageUrl") ?: ""

                                    if (url2.isNotEmpty()) {

                                        resolvedImageUrl = url2

                                        com.example.lbo_marketplace.utils.UserProfileCache.putProfileImage(pId, url2)
                                    }
                                }
                        }
                    }
            }
        }
    }

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(16.dp),

        colors =
            CardDefaults.cardColors(

                containerColor =
                    Color(0xFFF9F9F9)
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 1.dp
            )

    ) {

        Column(

            modifier =
                Modifier.padding(16.dp)

        ) {

            Row(

                verticalAlignment =
                    Alignment.CenterVertically,

                modifier =
                    Modifier.fillMaxWidth()

            ) {

                Box(

                    modifier =
                        Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Color(0xFFEFEFEF)
                            ),

                    contentAlignment =
                        Alignment.Center

                ) {

                    if (resolvedImageUrl.isNotEmpty()) {

                        AsyncImage(

                            model = resolvedImageUrl,

                            contentDescription = "Provider Profile",

                            modifier =
                                Modifier.fillMaxSize(),

                            contentScale =
                                ContentScale.Crop
                        )

                    } else {

                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Provider",
                            tint = Color.DarkGray
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.width(12.dp)
                )

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(

                        text = providerName,

                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,

                        fontWeight =
                            FontWeight.Bold,

                        color = Color.Black
                    )

                    Text(

                        text = problemTitle,

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,

                        color = Color.Gray
                    )
                }

                Surface(

                    color =
                        statusColor.copy(
                            alpha = 0.1f
                        ),

                    shape =
                        RoundedCornerShape(16.dp)

                ) {

                    Text(

                        text = status,

                        color = statusColor,

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        fontWeight =
                            FontWeight.Bold,

                        modifier =
                            Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 5.dp
                            )
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            HorizontalDivider(
                color = Color(0xFFEEEEEE)
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            if (problemDesc.isNotEmpty()) {

                Text(

                    text = "Description:",

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    fontWeight =
                        FontWeight.Bold,

                    color = Color.Black
                )

                Text(

                    text = problemDesc,

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    color = Color.DarkGray
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )
            }

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(
                    modifier =
                        Modifier.width(6.dp)
                )

                Text(

                    text = address,

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color = Color.DarkGray
                )
            }

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(
                    modifier =
                        Modifier.width(6.dp)
                )

                Text(

                    text = "Preferred Date: $date",

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color = Color.DarkGray
                )
            }

            if (status == "PENDING") {

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                Button(

                    onClick = onWithdrawClick,

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp),

                    shape =
                        RoundedCornerShape(12.dp),

                    colors =
                        ButtonDefaults.buttonColors(

                            containerColor =
                                Color(0xFFEEEEEE),

                            contentColor =
                                Color.Black
                        )

                ) {

                    Text(
                        text = "Withdraw Request",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (status == "CONFIRMED") {

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                Button(

                    onClick = onCompleteClick,

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp),

                    shape =
                        RoundedCornerShape(12.dp),

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color.Black
                        )

                ) {

                    Text(
                        text = "Mark as Completed",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            if (
                status == "COMPLETED" &&
                rating != null
            ) {

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                HorizontalDivider(
                    color = Color(0xFFEEEEEE)
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    repeat(5) { index ->

                        val active =
                            index < rating

                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,

                            tint =
                                if (active) {
                                    Color(0xFFFFC107)
                                } else {
                                    Color(0xFFE0E0E0)
                                },

                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )

                    Text(

                        text = "$rating / 5.0",

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,

                        fontWeight =
                            FontWeight.Bold,

                        color = Color.Black
                    )
                }

                if (
                    !feedback.isNullOrBlank()
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(6.dp)
                    )

                    Text(

                        text = "\"$feedback\"",

                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,

                        fontStyle =
                            FontStyle.Italic,

                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackRatingDialog(
    booking: Map<String, Any>,
    onDismiss: () -> Unit,
    onSubmit: (Float, String) -> Unit
) {

    val providerName =
        booking["providerName"] as? String ?: "Expert"

    var rating by remember {
        mutableStateOf(5)
    }

    var feedbackText by remember {
        mutableStateOf("")
    }

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {

            Text(
                text = "Rate your service",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },

        text = {

            Column(

                horizontalAlignment =
                    Alignment.CenterHorizontally,

                modifier =
                    Modifier.fillMaxWidth()

            ) {

                Text(
                    text = "How was your experience with $providerName?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                // Star rating row
                Row(

                    horizontalArrangement =
                        Arrangement.Center,

                    modifier =
                        Modifier.fillMaxWidth()

                ) {

                    repeat(5) { index ->

                        val starRating =
                            index + 1

                        val active =
                            starRating <= rating

                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star $starRating",

                            tint =
                                if (active) {
                                    Color(0xFFFFC107)
                                } else {
                                    Color(0xFFE0E0E0)
                                },

                            modifier =
                                Modifier
                                    .size(36.dp)
                                    .clickable {
                                        rating = starRating
                                    }
                                    .padding(4.dp)
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                OutlinedTextField(

                    value = feedbackText,

                    onValueChange = {
                        feedbackText = it
                    },

                    placeholder = {
                        Text("Write your feedback here...")
                    },

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp),

                    shape =
                        RoundedCornerShape(12.dp),

                    colors =
                        OutlinedTextFieldDefaults.colors(

                            focusedBorderColor =
                                Color.Black,

                            unfocusedBorderColor =
                                Color.LightGray,

                            focusedLabelColor =
                                Color.Black
                        )
                )
            }
        },

        confirmButton = {

            Button(

                onClick = {
                    onSubmit(
                        rating.toFloat(),
                        feedbackText
                    )
                },

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    ),

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp),

                shape =
                    RoundedCornerShape(12.dp)

            ) {

                Text(
                    text = "Submit Review",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },

        dismissButton = {

            TextButton(

                onClick = onDismiss,

                modifier =
                    Modifier.fillMaxWidth()

            ) {

                Text(
                    text = "Cancel",
                    color = Color.Black
                )
            }
        },

        containerColor = Color.White,

        shape = RoundedCornerShape(24.dp)
    )
}
