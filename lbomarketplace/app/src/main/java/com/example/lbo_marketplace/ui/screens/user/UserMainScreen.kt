package com.example.lbo_marketplace.ui.screens.user

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.clickable
import android.net.Uri
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
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
import com.example.lbo_marketplace.auth.AuthState
import com.example.lbo_marketplace.auth.ProviderViewModel
import com.example.lbo_marketplace.booking.BookingViewModel
import com.example.lbo_marketplace.ui.screens.user.chat.ChatScreen
import com.google.firebase.auth.FirebaseAuth
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream
import android.view.ViewGroup
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.draw.shadow
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout


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
    val authState = authViewModel.authState.value
    val isProvider = authState is AuthState.Authenticated && authState.role == "SERVICE_PROVIDER"
    var customerName by remember { mutableStateOf("") }
    var customerProfileImageUrl by remember { mutableStateOf("") }
    var hasCheckedProfileImageOnboarding by remember { mutableStateOf(false) }
    var showProfileImageOnboardingDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(customerProfileImageUrl, user?.uid) {
        user?.uid?.let { uid ->
            val prefs = context.getSharedPreferences("lbo_prefs", android.content.Context.MODE_PRIVATE)
            val isNewlyRegistered = prefs.getBoolean("newly_registered_$uid", false)
            if (!hasCheckedProfileImageOnboarding && customerProfileImageUrl == "" && isNewlyRegistered) {
                showProfileImageOnboardingDialog = true
                hasCheckedProfileImageOnboarding = true
                prefs.edit().putBoolean("newly_registered_$uid", false).apply()
            }
        }
    }

    var userLat by remember { mutableStateOf<Double?>(null) }
    var userLng by remember { mutableStateOf<Double?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            com.example.lbo_marketplace.utils.fetchProviderLocation(context) { lat, lng ->
                if (lat != 0.0 && lng != 0.0) {
                    userLat = lat
                    userLng = lng
                } else {
                    com.example.lbo_marketplace.utils.fallbackToAddressLocation(
                        userId = user?.uid,
                        authViewModel = authViewModel,
                        context = context
                    ) { fallbackLat, fallbackLng ->
                        userLat = fallbackLat
                        userLng = fallbackLng
                    }
                }
            }
        } else {
            com.example.lbo_marketplace.utils.fallbackToAddressLocation(
                userId = user?.uid,
                authViewModel = authViewModel,
                context = context
            ) { fallbackLat, fallbackLng ->
                userLat = fallbackLat
                userLng = fallbackLng
            }
        }
    }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        customerName = snapshot.getString("name") ?: ""
                        customerProfileImageUrl = snapshot.getString("profileImageUrl") ?: ""
                    }
                }
        }
    }

    LaunchedEffect(user?.uid) {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            com.example.lbo_marketplace.utils.fetchProviderLocation(context) { lat, lng ->
                if (lat != 0.0 && lng != 0.0) {
                    userLat = lat
                    userLng = lng
                } else {
                    com.example.lbo_marketplace.utils.fallbackToAddressLocation(
                        userId = user?.uid,
                        authViewModel = authViewModel,
                        context = context
                    ) { fallbackLat, fallbackLng ->
                        userLat = fallbackLat
                        userLng = fallbackLng
                    }
                }
            }
        } else {
            com.example.lbo_marketplace.utils.fallbackToAddressLocation(
                userId = user?.uid,
                authViewModel = authViewModel,
                context = context
            ) { fallbackLat, fallbackLng ->
                userLat = fallbackLat
                userLng = fallbackLng
            }
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    @Composable
    fun GlobalHeader() {
        Header(
            profileImageUrl = customerProfileImageUrl,
            onProfileClick = { selectedTab = 3 },
            onMenuClick = { menuExpanded = true },
            menuExpanded = menuExpanded,
            onDismissMenu = { menuExpanded = false },
            onAboutClick = { showAboutDialog = true },
            onFAQClick = { showFAQDialog = true },
            onHelpClick = { showHelpDialog = true },
            isProvider = isProvider,
            onSwitchToProviderClick = {
                authViewModel.isProviderInCustomerMode.value = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.White,
            contentColor = Color.Black,
            topBar = {
                GlobalHeader()
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
                ChatbotVideoButton(
                    onClick = { showChatScreen = true },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).background(Color.White)) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = { fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300)) },
                    label = ""
                ) { targetTab ->
                    when (targetTab) {
                        0 -> HomeTab(
                            onBookClick = { viewingProviderId = it },
                            authViewModel = authViewModel
                        )
                        1 -> CommunityTab(header = {})
                        2 -> BookingTab(bookingViewModel = bookingViewModel, header = {})
                        3 -> ProfileTab(authViewModel = authViewModel, onApplyClick = { showApplyScreen = true }, header = {})
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

        if (showProfileImageOnboardingDialog) {
            AlertDialog(
                onDismissRequest = { showProfileImageOnboardingDialog = false },
                title = {
                    Text(
                        text = "Welcome to LBO!",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                text = {
                    Text(
                        text = "Let's personalize your account! Take a moment to setup your profile and upload a clean profile picture.",
                        color = Color.DarkGray
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showProfileImageOnboardingDialog = false
                            selectedTab = 3 // Redirects straight to the Profile Tab!
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                    ) {
                        Text("Setup Now", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showProfileImageOnboardingDialog = false }
                    ) {
                        Text("Later", color = Color.Black, fontWeight = FontWeight.Medium)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(20.dp)
            )
        }

        // Overlay Screens
        if (showChatScreen) {
            BackHandler(onBack = { showChatScreen = false })
            ChatScreen(onBack = { showChatScreen = false })
        }

        if (viewingProviderId != null) {
            val provider = providerViewModel.providers.find { it.id == viewingProviderId }
            if (provider != null) {
                BackHandler(onBack = { viewingProviderId = null })
                ProviderDetailsScreen(
                    provider = provider,
                    userLat = userLat,
                    userLng = userLng,
                    onBack = { viewingProviderId = null },
                    onBookNow = {
                        selectedProviderId = viewingProviderId
                        viewingProviderId = null
                    }
                )
            }
        }

        if (selectedProviderId != null) {
            val selectedProvider = providerViewModel.providers.find { it.id == selectedProviderId }
            val pName = selectedProvider?.name ?: "Provider"

            BackHandler(onBack = { selectedProviderId = null })
            BookingScreen(
                providerId = selectedProviderId!!,
                onBack = { selectedProviderId = null },
                onSubmit = { problem, address, date, contact ->
                    user?.let {
                        val finalCustomerName = customerName.ifBlank { it.displayName ?: "User" }
                        bookingViewModel.book(
                            customerId = it.uid,
                            customerName = finalCustomerName,
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
                    verificationDocUri: Uri,
                    profilePhotoUri: Uri? ->

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
                        verificationDocUri = verificationDocUri,
                        profilePhotoUri = profilePhotoUri
                    )
                }

                showApplyScreen =
                    false
            }
        }
    }
}

@Composable
fun Header(
    profileImageUrl: String = "",
    onProfileClick: () -> Unit = {},
    onMenuClick: () -> Unit,
    menuExpanded: Boolean,
    onDismissMenu: () -> Unit,
    onAboutClick: () -> Unit,
    onFAQClick: () -> Unit,
    onHelpClick: () -> Unit,
    isProvider: Boolean = false,
    onSwitchToProviderClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFF0F0F0))
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            val finalUrl = profileImageUrl.ifBlank {
                com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.photoUrl?.toString() ?: ""
            }
            if (finalUrl.isNotEmpty()) {
                Image(
                    painter = coil.compose.rememberAsyncImagePainter(finalUrl),
                    contentDescription = "Profile Icon",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Icon",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
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
                if (isProvider) {
                    DropdownMenuItem(
                        text = { Text("Switch to Provider Dashboard", fontWeight = FontWeight.Bold) },
                        onClick = { onDismissMenu(); onSwitchToProviderClick() }
                    )
                }
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
    userLat: Double?,
    userLng: Double?,
    onBack: () -> Unit,
    onBookNow: () -> Unit
) {
    var dynamicProfileImageUrl by remember(provider.id) { mutableStateOf<String?>(null) }

    LaunchedEffect(provider.id) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users")
            .document(provider.id)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val url = doc.getString("profileImageUrl")
                    if (!url.isNullOrBlank()) {
                        dynamicProfileImageUrl = url
                    }
                }
            }
    }

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
            val imageUrl = dynamicProfileImageUrl ?: provider.profileImage ?: provider.profileImageUrl
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

        if (userLat != null && userLng != null && userLat != 0.0 && userLng != 0.0 && provider.latitude != 0.0 && provider.longitude != 0.0) {
            val dist = com.example.lbo_marketplace.utils.calculateDistance(
                userLat,
                userLng,
                provider.latitude,
                provider.longitude
            )
            val formatted = com.example.lbo_marketplace.utils.formatDistance(dist)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "📍 $formatted away",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
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
//dont code if you dont know what that caode will do..!
@Composable
fun CustomDialog(

    title: String,

    content: String,

    onClose: () -> Unit
) {

    AlertDialog(

        onDismissRequest = onClose,

        title = {

            Text(

                text = title,

                fontWeight =
                    FontWeight.Bold
            )
        },

        text = {

            Text(content)
        },

        confirmButton = {

            TextButton(

                onClick = onClose,

                colors =
                    ButtonDefaults
                        .textButtonColors(

                            contentColor =
                                Color.Black
                        )
            ) {

                Text("Close")
            }
        },

        shape =
            RoundedCornerShape(24.dp),

        containerColor =
            Color.White
    )
}}

@Composable
fun ChatbotVideoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(
                Uri.parse("android.resource://${context.packageName}/${R.raw.logo}")
            )
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f
            playWhenReady = true
            prepare()
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                }
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> {
                    exoPlayer.play()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier
            .size(60.dp)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.Black)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}