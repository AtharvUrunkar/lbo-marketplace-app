package com.example.lbo_marketplace.ui.screens.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lbo_marketplace.auth.AuthViewModel

/**
 * Provider Dashboard with Notifications/Community Tab.
 * 
 * UPDATES:
 * - Merged Work and History into a single tab under "Work" using the List/History icon.
 * - Reduced to exactly 4 modern navigation tabs.
 * - Forced Full White Background.
 */
@Composable
fun ProviderDashboard(
    authViewModel: AuthViewModel = viewModel()
) {

    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = Color.White, // ✅ FULL BRIGHT WHITE
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Work") }, // ✅ HISTORY LIST ICON
                    label = { Text("Work") }
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Community") }, // ✅ BELL ICON
                    label = { Text("Community") }
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { padding ->

        Box(modifier = Modifier.padding(padding).background(Color.White)) {
            when (selectedTab) {
                0 -> HomeScreen()
                1 -> WorkScreen()
                2 -> CommunityScreen()
                3 -> ProfileScreen(authViewModel)
            }
        }
    }
}