package com.example.lbo_marketplace.ui.screens.user

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.example.lbo_marketplace.R
import com.example.lbo_marketplace.auth.ProviderViewModel
import com.example.lbo_marketplace.data.model.Provider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Banner Item data class.
 */
data class BannerItem(
    val title: String,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val localImageRes: Int? = null,
    val localVideoRes: Int? = null,
    val isVideo: Boolean = false
)

/**
 * Home Screen Content.
 * 
 * FIXES:
 * - Removed duplicate shimmerEffect definition (already in ShimmerEffect.kt).
 * - Cleaned up redundant layout imports.
 */
@Composable
fun HomeTab(
    onBookClick: (String) -> Unit,
    viewModel: ProviderViewModel = viewModel()
) {
    val context = LocalContext.current
    val providers = viewModel.providers
    val isLoading = viewModel.isLoading
    var searchQuery by remember { mutableStateOf("") }
    var showTopRatedPopup by remember { mutableStateOf(false) }
    
    val isOnline = remember { checkNetworkAvailability(context) }
    val scrollState = rememberScrollState()

    BackHandler(enabled = searchQuery.isNotEmpty()) {
        searchQuery = ""
    }

    var userLat by remember { mutableStateOf<Double?>(null) }
    var userLng by remember { mutableStateOf<Double?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getLocation(context) { lat, lng ->
                userLat = lat
                userLng = lng
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchProviders()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation(context) { lat, lng ->
                userLat = lat
                userLng = lng
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val filteredProviders = providers.filter {
        it.serviceType.contains(searchQuery, ignoreCase = true) || 
        it.name.contains(searchQuery, ignoreCase = true)
    }.sortedWith(
        compareBy<Provider> { provider ->
            if (userLat != null && userLng != null) {
                calculateDistance(userLat!!, userLng!!, provider.latitude, provider.longitude)
            } else {
                0f
            }
        }.thenByDescending { provider ->
            provider.rating
        }
    )

    val bannerItems = remember {
        listOf(
            BannerItem("Expert Services", localImageRes = R.drawable.logo),
            BannerItem("Quality Work", localVideoRes = R.raw.logo, isVideo = true),
            BannerItem("Top Providers", localImageRes = R.drawable.logo)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        if (!isOnline) { OfflineWarning() }

        HomeSearchBar(query = searchQuery, onQueryChange = { searchQuery = it })

        AnimatedContent(
            targetState = searchQuery.isEmpty(),
            transitionSpec = { fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400)) },
            label = "ViewTransition"
        ) { isHomeView ->
            if (isHomeView) {
                Column {
                    if (isLoading && bannerItems.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(20.dp)).shimmerEffect())
                    } else {
                        BannerSlider(bannerItems)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { showTopRatedPopup = true },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9D9D9), contentColor = Color.Black),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(text = "TOP RATED OF THIS WEEK", fontWeight = FontWeight.ExtraBold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("👋 Welcome", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (isLoading) {
                        Column {
                            Box(modifier = Modifier.fillMaxWidth(0.7f).height(20.dp).shimmerEffect())
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(modifier = Modifier.fillMaxWidth(0.9f).height(20.dp).shimmerEffect())
                        }
                    } else {
                        Text(
                            "Find the best local service providers in your area. Quick, reliable, and rated by users like you.",
                            style = MaterialTheme.typography.bodyLarge, color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            } else {
                Column(modifier = Modifier.heightIn(max = 2000.dp)) {
                    Text(text = if (isLoading) "Searching..." else "Found ${filteredProviders.size} results", style = MaterialTheme.typography.titleMedium, color = Color.Black)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (isLoading) {
                        Column { repeat(3) { ProviderSkeleton() } }
                    } else if (filteredProviders.isEmpty()) {
                        EmptySearchState(searchQuery)
                    } else {
                        FlowRow(modifier = Modifier.fillMaxWidth(), mainAxisSpacing = 16.dp, crossAxisSpacing = 16.dp) {
                            filteredProviders.forEach { provider ->
                                ProviderGridCard(provider = provider, onBookClick = onBookClick, modifier = Modifier.fillMaxWidth(0.45f))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTopRatedPopup) TopRatedPopup(providers, isLoading, { showTopRatedPopup = false }, onBookClick)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(modifier: Modifier = Modifier, mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp, crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(mainAxisSpacing), verticalArrangement = Arrangement.spacedBy(crossAxisSpacing), maxItemsInEachRow = 2, content = { content() })
}

@Composable
fun OfflineWarning() {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.WifiOff, contentDescription = null, tint = Color.Red)
            Spacer(modifier = Modifier.width(8.dp))
            Text("You are offline. Some content may not load.", style = MaterialTheme.typography.bodySmall, color = Color.Red)
        }
    }
}

@Composable
fun BannerSlider(items: List<BannerItem>) {
    if (items.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { items.size })
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(pagerState.currentPage) {
        val currentItem = items[pagerState.currentPage]
        val flipDelay = if (currentItem.isVideo) 10000L else 3500L
        delay(flipDelay)
        coroutineScope.launch { pagerState.animateScrollToPage((pagerState.currentPage + 1) % items.size) }
    }
    Box(modifier = Modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(20.dp)).background(Color.White)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val item = items[page]
            var videoFailed by remember { mutableStateOf(false) }
            if (item.isVideo && !videoFailed) {
                DynamicVideoPlayer(url = item.videoUrl, localRes = item.localVideoRes, isActive = pagerState.currentPage == page, onError = { videoFailed = true }, onComplete = { coroutineScope.launch { pagerState.animateScrollToPage((page + 1) % items.size) } })
            } else {
                DynamicImage(url = item.imageUrl, localRes = item.localImageRes, title = item.title)
            }
        }
        Row(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp), horizontalArrangement = Arrangement.Center) {
            repeat(items.size) { iteration ->
                Box(modifier = Modifier.padding(4.dp).clip(CircleShape).background(if (pagerState.currentPage == iteration) Color.Black else Color.Black.copy(alpha = 0.2f)).size(10.dp).clickable { coroutineScope.launch { pagerState.animateScrollToPage(iteration) } })
            }
        }
    }
}

@Composable
fun DynamicImage(url: String?, localRes: Int?, title: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (url != null) { AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, error = painterResource(id = localRes ?: R.drawable.logo)) } 
        else { Image(painter = painterResource(id = localRes ?: R.drawable.logo), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
        Text(text = title, modifier = Modifier.align(Alignment.CenterStart).padding(24.dp), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun DynamicVideoPlayer(url: String?, localRes: Int?, isActive: Boolean, onError: () -> Unit, onComplete: () -> Unit) {
    val context = LocalContext.current
    var isReady by remember { mutableStateOf(false) }
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = when {
                url != null -> MediaItem.fromUri(Uri.parse(url))
                localRes != null -> MediaItem.fromUri(Uri.parse("android.resource://${context.packageName}/${localRes}"))
                else -> { onError(); return@apply }
            }
            setMediaItem(mediaItem); playWhenReady = true; prepare()
            addListener(object : Player.Listener { 
                override fun onPlaybackStateChanged(state: Int) { if (state == Player.STATE_READY) isReady = true; if (state == Player.STATE_ENDED) onComplete() }
                override fun onPlayerError(error: PlaybackException) { onError() } 
            })
        }
    }
    LaunchedEffect(isActive) { if (isActive) exoPlayer.play() else exoPlayer.pause() }
    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        if (!isReady) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(30.dp)) } }
        AndroidView(factory = { PlayerView(it).apply { player = exoPlayer; useController = false; resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT; layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT); setBackgroundColor(android.graphics.Color.WHITE) } }, modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun TopRatedPopup(providers: List<Provider>, isLoading: Boolean, onClose: () -> Unit, onBookClick: (String) -> Unit) {
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize().padding(20.dp), shape = RoundedCornerShape(28.dp), color = Color.White, tonalElevation = 0.dp) {
            Column(modifier = Modifier.padding(20.dp).background(Color.White)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Text("Top Rated Providers", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); IconButton(onClick = onClose) { Icon(Icons.Default.Close, null, tint = Color.Black) } }
                Spacer(modifier = Modifier.height(16.dp))
                if (isLoading) { Column { repeat(3) { ProviderSkeleton() } } } 
                else { LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) { items(providers.sortedByDescending { it.rating }.take(10)) { provider -> ProviderGridCard(provider, onBookClick) } } }
                Button(onClick = onClose, modifier = Modifier.fillMaxWidth().padding(top = 16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Black), shape = RoundedCornerShape(12.dp)) { Text("Close") }
            }
        }
    }
}

@Composable
fun ProviderGridCard(provider: Provider, onBookClick: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp).background(Color.White)) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF8F8F8))) {
            if (provider.profileImage != null) { AsyncImage(model = provider.profileImage, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) } 
            else { InitialsAvatar(name = provider.name) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = provider.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(text = provider.serviceType, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, maxLines = 1)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onBookClick(provider.id) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) { Text("Book Now", fontSize = 12.sp) }
    }
}

@Composable
fun InitialsAvatar(name: String) {
    val initials = remember(name) {
        val split = name.trim().split(" ")
        if (split.isEmpty() || split[0].isEmpty()) "?" else split.take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text = initials, style = MaterialTheme.typography.headlineLarge, color = Color.LightGray, fontWeight = FontWeight.ExtraBold) }
}

@Composable
fun ProviderSkeleton() {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(16.dp)).shimmerEffect())
        Spacer(modifier = Modifier.height(8.dp)); Box(modifier = Modifier.fillMaxWidth(0.7f).height(16.dp).shimmerEffect())
        Spacer(modifier = Modifier.height(4.dp)); Box(modifier = Modifier.fillMaxWidth(0.5f).height(14.dp).shimmerEffect())
    }
}

@Composable
fun HomeSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(value = query, onValueChange = onQueryChange, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), placeholder = { Text("What are you looking for?") }, leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Black) }, trailingIcon = if (query.isNotEmpty()) { { IconButton(onClick = { onQueryChange("") }) { Icon(Icons.Default.Close, null, tint = Color.Black) } } } else null, shape = CircleShape, singleLine = true, colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.Black, unfocusedContainerColor = Color(0xFFF4F4F4), focusedContainerColor = Color.White))
}

@Composable
fun EmptySearchState(query: String) { Column(modifier = Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("No results for '$query'", style = MaterialTheme.typography.bodyLarge, color = Color.Gray); Text("Try searching for 'Plumber' or 'Electrician'", style = MaterialTheme.typography.bodySmall, color = Color.LightGray) } }

private fun checkNetworkAvailability(context: android.content.Context): Boolean { val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager; val network = connectivityManager.activeNetwork ?: return false; val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false; return when { activeNetwork.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) -> true; activeNetwork.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) -> true; else -> false } }

fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val results = FloatArray(1)
    android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
    return results[0]
}