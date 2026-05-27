package com.example.lbo_marketplace.ui.screens.user

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
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
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lbo_marketplace.R
import com.example.lbo_marketplace.auth.AuthViewModel
import com.example.lbo_marketplace.auth.ProviderViewModel
import com.example.lbo_marketplace.booking.BookingViewModel
import com.example.lbo_marketplace.ui.screens.user.chat.ChatScreen
import com.google.firebase.auth.FirebaseAuth
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream

@Composable
fun UserMainScreen(
    authViewModel: AuthViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedProviderId by remember { mutableStateOf<String?>(null) }
    var viewingProviderId by remember { mutableStateOf<String?>(null) }
    var showApplyScreen by remember { mutableStateOf(false) }
    var showChatScreen by remember { mutableStateOf(false) }

    var menuExpanded by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showFAQDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val providerViewModel: ProviderViewModel = viewModel()
    val bookingViewModel: BookingViewModel = viewModel()
    val user = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    @Composable
    fun GlobalHeader() {
        Header(
            onMenuClick = { menuExpanded = true },
            menuExpanded = menuExpanded,
            onDismissMenu = { menuExpanded = false },
            onAboutClick = { showAboutDialog = true },
            onFAQClick = { showFAQDialog = true },
            onHelpClick = { showHelpDialog = true }
        )
    }

    if (showChatScreen) {
        BackHandler(onBack = { showChatScreen = false })
        ChatScreen(onBack = { showChatScreen = false })
        return
    }

    if (viewingProviderId != null) {
        val provider = providerViewModel.providers.find { it.id == viewingProviderId }
        if (provider != null) {
            ProviderDetailsScreen(
                provider = provider,
                onBack = { viewingProviderId = null },
                onBookNow = {
                    selectedProviderId = viewingProviderId
                    viewingProviderId = null
                }
            )
            return
        }
    }

    if (selectedProviderId != null) {
        val selectedProvider = providerViewModel.providers.find { it.id == selectedProviderId }
        val pName = selectedProvider?.name ?: "Provider"

        BookingScreen(
            providerId = selectedProviderId!!,
            onBack = { selectedProviderId = null },
            onSubmit = { problem, address, date, contact ->
                user?.let {
                    bookingViewModel.book(
                        customerId = it.uid,
                        customerName = it.displayName ?: "User",
                        customerPhone = contact,
                        providerId = selectedProviderId!!,
                        providerName = pName,
                        problemTitle = "Service Request",
                        problemDescription = problem,
                        address = address,
                        preferredDate = date,
                        preferredTime = "TBD",
                        onSuccess = { selectedProviderId = null }
                    )
                }
            }
        )
        return
    }

    if (showApplyScreen) {
        BackHandler(onBack = { showApplyScreen = false })
        ApplyProviderScreen {
                name: String,
                serviceType: String,
                description: String,
                experience: String,
                lat: Double,
                lng: Double,
                city: String,
                area: String,
                fullAddress: String,
                verificationDocUri: Uri ->
            user?.let {
                providerViewModel.applyWithDetails(
                    context = context,
                    userId = it.uid,
                    email = it.email ?: "",
                    name = name,
                    serviceType = serviceType,
                    description = description,
                    experience = experience,
                    latitude = lat,
                    longitude = lng,
                    city = city,
                    area = area,
                    fullAddress = fullAddress,
                    verificationDocUri = verificationDocUri
                )
            }
            showApplyScreen = false
        }
        return
    }

    Scaffold(
        containerColor = Color.White,
        contentColor = Color.Black,
        topBar = {
            if (selectedTab == 0) {
                GlobalHeader()
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Notifications, null) },
                    label = { Text("Community") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, null) },
                    label = { Text("Bookings") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Profile") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showChatScreen = true },
                shape = CircleShape,
                containerColor = Color(0xFF6C63FF),
                contentColor = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            ) { Icon(Icons.Default.Chat, contentDescription = "AI Chat Assistant") }
        }
    ) { padding ->
        val contentPadding = if (selectedTab == 0) padding else PaddingValues(bottom = padding.calculateBottomPadding())

        Box(modifier = Modifier.padding(contentPadding).background(Color.White)) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300)) },
                label = ""
            ) { targetTab ->
                when (targetTab) {
                    0 -> HomeTab(onBookClick = { viewingProviderId = it })
                    1 -> CommunityTab(header = { GlobalHeader() })
                    2 -> BookingTab(bookingViewModel = bookingViewModel, header = { GlobalHeader() })
                    3 -> ProfileTab(authViewModel = authViewModel, onApplyClick = { showApplyScreen = true }, header = { GlobalHeader() })
                }
            }
        }
    }

    if (showAboutDialog) GlobalMenuDialog(
        "About LBO",
        "LBO – Together We Grow 🤝\n\nConnecting local experts with our community seamlessly.",
        { showAboutDialog = false }
    )
    if (showFAQDialog) GlobalMenuDialog(
        "FAQ",
        "Q: How do I book?\nA: Search and click 'Book Now'.\n\nQ: Is it free?\nA: App is free; pay the provider directly.",
        { showFAQDialog = false }
    )
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Help & Support", fontWeight = FontWeight.Bold) },
            text = { Text("If you have some doubts contact admin or refered person") },
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

@Composable
fun Header(
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
            modifier = Modifier.weight(1f).height(50.dp),
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
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null) }
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

@Composable
fun GlobalMenuDialog(title: String, content: String, onClose: () -> Unit) {
<<<<<<< HEAD
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

@Composable
fun ProviderDetailsScreen(
    provider: com.example.lbo_marketplace.data.model.Provider,
    onBack: () -> Unit,
    onBookNow: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xFFF8F8F8)),
            contentAlignment = Alignment.Center
        ) {
            val imageUrl = provider.profileImage ?: provider.profileImageUrl
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                val initials = provider.name.take(2).uppercase()
                Text(
                    initials,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            provider.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black
        )
        Text(provider.serviceType, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)

        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("${provider.rating} / 5.0", fontWeight = FontWeight.Bold, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color(0xFFEEEEEE))
        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Text("EXPERIENCE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
            Text(provider.experience, style = MaterialTheme.typography.bodyLarge, color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))
            Text("ABOUT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
            Text(
                provider.description.ifBlank { "No description provided." },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
        }

        Button(
            onClick = onBookNow,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Book Now", fontWeight = FontWeight.Bold)
        }
    }

    AlertDialog(onDismissRequest = onClose, title = { Text(title, fontWeight = FontWeight.Bold) }, text = { Text(content) }, confirmButton = { TextButton(onClick = onClose, colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)) { Text("Close") } }, shape = RoundedCornerShape(24.dp), containerColor = Color.White)

}