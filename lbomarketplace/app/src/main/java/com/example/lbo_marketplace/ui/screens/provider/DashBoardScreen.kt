package com.example.lbo_marketplace.ui.screens.provider

import android.Manifest
import android.content.Context
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.lbo_marketplace.R
import com.example.lbo_marketplace.auth.ProviderViewModel
import com.example.lbo_marketplace.data.model.Provider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ProviderBannerItem(
    val title: String,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val localImageRes: Int? = null,
    val localVideoRes: Int? = null,
    val isVideo: Boolean = false
)

@Composable
fun DashboardScreen(
    header: @Composable () -> Unit,
    viewModel: ProviderViewModel = viewModel(),
    authViewModel: com.example.lbo_marketplace.auth.AuthViewModel = viewModel()
) {

    val context =
        LocalContext.current

    val providers =
        viewModel.providers

    val isLoading =
        viewModel.isLoading

    var searchQuery by remember {
        mutableStateOf("")
    }

    var showTopRatedPopup by remember {
        mutableStateOf(false)
    }

    var selectedDetailProvider by remember {
        mutableStateOf<Provider?>(null)
    }

    var selectedClusterForPopup by remember {
        mutableStateOf<String?>(null)
    }

    val isOnline = remember {
        checkNetworkAvailabilityLocal(
            context
        )
    }

    val scrollState =
        rememberScrollState()

    BackHandler(
        enabled = searchQuery.isNotEmpty()
    ) {

        searchQuery = ""
    }

    var userLat by remember {
        mutableStateOf<Double?>(null)
    }

    var userLng by remember {
        mutableStateOf<Double?>(null)
    }

    val currentUser =
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->

            if (isGranted) {

                com.example.lbo_marketplace.utils.fetchProviderLocation(
                    context
                ) { lat, lng ->

                    if (lat != 0.0 && lng != 0.0) {

                        userLat = lat
                        userLng = lng

                        currentUser?.uid?.let { uid ->

                            val locData =
                                com.example.lbo_marketplace.utils.getAddressFromLocation(
                                    context,
                                    lat,
                                    lng
                                )

                            viewModel.updateProviderLocationOnly(
                                userId = uid,
                                latitude = lat,
                                longitude = lng,
                                city = locData.city,
                                area = locData.area,
                                fullAddress = locData.fullAddress
                            )
                        }

                    } else {

                        com.example.lbo_marketplace.utils.fallbackToAddressLocation(
                            userId = currentUser?.uid,
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
                    userId = currentUser?.uid,
                    authViewModel = authViewModel,
                    context = context
                ) { fallbackLat, fallbackLng ->

                    userLat = fallbackLat
                    userLng = fallbackLng
                }
            }
        }

    LaunchedEffect(Unit) {

        viewModel.fetchProviders()

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            com.example.lbo_marketplace.utils.fetchProviderLocation(
                context
            ) { lat, lng ->

                if (lat != 0.0 && lng != 0.0) {

                    userLat = lat
                    userLng = lng

                    currentUser?.uid?.let { uid ->

                        val locData =
                            com.example.lbo_marketplace.utils.getAddressFromLocation(
                                context,
                                lat,
                                lng
                            )

                        viewModel.updateProviderLocationOnly(
                            userId = uid,
                            latitude = lat,
                            longitude = lng,
                            city = locData.city,
                            area = locData.area,
                            fullAddress = locData.fullAddress
                        )
                    }

                } else {

                    com.example.lbo_marketplace.utils.fallbackToAddressLocation(
                        userId = currentUser?.uid,
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
                userId = currentUser?.uid,
                authViewModel = authViewModel,
                context = context
            ) { fallbackLat, fallbackLng ->

                userLat = fallbackLat
                userLng = fallbackLng
            }

            locationPermissionLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    val filteredProviders =
        providers.filter { provider ->

            provider.serviceType.contains(searchQuery, ignoreCase = true) ||
                    provider.name.contains(searchQuery, ignoreCase = true)

        }.sortedWith(

            compareBy<Provider> { provider ->

                if (
                    userLat != null &&
                    userLng != null &&
                    userLat != 0.0 &&
                    userLng != 0.0 &&
                    provider.latitude != 0.0 &&
                    provider.longitude != 0.0
                ) {

                    val distance =
                        com.example.lbo_marketplace.utils.calculateDistance(
                            userLat!!,
                            userLng!!,
                            provider.latitude,
                            provider.longitude
                        )

                    if (distance <= 18000) 0 else 1

                } else {

                    1
                }

            }.thenBy { provider ->

                if (
                    userLat != null &&
                    userLng != null &&
                    userLat != 0.0 &&
                    userLng != 0.0 &&
                    provider.latitude != 0.0 &&
                    provider.longitude != 0.0
                ) {

                    com.example.lbo_marketplace.utils.calculateDistance(
                        userLat!!,
                        userLng!!,
                        provider.latitude,
                        provider.longitude
                    )

                } else {

                    Float.MAX_VALUE
                }

            }.thenByDescending { provider ->

                provider.rating
            }
        )

    val bannerItems = remember {

        listOf(
            ProviderBannerItem("Expert Services", localImageRes = R.drawable.logo),
            ProviderBannerItem("Quality Work", localVideoRes = R.raw.logo, isVideo = true),
            ProviderBannerItem("Top Providers", localImageRes = R.drawable.logo)
        )
    }

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.White)

    ) {

        // Render custom header at the top
        header()

        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)

        ) {

            if (!isOnline) {

                ProviderOfflineWarning()
            }

            ProviderHomeSearchBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                }
            )

            AnimatedContent(

                targetState = searchQuery.isEmpty(),

                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(400)
                    ) togetherWith
                            fadeOut(
                                animationSpec = tween(400)
                            )
                },

                label = "ViewTransition"

            ) { isHomeView ->

                if (isHomeView) {

                    Column {

                        if (isLoading && bannerItems.isEmpty()) {

                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(210.dp)
                                        .clip(
                                            RoundedCornerShape(20.dp)
                                        )
                                        .localShimmerEffect()
                            )

                        } else {

                            ProviderBannerSlider(
                                items = bannerItems
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.height(24.dp)
                        )

                        Button(

                            onClick = {
                                showTopRatedPopup = true
                            },

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),

                            shape =
                                RoundedCornerShape(16.dp),

                            colors =
                                ButtonDefaults.buttonColors(

                                    containerColor =
                                        Color(0xFFD9D9D9),

                                    contentColor =
                                        Color.Black
                                ),

                            elevation =
                                ButtonDefaults.buttonElevation(
                                    defaultElevation = 2.dp
                                )

                        ) {

                            Text(
                                text = "TOP RATED OF THIS WEEK",
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )

                        Spacer(
                            modifier =
                                Modifier.height(24.dp)
                        )

                        Text(
                            text = "📍 Explore by Location",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Row 1: Sangli & Kolhapur
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                listOf("Sangli", "Kolhapur").forEach { clusterName ->
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1.2f)
                                            .clickable {
                                                selectedClusterForPopup = clusterName
                                                // Future feature: Navigation to new screen can be handled here
                                            },
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFFE0E0E0),
                                            contentColor = Color.Black
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = clusterName,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Row 2: Belgav & Ichalkaranji
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                listOf("Belgav", "Ichalkaranji").forEach { clusterName ->
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1.2f)
                                            .clickable {
                                                selectedClusterForPopup = clusterName
                                                // Future feature: Navigation to new screen can be handled here
                                            },
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFFE0E0E0),
                                            contentColor = Color.Black
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = clusterName,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(
                            modifier =
                                Modifier.height(20.dp)
                        )
                    }

                } else {

                    Column(
                        modifier =
                            Modifier.heightIn(max = 2000.dp)
                    ) {

                        Text(

                            text =
                                if (isLoading) {
                                    "Searching..."
                                } else {
                                    "Found ${filteredProviders.size} results"
                                },

                            style =
                                MaterialTheme
                                    .typography
                                    .titleMedium,

                            color = Color.Black
                        )

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )

                        if (isLoading) {

                            Column {

                                repeat(3) {

                                    ProviderSkeletonCard()
                                }
                            }

                        } else if (filteredProviders.isEmpty()) {

                            ProviderEmptySearchState(
                                query = searchQuery
                            )

                        } else {

                            ProviderFlowRow(

                                modifier =
                                    Modifier.fillMaxWidth(),

                                mainAxisSpacing = 16.dp,

                                crossAxisSpacing = 16.dp

                            ) {

                                filteredProviders.forEach { provider ->

                                    ProviderGridCard(

                                        provider = provider,

                                        userLat = userLat,

                                        userLng = userLng,

                                        onDetailClick = {
                                            selectedDetailProvider = it
                                        },

                                        modifier =
                                            Modifier.fillMaxWidth(0.45f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTopRatedPopup) {

        ProviderTopRatedPopup(

            providers = providers,

            isLoading = isLoading,

            userLat = userLat,

            userLng = userLng,

            title = "Top Rated Providers",

            onClose = {
                showTopRatedPopup = false
            },

            onDetailClick = { provider ->

                showTopRatedPopup = false
                selectedDetailProvider = provider
            }
        )
    }

    if (selectedClusterForPopup != null) {
        val clusterName = selectedClusterForPopup!!
        val clusterProvidersList = remember(clusterName, providers) {
            providers.filter { provider ->
                provider.city.contains(clusterName, ignoreCase = true) ||
                provider.area.contains(clusterName, ignoreCase = true) ||
                provider.fullAddress.contains(clusterName, ignoreCase = true)
            }.sortedByDescending { it.rating }
        }

        ProviderTopRatedPopup(
            providers = clusterProvidersList,
            isLoading = isLoading,
            userLat = userLat,
            userLng = userLng,
            title = "Providers in $clusterName",
            onClose = {
                selectedClusterForPopup = null
            },
            onDetailClick = { provider ->
                selectedClusterForPopup = null
                selectedDetailProvider = provider
            }
        )
    }

    if (selectedDetailProvider != null) {

        ProviderDetailPopup(

            provider = selectedDetailProvider!!,

            userLat = userLat,

            userLng = userLng,

            onClose = {
                selectedDetailProvider = null
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProviderFlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {

    androidx.compose.foundation.layout.FlowRow(

        modifier = modifier,

        horizontalArrangement =
            Arrangement.spacedBy(mainAxisSpacing),

        verticalArrangement =
            Arrangement.spacedBy(crossAxisSpacing),

        maxItemsInEachRow = 2,

        content = {
            content()
        }
    )
}

@Composable
fun ProviderOfflineWarning() {

    Card(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),

        colors =
            CardDefaults.cardColors(
                containerColor = Color(0xFFFFEBEE)
            )

    ) {

        Row(

            modifier =
                Modifier.padding(12.dp),

            verticalAlignment =
                Alignment.CenterVertically

        ) {

            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = null,
                tint = Color.Red
            )

            Spacer(
                modifier =
                    Modifier.width(8.dp)
            )

            Text(
                text = "You are offline. Some content may not load.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Red
            )
        }
    }
}

@Composable
fun ProviderBannerSlider(
    items: List<ProviderBannerItem>
) {

    if (items.isEmpty()) return

    val pagerState =
        rememberPagerState(
            pageCount = {
                items.size
            }
        )

    val coroutineScope =
        rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {

        val currentItem =
            items[pagerState.currentPage]

        val flipDelay =
            if (currentItem.isVideo) 10000L else 3500L

        delay(flipDelay)

        coroutineScope.launch {

            pagerState.animateScrollToPage(
                (pagerState.currentPage + 1) % items.size
            )
        }
    }

    Box(

        modifier =
            Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(
                    RoundedCornerShape(20.dp)
                )
                .background(Color.White)

    ) {

        HorizontalPager(

            state = pagerState,

            modifier =
                Modifier.fillMaxSize()

        ) { page ->

            val item =
                items[page]

            var videoFailed by remember {
                mutableStateOf(false)
            }

            if (item.isVideo && !videoFailed) {

                ProviderDynamicVideoPlayer(

                    url = item.videoUrl,

                    localRes = item.localVideoRes,

                    isActive =
                        pagerState.currentPage == page,

                    onError = {
                        videoFailed = true
                    },

                    onComplete = {

                        coroutineScope.launch {

                            pagerState.animateScrollToPage(
                                (page + 1) % items.size
                            )
                        }
                    }
                )

            } else {

                ProviderDynamicImage(
                    url = item.imageUrl,
                    localRes = item.localImageRes,
                    title = item.title
                )
            }
        }

        Row(

            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),

            horizontalArrangement =
                Arrangement.Center

        ) {

            repeat(items.size) { iteration ->

                Box(
                    modifier =
                        Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == iteration) {
                                    Color.Black
                                } else {
                                    Color.Black.copy(alpha = 0.2f)
                                }
                            )
                            .size(10.dp)
                            .clickable {

                                coroutineScope.launch {

                                    pagerState.animateScrollToPage(
                                        iteration
                                    )
                                }
                            }
                )
            }
        }
    }
}

@Composable
fun ProviderDynamicImage(
    url: String?,
    localRes: Int?,
    title: String
) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        if (url != null) {

            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = painterResource(id = localRes ?: R.drawable.logo)
            )

        } else {

            Image(
                painter = painterResource(id = localRes ?: R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = title,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(24.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun ProviderDynamicVideoPlayer(
    url: String?,
    localRes: Int?,
    isActive: Boolean,
    onError: () -> Unit,
    onComplete: () -> Unit
) {

    val context =
        LocalContext.current

    var isReady by remember {
        mutableStateOf(false)
    }

    val exoPlayer = remember {

        ExoPlayer.Builder(context).build().apply {

            val mediaItem =
                when {

                    url != null ->
                        MediaItem.fromUri(Uri.parse(url))

                    localRes != null ->
                        MediaItem.fromUri(
                            Uri.parse(
                                "android.resource://${context.packageName}/${localRes}"
                            )
                        )

                    else -> {
                        onError()
                        return@apply
                    }
                }

            setMediaItem(mediaItem)
            playWhenReady = true
            prepare()

            addListener(
                object : Player.Listener {

                    override fun onPlaybackStateChanged(
                        state: Int
                    ) {

                        if (state == Player.STATE_READY) {

                            isReady = true
                        }

                        if (state == Player.STATE_ENDED) {

                            onComplete()
                        }
                    }

                    override fun onPlayerError(
                        error: PlaybackException
                    ) {

                        onError()
                    }
                }
            )
        }
    }

    LaunchedEffect(isActive) {

        if (isActive) {

            exoPlayer.play()

        } else {

            exoPlayer.pause()
        }
    }

    DisposableEffect(Unit) {

        onDispose {

            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        if (!isReady) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                CircularProgressIndicator(
                    color = Color.Black,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        AndroidView(

            factory = {

                PlayerView(it).apply {

                    player = exoPlayer
                    useController = false

                    resizeMode =
                        AspectRatioFrameLayout.RESIZE_MODE_FIT

                    layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                    setBackgroundColor(
                        android.graphics.Color.WHITE
                    )
                }
            },

            modifier =
                Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ProviderTopRatedPopup(
    providers: List<Provider>,
    isLoading: Boolean,
    userLat: Double?,
    userLng: Double?,
    title: String = "Top Rated Providers",
    onClose: () -> Unit,
    onDetailClick: (Provider) -> Unit
) {

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            tonalElevation = 0.dp
        ) {

            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .background(Color.White)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onClose
                    ) {

                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.Black
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                if (isLoading) {

                    Column {

                        repeat(3) {

                            ProviderSkeletonCard()
                        }
                    }

                } else {

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {

                        items(
                            providers
                                .sortedByDescending { it.rating }
                                .take(10)
                        ) { provider ->

                            ProviderGridCard(
                                provider = provider,
                                userLat = userLat,
                                userLng = userLng,
                                onDetailClick = onDetailClick
                            )
                        }
                    }
                }

                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {

                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun ProviderGridCard(
    provider: Provider,
    userLat: Double?,
    userLng: Double?,
    onDetailClick: (Provider) -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color.White)
            .clickable {
                onDetailClick(provider)
            }
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF8F8F8))
        ) {

            val imageUrl =
                provider.profileImage ?: provider.profileImageUrl

            if (!imageUrl.isNullOrBlank()) {

                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

            } else {

                ProviderInitialsAvatar(
                    name = provider.name
                )
            }

            // Rating Badge Overlay
            if (provider.rating > 0.0) {

                Surface(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(topStart = 16.dp, bottomEnd = 16.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {

                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(14.dp)
                        )

                        Spacer(
                            modifier =
                                Modifier.width(2.dp)
                        )

                        Text(
                            text = String.format(java.util.Locale.US, "%.1f", provider.rating),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Text(
            text = provider.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )

        Text(
            text = provider.serviceType,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            maxLines = 1
        )

        if (
            userLat != null &&
            userLng != null &&
            userLat != 0.0 &&
            userLng != 0.0 &&
            provider.latitude != 0.0 &&
            provider.longitude != 0.0
        ) {

            val distance =
                com.example.lbo_marketplace.utils.calculateDistance(
                    userLat,
                    userLng,
                    provider.latitude,
                    provider.longitude
                )

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Text(
                text = "📍 ${com.example.lbo_marketplace.utils.formatDistance(distance)} away",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1
            )
        }

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Button(

            onClick = {
                onDetailClick(provider)
            },

            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(8.dp),

            contentPadding =
                PaddingValues(
                    horizontal = 8.dp,
                    vertical = 4.dp
                ),

            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                )

        ) {

            Text(
                text = "View Profile",
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun ProviderDetailPopup(
    provider: Provider,
    userLat: Double?,
    userLng: Double?,
    onClose: () -> Unit
) {

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {

                    IconButton(
                        onClick = onClose
                    ) {

                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF8F8F8)),
                    contentAlignment = Alignment.Center
                ) {

                    val imageUrl =
                        provider.profileImage ?: provider.profileImageUrl

                    if (!imageUrl.isNullOrBlank()) {

                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                    } else {

                        val initials =
                            provider.name.take(2).uppercase()

                        Text(
                            text = initials,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                Text(
                    text = provider.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )

                Text(
                    text = provider.serviceType,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(
                        modifier =
                            Modifier.width(4.dp)
                    )

                    Text(
                        text = "${provider.rating} / 5.0",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                if (
                    userLat != null &&
                    userLng != null &&
                    userLat != 0.0 &&
                    userLng != 0.0 &&
                    provider.latitude != 0.0 &&
                    provider.longitude != 0.0
                ) {

                    val dist =
                        com.example.lbo_marketplace.utils.calculateDistance(
                            userLat,
                            userLng,
                            provider.latitude,
                            provider.longitude
                        )

                    val formatted =
                        com.example.lbo_marketplace.utils.formatDistance(dist)

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    Text(
                        text = "📍 $formatted away",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(24.dp)
                )

                HorizontalDivider(
                    color = Color(0xFFEEEEEE)
                )

                Spacer(
                    modifier =
                        Modifier.height(24.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {

                    Text(
                        text = "EXPERIENCE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )

                    Text(
                        text = provider.experience,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black
                    )

                    Spacer(
                        modifier =
                            Modifier.height(16.dp)
                    )

                    Text(
                        text = "ABOUT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )

                    Text(
                        text = provider.description.ifBlank { "No description provided." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                }

                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {

                    Text(
                        text = "Close",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ProviderInitialsAvatar(
    name: String
) {

    val initials = remember(name) {

        val split =
            name.trim().split(" ")

        if (split.isEmpty() || split[0].isEmpty()) {
            "?"
        } else {
            split.take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = initials,
            style = MaterialTheme.typography.headlineLarge,
            color = Color.LightGray,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun ProviderSkeletonCard() {

    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .localShimmerEffect()
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(16.dp)
                .localShimmerEffect()
        )

        Spacer(
            modifier =
                Modifier.height(4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(14.dp)
                .localShimmerEffect()
        )
    }
}

@Composable
fun ProviderHomeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {

    OutlinedTextField(

        value = query,

        onValueChange = onQueryChange,

        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),

        placeholder = {
            Text("What are you looking for?")
        },

        leadingIcon = {

            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color.Black
            )
        },

        trailingIcon = {

            if (query.isNotEmpty()) {

                IconButton(
                    onClick = {
                        onQueryChange("")
                    }
                ) {

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.Black
                    )
                }
            }
        },

        shape = CircleShape,
        singleLine = true,

        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Black,
            unfocusedContainerColor = Color(0xFFF4F4F4),
            focusedContainerColor = Color.White
        )
    )
}

@Composable
fun ProviderEmptySearchState(
    query: String
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "No results for '$query'",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )

        Text(
            text = "Try searching for 'Plumber' or 'Electrician'",
            style = MaterialTheme.typography.bodySmall,
            color = Color.LightGray
        )
    }
}

private fun checkNetworkAvailabilityLocal(
    context: Context
): Boolean {

    val connectivityManager =
        context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as android.net.ConnectivityManager

    val network =
        connectivityManager.activeNetwork ?: return false

    val activeNetwork =
        connectivityManager.getNetworkCapabilities(network) ?: return false

    return when {

        activeNetwork.hasTransport(
            android.net.NetworkCapabilities.TRANSPORT_WIFI
        ) -> true

        activeNetwork.hasTransport(
            android.net.NetworkCapabilities.TRANSPORT_CELLULAR
        ) -> true

        else -> false
    }
}

fun Modifier.localShimmerEffect(): Modifier =
    composed {

        val transition =
            rememberInfiniteTransition(label = "shimmer")

        val translateAnim by transition.animateFloat(

            initialValue = 0f,

            targetValue = 1000f,

            animationSpec =
                infiniteRepeatable(

                    animation =
                        tween(
                            durationMillis = 1200,
                            easing = LinearEasing
                        ),

                    repeatMode =
                        RepeatMode.Restart
                ),

            label = "shimmer"
        )

        val shimmerColors =
            listOf(
                Color.LightGray.copy(alpha = 0.6f),
                Color.LightGray.copy(alpha = 0.2f),
                Color.LightGray.copy(alpha = 0.6f)
            )

        val brush =
            Brush.linearGradient(

                colors = shimmerColors,

                start = Offset.Zero,

                end =
                    Offset(
                        x = translateAnim,
                        y = translateAnim
                    )
            )

        return@composed this.background(brush)
    }