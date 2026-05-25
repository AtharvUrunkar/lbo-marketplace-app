package com.example.lbo_marketplace.ui.screens.provider

import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.lbo_marketplace.R
import com.example.lbo_marketplace.auth.AuthViewModel
import com.example.lbo_marketplace.booking.BookingViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Provider Header (mirrors user Header)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ProviderHeader(
    onMenuClick: () -> Unit,
    menuExpanded: Boolean,
    onDismissMenu: () -> Unit,
    onAboutClick: () -> Unit,
    onFAQClick: () -> Unit,
    onHelpClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(48.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.height(40.dp),
                contentScale = ContentScale.Fit
            )
        }
        Box(contentAlignment = Alignment.TopEnd) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = onDismissMenu,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.background(Color.White)
            ) {
                DropdownMenuItem(
                    text = { Text("About", fontWeight = FontWeight.Bold) },
                    onClick = { onDismissMenu(); onAboutClick() }
                )
                DropdownMenuItem(
                    text = { Text("FAQ", fontWeight = FontWeight.Bold) },
                    onClick = { onDismissMenu(); onFAQClick() }
                )
                DropdownMenuItem(
                    text = { Text("Help", color = Color(0xFF6C63FF), fontWeight = FontWeight.Bold) },
                    onClick = { onDismissMenu(); onHelpClick() }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Provider Home / Dashboard Tab
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ProviderHomeTab(
    bookingViewModel: BookingViewModel = viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val scrollState = rememberScrollState()
    val uriHandler = LocalUriHandler.current

    // Header menu state
    var menuExpanded by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showFAQDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showTopRatedPopup by remember { mutableStateOf(false) }

    // Load provider bookings once
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { bookingViewModel.loadProviderBookings(it) }
    }

    val allBookings = bookingViewModel.providerBookings
    val pendingBookings = allBookings.filter {
        it["status"] == "PENDING" || it["status"] == "CONFIRMED"
    }

    val bannerItems = remember {
        listOf(
            ProviderBannerItem("Grow Your Business", localImageRes = R.drawable.logo),
            ProviderBannerItem("Serve Your Community", localVideoRes = R.raw.logo, isVideo = true),
            ProviderBannerItem("Top Rated Providers", localImageRes = R.drawable.logo)
        )
    }

    // ── Sticky top-bar ──
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        ProviderHeader(
            onMenuClick = { menuExpanded = true },
            menuExpanded = menuExpanded,
            onDismissMenu = { menuExpanded = false },
            onAboutClick = { showAboutDialog = true },
            onFAQClick = { showFAQDialog = true },
            onHelpClick = { showHelpDialog = true }
        )

        // ── Scrollable content ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            // Marketing Banner
            ProviderBannerSlider(bannerItems)

            Spacer(modifier = Modifier.height(24.dp))

            // Top Rated CTA (same style as user)
            Button(
                onClick = { showTopRatedPopup = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD9D9D9),
                    contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text("TOP RATED OF THIS WEEK", fontWeight = FontWeight.ExtraBold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Welcome / Greeting
            Text(
                "👷 Active Requests",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Review and manage incoming bookings from customers.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Pending/Active Booking Cards
            if (pendingBookings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", fontSize = 52.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No active requests right now",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "New booking requests will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                pendingBookings.forEach { booking ->
                    UpdatesBookingCard(
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
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // ── Dialogs ──
    if (showTopRatedPopup) {
        ProviderTopRatedPopup(onClose = { showTopRatedPopup = false })
    }
    if (showAboutDialog) ProviderMenuDialog(
        "About LBO",
        "LBO – Together We Grow 🤝\n\nConnecting local experts with our community seamlessly.",
        { showAboutDialog = false }
    )
    if (showFAQDialog) ProviderMenuDialog(
        "FAQ",
        "Q: How do I get bookings?\nA: Customers will find and book you.\n\nQ: How are payments handled?\nA: Customers pay you directly.",
        { showFAQDialog = false }
    )
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Help & Support", fontWeight = FontWeight.Bold) },
            text = { Text("If you have doubts, contact the admin or a referred person.") },
            confirmButton = {
                Button(
                    onClick = {
                        showHelpDialog = false
                        uriHandler.openUri("mailto:lbo.org.ask@gmail.com")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) { Text("Contact") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
                ) { Text("Close") }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top-Rated Popup (provider context)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ProviderTopRatedPopup(onClose: () -> Unit) {
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            tonalElevation = 0.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Top Rated This Week",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Keep delivering quality work to earn a spot here!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(32.dp))
                // Illustrative tip card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("⭐ How to get top rated?", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Respond to bookings quickly\n• Deliver quality service\n• Collect 5-star reviews from customers", color = Color.DarkGray, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Close") }
            }
        }
    }
}

@Composable
fun ProviderMenuDialog(title: String, content: String, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(content) },
        confirmButton = {
            TextButton(
                onClick = onClose,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
            ) { Text("Close") }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Banner
// ─────────────────────────────────────────────────────────────────────────────

data class ProviderBannerItem(
    val title: String,
    val localImageRes: Int? = null,
    val localVideoRes: Int? = null,
    val isVideo: Boolean = false
)

@Composable
fun ProviderBannerSlider(items: List<ProviderBannerItem>) {
    if (items.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { items.size })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        val flipDelay = if (items[pagerState.currentPage].isVideo) 10000L else 3500L
        delay(flipDelay)
        coroutineScope.launch {
            pagerState.animateScrollToPage((pagerState.currentPage + 1) % items.size)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val item = items[page]
            var videoFailed by remember { mutableStateOf(false) }
            if (item.isVideo && !videoFailed && item.localVideoRes != null) {
                ProviderVideoBanner(
                    localRes = item.localVideoRes,
                    isActive = pagerState.currentPage == page,
                    onError = { videoFailed = true }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = item.localImageRes ?: R.drawable.logo),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = item.title,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(24.dp),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        // Page dots
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(items.size) { i ->
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (pagerState.currentPage == i) Color.Black
                            else Color.Black.copy(alpha = 0.2f)
                        )
                        .size(10.dp)
                        .clickable {
                            coroutineScope.launch { pagerState.animateScrollToPage(i) }
                        }
                )
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun ProviderVideoBanner(localRes: Int, isActive: Boolean, onError: () -> Unit) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val uri = android.net.Uri.parse("android.resource://${context.packageName}/$localRes")
            setMediaItem(MediaItem.fromUri(uri))
            playWhenReady = true
            prepare()
            addListener(object : Player.Listener {
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) { onError() }
            })
        }
    }
    LaunchedEffect(isActive) { if (isActive) exoPlayer.play() else exoPlayer.pause() }
    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }
    AndroidView(
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                useController = false
                setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(android.graphics.Color.WHITE)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}