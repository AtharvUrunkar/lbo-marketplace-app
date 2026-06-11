package com.example.lbo_marketplace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.lbo_marketplace.auth.AuthSessionTestScreen
import com.example.lbo_marketplace.ui.theme.LbomarketplaceTheme
import com.example.lbo_marketplace.ui.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    // Lifecycle method executed when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        // Call the parent activity's onCreate method
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge content display on the screen
        enableEdgeToEdge()

        // Check if the running device version is Android 13 (API 33, Tiramisu) or above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Check if POST_NOTIFICATIONS permission has already been granted by the user
            val permissionCheck = androidx.core.content.ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            // If the permission is not yet granted, request it dynamically from the user
            if (permissionCheck != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    101 // Request code for notification permission callback identification
                )
            }
        }

        // Set Jetpack Compose UI content for the activity
        setContent {
            // Apply the custom theme styling to the layout
            LbomarketplaceTheme {
                // Scaffold layout structure filling the entire screen modifier size
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    // Render the main application navigation graph
                    AppNavigation()
                }
            }
        }
    }
}
