package com.example.lbo_marketplace.ui.screens.user

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lbo_marketplace.R
import com.example.lbo_marketplace.auth.ProviderViewModel
import com.example.lbo_marketplace.data.model.Provider

@Composable
fun HomeTab(
    viewModel: ProviderViewModel = viewModel()
) {
    val providers = viewModel.providers
    var searchQuery by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }

    // 🔥 Fetch providers once
    LaunchedEffect(Unit) {
        viewModel.fetchProviders()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- Header Section ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left placeholder to keep logo in middle
            Spacer(modifier = Modifier.width(48.dp))

            // Middle Logo (Circular)
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            // Right Menu Icon
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("About") },
                        onClick = { 
                            menuExpanded = false 
                            // Add dummy action
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("FAQ") },
                        onClick = { 
                            menuExpanded = false 
                            // Add dummy action
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Help") },
                        onClick = { 
                            menuExpanded = false 
                            // Add dummy action
                        }
                    )
                }
            }
        }

        // --- Search Bar ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Search services...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = CircleShape,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray
            )
        )

        // --- Existing Content with Animation ---
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text("👋 Welcome", style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Top Providers ⭐", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(10.dp))

                if (providers.isEmpty()) {
                    Text("No providers available")
                }

                // 🔥 TAKE TOP 3
                val topProviders = providers.take(3)

                topProviders.forEach { provider: Provider ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = provider.name,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = provider.serviceType,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text("⭐ Top Rated", color = Color(0xFFFFB400)) 
                        }
                    }
                }
            }
        }
    }
}