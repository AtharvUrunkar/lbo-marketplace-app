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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    var showCategoryPopup by remember {
        mutableStateOf(false)
    }

    var selectedCluster by remember {
        mutableStateOf("All")
    }

    var selectedClusterForPopup by remember {
        mutableStateOf<String?>(null)
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

    val clusterProviders = remember(selectedCluster, providers) {
        if (selectedCluster == "All") {
            emptyList<Provider>()
        } else {
            providers.filter { provider ->
                provider.city.contains(selectedCluster, ignoreCase = true) ||
                provider.area.contains(selectedCluster, ignoreCase = true) ||
                provider.fullAddress.contains(selectedCluster, ignoreCase = true)
            }.sortedByDescending { it.rating }
        }
    }

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
            },
            providers = providers
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                showTopRatedPopup = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD9D9D9),
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = "TOP RATED",
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Button(
                            onClick = {
                                showCategoryPopup = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD9D9D9),
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = "CATEGORIES",
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }


                    Spacer(
                        modifier =
                            Modifier.height(20.dp)
                    )


                    val clustersList =
                        listOf(
                            "All",
                            "Sangli",
                            "Ichalkaranji",
                            "Kolhapur",
                            "Belgav"
                        )

                    // 2x2 Grid Layout for Explore by Location
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
                                        },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEBEBEB)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.Start
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = Color.Black.copy(alpha = 0.6f),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = clusterName,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.Black
                                            )
                                            Text(
                                                text = "Explore services",
                                                fontSize = 10.sp,
                                                color = Color.Gray,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
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
                                        },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEBEBEB)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.Start
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = Color.Black.copy(alpha = 0.6f),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = clusterName,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.Black
                                            )
                                            Text(
                                                text = "Explore services",
                                                fontSize = 10.sp,
                                                color = Color.Gray,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
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

            title = "Top Rated Providers",

            onClose = {
                showTopRatedPopup = false
            },

            onBookClick = onBookClick,

            userLat = userLat,

            userLng = userLng
        )
    }

    if (selectedClusterForPopup != null) {
        val clusterName = selectedClusterForPopup!!
        val clusterProvidersList = remember(clusterName, providers) {
            providers.filter { provider ->
                val searchTerms = when (clusterName.lowercase()) {
                    "belgav", "belgaum" -> listOf("belgav", "belgaum")
                    "ichalkaranji", "shilkaranji", "ichalkarnji" -> listOf("ichalkaranji", "shilkaranji", "ichalkarnji")
                    else -> listOf(clusterName.lowercase())
                }
                searchTerms.any { term ->
                    provider.city.contains(term, ignoreCase = true) ||
                    provider.area.contains(term, ignoreCase = true) ||
                    provider.fullAddress.contains(term, ignoreCase = true)
                }
            }.sortedByDescending { it.rating }
        }

        TopRatedPopup(
            providers = clusterProvidersList,
            isLoading = isLoading,
            title = "Providers in $clusterName",
            onClose = {
                selectedClusterForPopup = null
            },
            onBookClick = onBookClick,
            userLat = userLat,
            userLng = userLng
        )
    }

    if (showCategoryPopup) {
        CategoryPopup(
            providers = providers,
            isLoading = isLoading,
            onClose = {
                showCategoryPopup = false
            },
            onBookClick = onBookClick,
            userLat = userLat,
            userLng = userLng
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
            val imageUrl = dynamicProfileImageUrl ?: provider.profileImage ?: provider.profileImageUrl
            if (!imageUrl.isNullOrBlank()) {
                Image(
                    painter = coil.compose.rememberAsyncImagePainter(imageUrl),
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
    onQueryChange: (String) -> Unit,
    providers: List<Provider> = emptyList()
) {
    var isFocused by remember { mutableStateOf(false) }

    val suggestions = remember(query, providers) {
        if (query.isBlank()) {
            emptyList()
        } else {
            val list = mutableListOf<String>()
            
            // 1. Match categories / serviceTypes
            providers.map { it.serviceType.trim() }
                .distinctBy { it.lowercase() }
                .filter { it.contains(query, ignoreCase = true) }
                .forEach { list.add(it) }

            // 2. Match provider names
            providers.map { it.name.trim() }
                .distinctBy { it.lowercase() }
                .filter { it.contains(query, ignoreCase = true) }
                .forEach { list.add(it) }

            // 3. Smart phrase queries (e.g. "Top Rated <Service>")
            providers.map { it.serviceType.trim() }
                .distinctBy { it.lowercase() }
                .filter { it.contains(query, ignoreCase = true) }
                .forEach {
                    list.add("Top Rated $it")
                }

            list.distinct().take(5)
        }
    }

    Box(modifier = Modifier.fillMaxWidth().zIndex(10f)) {
        Column {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused },
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
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.LightGray,
                    focusedLabelColor = Color.Black
                )
            )

            // Dynamic suggestions dropdown list overlay
            if (isFocused && suggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        suggestions.forEach { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val cleanQuery = if (suggestion.startsWith("Top Rated ")) {
                                            suggestion.removePrefix("Top Rated ")
                                        } else {
                                            suggestion
                                        }
                                        onQueryChange(cleanQuery)
                                        isFocused = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = suggestion,
                                    color = Color.Black,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
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

@OptIn(UnstableApi::class)
@Composable
fun BannerVideoPlayer(
    videoResId: Int,
    onVideoCompleted: () -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri("android.resource://${context.packageName}/$videoResId")
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_OFF
            playWhenReady = true
            volume = 1f // Enable video audio so user can hear banner music/sound
            prepare()

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        onVideoCompleted()
                    }
                }
            })
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

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false // Hide controls for banner
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

    val currentItem = items[pagerState.currentPage]
    LaunchedEffect(
        pagerState.currentPage
    ) {
        // If it is a video, do not auto-scroll by time.
        // It will scroll dynamically when the video finishes playing.
        if (!currentItem.isVideo) {
            delay(4000)
            coroutineScope.launch {
                pagerState.animateScrollToPage(
                    (pagerState.currentPage + 1)
                            % items.size
                )
            }
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
            if (item.isVideo && item.localVideoRes != null) {
                BannerVideoPlayer(
                    videoResId = item.localVideoRes,
                    onVideoCompleted = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                (pagerState.currentPage + 1)
                                        % items.size
                            )
                        }
                    }
                )
            } else {
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
}

@Composable
fun TopRatedPopup(

    providers: List<Provider>,

    isLoading: Boolean,

    title: String = "Top Rated Providers",

    onClose: () -> Unit,

    onBookClick: (String) -> Unit,

    userLat: Double? = null,

    userLng: Double? = null
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

                        title,

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
                                    onBookClick,

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

@Composable
fun CategoryPopup(
    providers: List<Provider>,
    isLoading: Boolean,
    onClose: () -> Unit,
    onBookClick: (String) -> Unit,
    userLat: Double? = null,
    userLng: Double? = null
) {
    var selectedCategory by remember { mutableStateOf("") }
    val categories = remember(providers) {
        providers.map { it.serviceType.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .sorted()
    }

    val categoryProviders = remember(selectedCategory, providers, userLat, userLng) {
        if (selectedCategory.isBlank()) {
            emptyList()
        } else {
            providers.filter { it.serviceType.trim().equals(selectedCategory.trim(), ignoreCase = true) }
                .sortedWith(
                    compareBy<Provider> { provider ->
                        if (userLat != null && userLng != null && userLat != 0.0 && userLng != 0.0 && provider.latitude != 0.0 && provider.longitude != 0.0) {
                            val distance = calculateDistance(userLat, userLng, provider.latitude, provider.longitude)
                            if (distance <= 18000) 0 else 1
                        } else {
                            1
                        }
                    }.thenBy { provider ->
                        if (userLat != null && userLng != null && userLat != 0.0 && userLng != 0.0 && provider.latitude != 0.0 && provider.longitude != 0.0) {
                            calculateDistance(userLat, userLng, provider.latitude, provider.longitude)
                        } else {
                            Float.MAX_VALUE
                        }
                    }.thenByDescending { provider ->
                        provider.rating
                    }.thenBy { provider ->
                        provider.name.lowercase()
                    }
                )
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (selectedCategory.isNotEmpty()) {
                        IconButton(onClick = { selectedCategory = "" }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                    Text(
                        text = if (selectedCategory.isEmpty()) "Select Category" else selectedCategory,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.Black)
                    }
                } else {
                    if (selectedCategory.isEmpty()) {
                        // Categories Grid List
                        if (categories.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No categories found", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(categories) { category ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1.2f)
                                            .clickable { selectedCategory = category },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFFF0F0F0),
                                            contentColor = Color.Black
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize().padding(12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = category,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Respective Providers Grid List sorted
                        if (categoryProviders.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No providers under this category", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(categoryProviders) { provider ->
                                    ProviderGridCard(
                                        provider = provider,
                                        onBookClick = onBookClick,
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
    }
}

