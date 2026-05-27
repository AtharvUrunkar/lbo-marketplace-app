package com.example.lbo_marketplace.ui.screens.user

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.lbo_marketplace.R
import com.example.lbo_marketplace.auth.ProviderViewModel
import com.example.lbo_marketplace.auth.AuthViewModel
import com.example.lbo_marketplace.data.model.Provider
import com.example.lbo_marketplace.utils.calculateDistance
import com.example.lbo_marketplace.utils.formatDistance
import com.example.lbo_marketplace.utils.fetchProviderLocation
import com.example.lbo_marketplace.utils.getAddressFromLocation
import com.example.lbo_marketplace.utils.fallbackToAddressLocation
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class BannerItem(
    val title: String,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val localImageRes: Int? = null,
    val localVideoRes: Int? = null,
    val isVideo: Boolean = false
)

@Composable
fun HomeTab(
    onBookClick: (String) -> Unit,
    viewModel: ProviderViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {

    val context = LocalContext.current
    val providers = viewModel.providers
    val isLoading = viewModel.isLoading
    val currentUser = FirebaseAuth.getInstance().currentUser

    var searchQuery by remember {
        mutableStateOf("")
    }

    var showTopRatedPopup by remember {
        mutableStateOf(false)
    }

    val scrollState = rememberScrollState()

    BackHandler(enabled = searchQuery.isNotEmpty()) {
        searchQuery = ""
    }

    var userLat by remember {
        mutableStateOf<Double?>(null)
    }

    var userLng by remember {
        mutableStateOf<Double?>(null)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchProviderLocation(context) { lat: Double, lng: Double ->
                if (lat != 0.0 && lng != 0.0) {
                    userLat = lat
                    userLng = lng
                    currentUser?.uid?.let { uid ->
                        val locData = getAddressFromLocation(context, lat, lng)
                        authViewModel.updateUserLocation(
                            userId = uid,
                            latitude = lat,
                            longitude = lng,
                            city = locData.city,
                            area = locData.area,
                            fullAddress = locData.fullAddress
                        )
                    }
                } else {
                    fallbackToAddressLocation(
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
            fallbackToAddressLocation(
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

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fetchProviderLocation(context) { lat: Double, lng: Double ->
                if (lat != 0.0 && lng != 0.0) {
                    userLat = lat
                    userLng = lng
                } else {
                    fallbackToAddressLocation(
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
            fallbackToAddressLocation(
                userId = currentUser?.uid,
                authViewModel = authViewModel,
                context = context
            ) { fallbackLat, fallbackLng ->
                userLat = fallbackLat
                userLng = fallbackLng
            }
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    val filteredProviders = providers
        .filter { provider ->
            provider.serviceType.contains(searchQuery, ignoreCase = true) ||
                    provider.name.contains(searchQuery, ignoreCase = true)
        }
        .sortedWith(
            compareBy<Provider> { provider ->
                if (userLat != null && userLng != null && userLat != 0.0 && userLng != 0.0 && provider.latitude != 0.0 && provider.longitude != 0.0) {
                    val distance = calculateDistance(userLat!!, userLng!!, provider.latitude, provider.longitude)
                    if (distance <= 18000) 0 else 1
                } else {
                    1
                }
            }.thenBy { provider ->
                if (userLat != null && userLng != null && userLat != 0.0 && userLng != 0.0 && provider.latitude != 0.0 && provider.longitude != 0.0) {
                    calculateDistance(userLat!!, userLng!!, provider.latitude, provider.longitude)
                } else {
                    Float.MAX_VALUE
                }
            }.thenByDescending { provider ->
                provider.rating
            }
        )

    val bannerItems = remember {

        listOf(
            BannerItem("Expert Constructions", localImageRes = R.drawable.constructionbanner),
            BannerItem("Quality Work", localVideoRes = R.raw.fevicol, isVideo = true),
            BannerItem("Computer Services", localImageRes = R.drawable.kushnacombanner),
            BannerItem("Hotel", localImageRes = R.drawable.sayajibanner),
            BannerItem("Marketing", localImageRes = R.drawable.digitalmarketing),
            BannerItem("Painters", localImageRes = R.drawable.painterbranding)
        )
    }


    Column(

        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {

        HomeSearchBar(
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
                ) togetherWith fadeOut(
                    animationSpec = tween(400)
                )
            },

            label = "ViewTransition"

        ) { isHomeView ->

            if (isHomeView) {

                Column {

                    if (
                        isLoading &&
                        bannerItems.isEmpty()
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .clip(
                                    RoundedCornerShape(20.dp)
                                )
                                .shimmerEffect()
                        )

                    } else {

                        BannerSlider(
                            bannerItems
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

                        modifier = Modifier
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
                            )
                    ) {

                        Text(

                            text =
                                "TOP RATED OF THIS WEEK",

                            fontWeight =
                                FontWeight.ExtraBold
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(24.dp)
                    )

                    Text(

                        "👋 Welcome",

                        style =
                            MaterialTheme
                                .typography
                                .headlineSmall,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(

                        "Find the best local service providers in your area.",

                        style =
                            MaterialTheme
                                .typography
                                .bodyLarge,

                        color = Color.Gray
                    )

                    Spacer(
                        modifier =
                            Modifier.height(40.dp)
                    )
                }

            } else {

                Column {

                    Text(

                        text =
                            "Found ${filteredProviders.size} results",

                        style =
                            MaterialTheme
                                .typography
                                .titleMedium
                    )

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    if (filteredProviders.isEmpty()) {

                        EmptySearchState(
                            searchQuery
                        )

                    } else {

                        FlowRow(

                            modifier =
                                Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.spacedBy(16.dp),

                            verticalArrangement =
                                Arrangement.spacedBy(16.dp)
                        ) {

                            filteredProviders.forEach { provider ->

                                ProviderGridCard(

                                    provider = provider,

                                    onBookClick = onBookClick,

                                    modifier =
                                        Modifier.fillMaxWidth(
                                            0.45f
                                        ),

                                    userLat = userLat,

                                    userLng = userLng
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTopRatedPopup) {

        TopRatedPopup(

            providers = providers,

            isLoading = isLoading,

            onClose = {
                showTopRatedPopup = false
            },

            onBookClick = onBookClick
        )
    }
}

@Composable
fun ProviderGridCard(
    provider: Provider,
    onBookClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    userLat: Double? = null,
    userLng: Double? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color.White)
            .clickable { onBookClick(provider.id) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF8F8F8))
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
                InitialsAvatar(name = provider.name)
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
                        Spacer(modifier = Modifier.width(2.dp))
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

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = provider.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            color = Color.Black
        )

        Text(
            text = provider.serviceType,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            maxLines = 1
        )

        if (userLat != null && userLng != null && userLat != 0.0 && userLng != 0.0 && provider.latitude != 0.0 && provider.longitude != 0.0) {
            val distance = calculateDistance(
                userLat,
                userLng,
                provider.latitude,
                provider.longitude
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "📍 ${formatDistance(distance)} away",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onBookClick(provider.id) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("View Profile", fontSize = 12.sp, color = Color.White)
        }
    }
}

@Composable
fun HomeSearchBar(
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
            Text("Search providers...")
        },

        leadingIcon = {
            Icon(
                Icons.Default.Search,
                null
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
                        Icons.Default.Close,
                        null
                    )
                }
            }
        },

        shape = CircleShape,

        singleLine = true
    )
}

@Composable
fun EmptySearchState(
    query: String
) {

    Column(

        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            "No results for '$query'"
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Text(
            "Try searching electrician or plumber",
            color = Color.Gray
        )
    }
}

@Composable
fun InitialsAvatar(
    name: String
) {

    val initials = remember(name) {

        val split =
            name.trim().split(" ")

        if (
            split.isEmpty() ||
            split[0].isEmpty()
        ) {

            "?"

        } else {

            split.take(2)
                .mapNotNull {
                    it.firstOrNull()
                        ?.uppercase()
                }
                .joinToString("")
        }
    }

    Box(

        modifier =
            Modifier.fillMaxSize(),

        contentAlignment =
            Alignment.Center
    ) {

        Text(

            text = initials,

            style =
                MaterialTheme
                    .typography
                    .headlineLarge,

            color = Color.LightGray,

            fontWeight =
                FontWeight.ExtraBold
        )
    }
}

@Composable
fun BannerSlider(
    items: List<BannerItem>
) {

    if (items.isEmpty()) return

    val pagerState =
        rememberPagerState {
            items.size
        }

    val coroutineScope =
        rememberCoroutineScope()

    LaunchedEffect(
        pagerState.currentPage
    ) {

        delay(3000)

        coroutineScope.launch {

            pagerState.animateScrollToPage(
                (pagerState.currentPage + 1)
                        % items.size
            )
        }
    }

    HorizontalPager(

        state = pagerState,

        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
    ) { page ->

        val item = items[page]

        Box(

            modifier = Modifier
                .fillMaxSize()
                .clip(
                    RoundedCornerShape(20.dp)
                )
        ) {

            Image(

                painter = painterResource(
                    id = item.localImageRes
                        ?: R.drawable.logo
                ),

                contentDescription = null,

                modifier =
                    Modifier.fillMaxSize(),

                contentScale =
                    ContentScale.Crop
            )
        }
    }
}

@Composable
fun TopRatedPopup(

    providers: List<Provider>,

    isLoading: Boolean,

    onClose: () -> Unit,

    onBookClick: (String) -> Unit
) {

    Dialog(

        onDismissRequest = onClose,

        properties =
            DialogProperties(
                usePlatformDefaultWidth = false
            )
    ) {

        Surface(

            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),

            shape =
                RoundedCornerShape(28.dp),

            color = Color.White
        ) {

            Column(
                modifier =
                    Modifier.padding(20.dp)
            ) {

                Row(

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(

                        "Top Rated Providers",

                        style =
                            MaterialTheme
                                .typography
                                .headlineSmall,

                        fontWeight =
                            FontWeight.Bold,

                        modifier =
                            Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onClose
                    ) {

                        Icon(
                            Icons.Default.Close,
                            null
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                if (isLoading) {

                    CircularProgressIndicator()

                } else {

                    LazyVerticalGrid(

                        columns =
                            GridCells.Fixed(2),

                        horizontalArrangement =
                            Arrangement.spacedBy(16.dp),

                        verticalArrangement =
                            Arrangement.spacedBy(16.dp),

                        modifier =
                            Modifier.weight(1f)
                    ) {

                        items(

                            providers
                                .sortedByDescending {
                                    it.rating
                                }
                                .take(10)

                        ) { provider ->

                            ProviderGridCard(

                                provider = provider,

                                onBookClick =
                                    onBookClick
                            )
                        }
                    }
                }
            }
        }
    }
}

