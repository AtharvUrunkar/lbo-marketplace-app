package com.example.lbo_marketplace.ui.screens.provider

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lbo_marketplace.R
import com.example.lbo_marketplace.auth.AuthViewModel

/**
 * Provider Dashboard Shell.
 *
 * Implements a unified container for 4 provider screens:
 *   - Tab 0: Home (DashboardScreen) - discovery, popups, banner.
 *   - Tab 1: Updates (UpdatesScreen) - requests, accept/reject, history.
 *   - Tab 2: Community (CommunityScreen) - community notifications.
 *   - Tab 3: Profile (ProviderProfileScreen) - verified card, cloudinary settings.
 */
@Composable
fun ProviderDashboard(
    authViewModel: AuthViewModel = viewModel()
) {

    var selectedTab by remember {
        mutableStateOf(0)
    }

    var menuExpanded by remember {
        mutableStateOf(false)
    }

    var showAboutDialog by remember {
        mutableStateOf(false)
    }

    var showFAQDialog by remember {
        mutableStateOf(false)
    }

    var showHelpDialog by remember {
        mutableStateOf(false)
    }

    @Composable
    fun GlobalHeader() {

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically

        ) {

            Spacer(
                modifier =
                    Modifier.width(48.dp)
            )

            Box(

                modifier =
                    Modifier
                        .weight(1f)
                        .height(50.dp),

                contentAlignment =
                    Alignment.Center

            ) {

                Image(

                    painter =
                        painterResource(
                            id = R.drawable.logo
                        ),

                    contentDescription = "Logo",

                    modifier =
                        Modifier.height(40.dp),

                    contentScale =
                        ContentScale.Fit
                )
            }

            Box(
                contentAlignment = Alignment.TopEnd
            ) {

                IconButton(
                    onClick = {
                        menuExpanded = true
                    }
                ) {

                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.Black
                    )
                }

                DropdownMenu(

                    expanded = menuExpanded,

                    onDismissRequest = {
                        menuExpanded = false
                    },

                    shape =
                        RoundedCornerShape(16.dp),

                    modifier =
                        Modifier.background(
                            Color.White
                        )

                ) {

                    DropdownMenuItem(

                        text = {

                            Text(
                                text = "Switch to Customer Mode",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        },

                        onClick = {
                            menuExpanded = false
                            authViewModel.isProviderInCustomerMode.value = true
                        }
                    )

                    DropdownMenuItem(

                        text = {

                            Text(
                                text = "About",
                                fontWeight = FontWeight.Bold
                            )
                        },

                        onClick = {
                            menuExpanded = false
                            showAboutDialog = true
                        }
                    )

                    DropdownMenuItem(

                        text = {

                            Text(
                                text = "FAQ",
                                fontWeight = FontWeight.Bold
                            )
                        },

                        onClick = {
                            menuExpanded = false
                            showFAQDialog = true
                        }
                    )

                    DropdownMenuItem(

                        text = {

                            Text(
                                text = "Help",
                                color = Color(0xFF6C63FF),
                                fontWeight = FontWeight.Bold
                            )
                        },

                        onClick = {
                            menuExpanded = false
                            showHelpDialog = true
                        }
                    )
                }
            }
        }
    }

    Scaffold(

        containerColor = Color.White,

        bottomBar = {

            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {

                NavigationBarItem(

                    selected =
                        selectedTab == 0,

                    onClick = {
                        selectedTab = 0
                    },

                    icon = {

                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home"
                        )
                    },

                    label = {
                        Text("Home")
                    },

                    colors =
                        NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = Color.Black,
                            indicatorColor = Color(0xFFEEEEEE),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                )

                NavigationBarItem(

                    selected =
                        selectedTab == 1,

                    onClick = {
                        selectedTab = 1
                    },

                    icon = {

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "Updates"
                        )
                    },

                    label = {
                        Text("Updates")
                    },

                    colors =
                        NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = Color.Black,
                            indicatorColor = Color(0xFFEEEEEE),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                )

                NavigationBarItem(

                    selected =
                        selectedTab == 2,

                    onClick = {
                        selectedTab = 2
                    },

                    icon = {

                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Community"
                        )
                    },

                    label = {
                        Text("Community")
                    },

                    colors =
                        NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = Color.Black,
                            indicatorColor = Color(0xFFEEEEEE),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                )

                NavigationBarItem(

                    selected =
                        selectedTab == 3,

                    onClick = {
                        selectedTab = 3
                    },

                    icon = {

                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile"
                        )
                    },

                    label = {
                        Text("Profile")
                    },

                    colors =
                        NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = Color.Black,
                            indicatorColor = Color(0xFFEEEEEE),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                )
            }
        }

    ) { paddingValues ->

        Box(

            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(
                        bottom =
                            paddingValues.calculateBottomPadding()
                    )

        ) {

            AnimatedContent(

                targetState = selectedTab,

                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(300)
                    ) togetherWith
                            fadeOut(
                                animationSpec = tween(300)
                            )
                },

                label = "TabTransition"

            ) { tab ->

                when (tab) {

                    0 ->
                        DashboardScreen(
                            header = {
                                GlobalHeader()
                            }
                        )

                    1 ->
                        UpdatesScreen(
                            header = {
                                GlobalHeader()
                            }
                        )

                    2 ->
                        CommunityScreen(
                            header = {
                                GlobalHeader()
                            }
                        )

                    3 ->
                        ProviderProfileScreen(

                            header = {
                                GlobalHeader()
                            },

                            authViewModel = authViewModel
                        )
                }
            }
        }
    }

    if (showAboutDialog) {

        AlertDialog(

            onDismissRequest = {
                showAboutDialog = false
            },

            title = {

                Text(
                    text = "About LBO",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Text(
                    text = "LBO – Together We Grow 🤝\n\nConnecting local experts with our community seamlessly."
                )
            },

            confirmButton = {

                TextButton(

                    onClick = {
                        showAboutDialog = false
                    },

                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = Color.Black
                        )

                ) {

                    Text("Close")
                }
            },

            shape =
                RoundedCornerShape(24.dp),

            containerColor = Color.White
        )
    }

    if (showFAQDialog) {

        AlertDialog(

            onDismissRequest = {
                showFAQDialog = false
            },

            title = {

                Text(
                    text = "FAQ",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Text(
                    text = "Q: How do I manage requests?\nA: Tap 'Updates' tab to Accept or Reject pending bookings.\n\nQ: How do I update details?\nA: Tap 'Profile' tab and choose 'Edit' to change your services or geo-coordinates."
                )
            },

            confirmButton = {

                TextButton(

                    onClick = {
                        showFAQDialog = false
                    },

                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = Color.Black
                        )

                ) {

                    Text("Close")
                }
            },

            shape =
                RoundedCornerShape(24.dp),

            containerColor = Color.White
        )
    }

    val uriHandler =
        androidx.compose.ui.platform.LocalUriHandler.current

    if (showHelpDialog) {

        AlertDialog(

            onDismissRequest = {
                showHelpDialog = false
            },

            title = {

                Text(
                    text = "Help & Support",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Text(
                    text = "If you have some doubts contact admin or refered person"
                )
            },

            confirmButton = {

                Button(

                    onClick = {
                        showHelpDialog = false
                        uriHandler.openUri("mailto:lbo.org.ask@gmail.com")
                    },

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color.Black
                        )

                ) {

                    Text("Contact")
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {
                        showHelpDialog = false
                    },

                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = Color.Black
                        )

                ) {

                    Text("Close")
                }
            },

            shape =
                RoundedCornerShape(24.dp),

            containerColor = Color.White
        )
    }
}