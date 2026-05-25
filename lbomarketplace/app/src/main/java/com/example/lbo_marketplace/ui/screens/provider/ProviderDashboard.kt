package com.example.lbo_marketplace.ui.screens.provider

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lbo_marketplace.auth.AuthViewModel

/**
 * Provider Dashboard Navigation Shell.
 *
 * TABS:
 *  0 → Dashboard  (Home – mirrors user HomeTab with banner + bookings)
 *  1 → Updates    (renamed from History – pending/confirmed + completed jobs)
 *  2 → Community  (renamed from Activity – same notifications UI)
 *  3 → Profile    (matches user Profile, no "Become Provider" button, shareable card)
 */
@Composable
fun ProviderDashboard(
    authViewModel: AuthViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Color.Black,
                        indicatorColor = Color(0xFFF0F0F0),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Updates") },
                    label = { Text("Updates") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Color.Black,
                        indicatorColor = Color(0xFFF0F0F0),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Community") },
                    label = { Text("Community") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Color.Black,
                        indicatorColor = Color(0xFFF0F0F0),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Color.Black,
                        indicatorColor = Color(0xFFF0F0F0),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .background(Color.White)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "ProviderTabTransition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> ProviderHomeTab()
                    1 -> UpdatesScreen()
                    2 -> CommunityScreen()
                    3 -> ProviderProfileScreen(authViewModel)
                }
            }
        }
    }
}