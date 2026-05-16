package com.example.lbo_marketplace.ui.screens.user

import android.net.Uri
import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.lbo_marketplace.R
import com.example.lbo_marketplace.auth.ProviderViewModel
import com.example.lbo_marketplace.data.model.Provider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Banner Item data class to handle both images and videos in the slider.
 */
data class BannerItem(
    val title: String,
    val imageRes: Int? = null,
    val videoRes: Int? = null,
    val isVideo: Boolean = false
)

/**
 * Main Home Tab for the User experience.
 * Features: Header, Search Bar, Auto-sliding Banner (Image/Video), and Top Rated Popup.
 * Now includes Skeleton Loading (Shimmer Effect) for improved UX.
 */
@Composable
fun HomeTab(
    onBookClick: (String) -> Unit,
    viewModel: ProviderViewModel = viewModel()
) {
    val providers = viewModel.providers
    val isLoading = viewModel.isLoading
    var searchQuery by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }
    var showTopRatedPopup by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()

    // Fetch data on initialization
    LaunchedEffect(Unit) {
        viewModel.fetchProviders()
    }

    // Filter providers based on search query
    val filteredProviders = providers.filter {
        it.serviceType.contains(searchQuery, ignoreCase = true) || 
        it.name.contains(searchQuery, ignoreCase = true)
    }

    // Mock Banner Data
    val bannerItems = remember {
        listOf(
            BannerItem("Expert Services", imageRes = R.drawable.logo),
            BannerItem("Quality Work", videoRes = R.raw.logo, isVideo = true),
            BannerItem("Top Providers", imageRes = R.drawable.logo)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // --- Header Section ---
        HomeHeader(
            onMenuClick = { menuExpanded = true },
            menuExpanded = menuExpanded,
            onDismissMenu = { menuExpanded = false }
        )

        // --- Search Bar ---
        HomeSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        // --- View Switching Logic ---
        AnimatedContent(
            targetState = searchQuery.isEmpty(),
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
            }, label = "ViewTransition"
        ) { isHomeView ->
            if (isHomeView) {
                // DEFAULT HOME CONTENT
                Column {
                    // 1. Sliding Banner with Skeleton support
                    if (isLoading && bannerItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .shimmerEffect()
                        )
                    } else {
                        BannerSlider(bannerItems)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 2. Top Rated Action Button
                    Button(
                        onClick = { showTopRatedPopup = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF0F0F0), 
                            contentColor = Color.Black
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = "TOP RATED OF THIS WEEK",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 3. Welcome Section
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
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            } else {
                // SEARCH RESULTS CONTENT
                Column(modifier = Modifier.heightIn(max = 1000.dp)) {
                    Text(
                        text = if (isLoading) "Searching..." else "Found ${filteredProviders.size} results for '$searchQuery'",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (isLoading) {
                        repeat(5) {
                            ProviderSkeleton()
                        }
                    } else if (filteredProviders.isEmpty()) {
                        EmptySearchState(searchQuery)
                    } else {
                        filteredProviders.forEach { provider ->
                            ProviderCard(provider, onBookClick, showBookButton = true)
                        }
                    }
                }
            }
        }
    }

    // --- Popups ---
    if (showTopRatedPopup) {
        TopRatedPopup(
            providers = providers,
            isLoading = isLoading,
            onClose = { showTopRatedPopup = false },
            onBookClick = onBookClick
        )
    }
}

@Composable
fun HomeHeader(onMenuClick: () -> Unit, menuExpanded: Boolean, onDismissMenu: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Default.Menu, contentDescription = "Menu")
        }
        Text(
            text = "LoGO",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(48.dp))

        Box {
            DropdownMenu(expanded = menuExpanded, onDismissRequest = onDismissMenu) {
                DropdownMenuItem(text = { Text("About") }, onClick = onDismissMenu)
                DropdownMenuItem(text = { Text("FAQ") }, onClick = onDismissMenu)
                DropdownMenuItem(text = { Text("Help") }, onClick = onDismissMenu)
            }
        }
    }
}

@Composable
fun HomeSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        placeholder = { Text("What are you looking for?") },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        trailingIcon = if (query.isNotEmpty()) {
            { IconButton(onClick = { onQueryChange("") }) { Icon(Icons.Default.Close, null) } }
        } else null,
        shape = CircleShape,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = Color(0xFFF4F4F4),
            focusedContainerColor = Color.White
        )
    )
}

@Composable
fun BannerSlider(items: List<BannerItem>) {
    val pagerState = rememberPagerState(pageCount = { items.size })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        val currentItem = items[pagerState.currentPage]
        if (!currentItem.isVideo) {
            delay(3500)
            val nextPage = (pagerState.currentPage + 1) % items.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.LightGray)
        ) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                val item = items[page]
                if (item.isVideo && item.videoRes != null) {
                    VideoBannerItem(
                        videoRes = item.videoRes,
                        isActive = pagerState.currentPage == page,
                        onComplete = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage((page + 1) % items.size)
                            }
                        }
                    )
                } else {
                    ImageBannerItem(item)
                }
            }

            Row(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(items.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(10.dp)
                            .clickable {
                                coroutineScope.launch { pagerState.animateScrollToPage(iteration) }
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun ImageBannerItem(item: BannerItem) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = item.imageRes ?: R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))
        Text(
            text = item.title,
            modifier = Modifier.align(Alignment.CenterStart).padding(24.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun VideoBannerItem(videoRes: Int, isActive: Boolean, onComplete: () -> Unit) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val uri = Uri.parse("android.resource://${context.packageName}/$videoRes")
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            repeatMode = Player.REPEAT_MODE_OFF
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) onComplete()
                }
            })
        }
    }

    LaunchedEffect(isActive) {
        if (isActive) exoPlayer.play() else exoPlayer.pause()
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                useController = false
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun TopRatedPopup(
    providers: List<Provider>, 
    isLoading: Boolean,
    onClose: () -> Unit, 
    onBookClick: (String) -> Unit
) {
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Top Rated Providers", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    IconButton(onClick = onClose) { Icon(Icons.Default.Close, null) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    if (isLoading) {
                        items(5) {
                            ProviderSkeleton()
                        }
                    } else {
                        items(providers.take(10)) { provider ->
                            ProviderCard(provider, onBookClick, showBookButton = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.fillMaxWidth(0.5f).height(20.dp).shimmerEffect())
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(0.3f).height(16.dp).shimmerEffect())
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.align(Alignment.End).size(width = 100.dp, height = 36.dp).clip(RoundedCornerShape(20.dp)).shimmerEffect())
        }
    }
}

@Composable
fun EmptySearchState(query: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No results for '$query'", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        Text("Try searching for 'Plumber' or 'Electrician'", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
    }
}

@Composable
fun ProviderCard(provider: Provider, onBookClick: (String) -> Unit, showBookButton: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = provider.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = provider.serviceType, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            if (showBookButton) {
                Button(
                    onClick = { onBookClick(provider.id) },
                    modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                ) { Text("Book Now") }
            } else {
                Text("⭐ Top Rated", color = Color(0xFFFFB400), modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}