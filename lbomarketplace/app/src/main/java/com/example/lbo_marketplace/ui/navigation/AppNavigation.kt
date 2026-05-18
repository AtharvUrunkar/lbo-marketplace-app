package com.example.lbo_marketplace.ui.navigation

import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.lbo_marketplace.R
import com.example.lbo_marketplace.auth.*
import com.example.lbo_marketplace.ui.screens.user.UserMainScreen
import com.example.lbo_marketplace.ui.screens.provider.ProviderDashboard
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.media3.common.PlaybackException

/**
 * Main entry point for the Application's Navigation and Session handling.
 */
@Composable
fun AppNavigation(viewModel: AuthViewModel = viewModel()) {

    val state = viewModel.authState.value

    // 🔥 SESSION CHECK: Runs once when the navigation is initialized.
    LaunchedEffect(Unit) {
        viewModel.checkSession()
    }

    Surface(modifier = Modifier.fillMaxSize()) {

        when (state) {

            // STATE: Idle/Loading -> Shows the centered adaptive video logo.
            // Handling Idle explicitly prevents flickering during initial state transition.
            is AuthState.Idle, is AuthState.Loading -> {
                LoadingScreen()
            }

            // STATE: Unauthenticated -> User is not logged in, show Auth Screen.
            is AuthState.Unauthenticated -> {
                AuthSessionTestScreen(viewModel)
            }

            // STATE: Authenticated -> User is logged in, check their role.
            is AuthState.Authenticated -> {

                when (state.role) {

                    // FLOW: User Dashboard
                    "USER" -> UserMainScreen(viewModel)

                    // FLOW: Provider Dashboard
                    "SERVICE_PROVIDER" -> ProviderDashboard()

                    // FLOW: Admin (Restricted on Mobile)
                    "ADMIN" -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Admin panel available on web")
                        }
                    }

                    // FALLBACK: Go to Auth screen if role is unknown.
                    else -> {
                        AuthSessionTestScreen(viewModel)
                    }
                }
            }

            // STATE: Error -> Fallback to login screen.
            is AuthState.Error -> {
                AuthSessionTestScreen(viewModel)
            }

            // STATE: ProviderPending -> Shows review screen.
            is AuthState.ProviderPending -> {
                PendingReviewScreen()
            }

            // DEFAULT fallback.
            else -> {
                AuthSessionTestScreen(viewModel)
            }
        }
    }
}

/**
 * Renders the centered Loading Screen with an MP4 video or PNG fallback.
 */
@Composable
fun LoadingScreen() {
    var useFallback by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!useFallback) {
            // Loop Video (MP4) - Circular container
            Box(
                modifier = Modifier
                    .size(160.dp) // Industry standard for circular branding loaders
                    .clip(CircleShape)
            ) {
                VideoLoader(
                    videoResId = R.raw.logo, // raw/logo.mp4
                    onError = { useFallback = true }
                )
            }
        } else {
            // Fallback Image (PNG) - Circular format to match branding
            Image(
                painter = painterResource(id = R.drawable.logo), // drawable/logo.png
                contentDescription = "LBO Logo",
                modifier = Modifier
                    .size(160.dp) // Square size for circular clipping
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Branding Text
        Text(
            text = "Loading LBO Marketplace...",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Component to play a looping MP4 video using ExoPlayer.
 */
@Composable
fun VideoLoader(videoResId: Int, onError: () -> Unit) {

    val context = LocalContext.current

    val exoPlayer = remember {

        ExoPlayer.Builder(context).build().apply {

            try {

                val uri = android.net.Uri.parse(
                    "android.resource://${context.packageName}/$videoResId"
                )

                setMediaItem(MediaItem.fromUri(uri))

                repeatMode = Player.REPEAT_MODE_ALL

                playWhenReady = true

                prepare()

            } catch (e: Exception) {

                onError()
            }

            addListener(object : Player.Listener {

                override fun onPlayerError(error: PlaybackException) {

                    onError()
                }
            })
        }
    }

    DisposableEffect(Unit) {

        onDispose {

            exoPlayer.release()
        }
    }

    AndroidView(

        factory = {

            PlayerView(context).apply {

                player = exoPlayer

                useController = false

                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },

        modifier = Modifier.fillMaxSize()
    )
}

/**
 * Renders a premium "Under Review" screen for Service Providers.
 */
@Composable
fun PendingReviewScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⏳",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your application is under review",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Our team is verifying your credentials. Please check back later.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Legacy helper for GIF loading (kept for future use if needed).
 */
@Composable
fun GifImage(
    modifier: Modifier = Modifier,
    gifResource: Int
) {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(context)
                .data(data = gifResource)
                .build(),
            imageLoader = imageLoader
        ),
        contentDescription = "Loading...",
        modifier = modifier
    )
}