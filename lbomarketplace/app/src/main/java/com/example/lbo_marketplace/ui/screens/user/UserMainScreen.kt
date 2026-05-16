package com.example.lbo_marketplace.ui.screens.user

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lbo_marketplace.auth.AuthViewModel
import com.example.lbo_marketplace.auth.ProviderViewModel
import com.example.lbo_marketplace.booking.BookingViewModel
import com.example.lbo_marketplace.ui.screens.user.chat.ChatScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun UserMainScreen(
    authViewModel: AuthViewModel = viewModel()
) {

    var selectedTab by remember { mutableStateOf(0) }

    // 🔥 APPLY FLOW
    var showApplyScreen by remember { mutableStateOf(false) }

    // 🔥 BOOKING FLOW
    var selectedProviderId by remember { mutableStateOf<String?>(null) }

    // 🤖 CHAT FLOW
    var showChatScreen by remember { mutableStateOf(false) }

    val providerViewModel: ProviderViewModel = viewModel()
    val bookingViewModel: BookingViewModel = viewModel()

    val user = FirebaseAuth.getInstance().currentUser

    // ===================== 🤖 CHAT SCREEN =====================
    if (showChatScreen) {
        ChatScreen(
            onBack = { showChatScreen = false }
        )
        return
    }

    // ===================== 🔥 BOOKING SCREEN =====================
    if (selectedProviderId != null) {

        BookingScreen(
            providerId = selectedProviderId!!,
            onSubmit = { problem, address ->

                user?.let {
                    bookingViewModel.book(
                        userId = it.uid,
                        providerId = selectedProviderId!!,
                        problem = problem,
                        address = address
                    )
                }

                // 🔙 BACK AFTER SUBMIT
                selectedProviderId = null
            }
        )
        return
    }

    // ===================== 🔥 APPLY SCREEN =====================
    if (showApplyScreen) {

        ApplyProviderScreen { name, serviceType, description, experience, lat, lng ->

            user?.let {
                providerViewModel.applyWithDetails(
                    userId = it.uid,
                    email = it.email ?: "",
                    name = name,
                    serviceType = serviceType,
                    description = description,
                    experience = experience,
                    latitude = lat,
                    longitude = lng
                )
            }

            showApplyScreen = false
        }
        return
    }

    // ===================== 🔥 MAIN UI =====================
    Scaffold(
        bottomBar = {
            NavigationBar {

                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Search, null) },
                    label = { Text("Services") }
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.List, null) },
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
        // 🤖 CHAT FAB
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showChatScreen = true },
                shape = CircleShape,
                containerColor = Color(0xFF6C63FF),
                contentColor = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    Icons.Default.Chat,
                    contentDescription = "AI Chat Assistant"
                )
            }
        }
    ) { padding ->

        Box(modifier = Modifier.padding(padding)) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                }, label = ""
            ) { targetTab ->
                when (targetTab) {
                    0 -> HomeTab()
                    1 -> ServicesTab(
                        onBookClick = { providerId ->
                            selectedProviderId = providerId
                        }
                    )
                    2 -> BookingTab()
                    3 -> ProfileTab(
                        authViewModel = authViewModel,
                        onApplyClick = {
                            showApplyScreen = true
                        }
                    )
                }
            }
        }
    }
}