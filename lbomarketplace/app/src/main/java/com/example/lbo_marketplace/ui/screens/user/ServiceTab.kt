package com.example.lbo_marketplace.ui.screens.user

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.lbo_marketplace.auth.ProviderViewModel

@Composable
fun ServicesTab(
    onBookClick: (String) -> Unit,
    viewModel: ProviderViewModel = viewModel()
) {

    // =========================================================
    // 🔥 PROVIDERS
    // =========================================================

    val providers = viewModel.providers

    // =========================================================
    // 🔥 SEARCH QUERY
    // =========================================================

    var searchQuery by remember {
        mutableStateOf("")
    }

    // =========================================================
    // 🔥 FETCH PROVIDERS
    // =========================================================

    LaunchedEffect(Unit) {
        viewModel.fetchProviders()
    }

    // =========================================================
    // 🔥 FILTERED PROVIDERS
    // =========================================================

    val filteredProviders = providers.filter {

        it.serviceType.contains(
            searchQuery,
            ignoreCase = true
        ) ||

                it.name.contains(
                    searchQuery,
                    ignoreCase = true
                )
    }

    // =========================================================
    // 🔥 UI
    // =========================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // =====================================================
        // 🔥 TITLE
        // =====================================================

        Text(
            text = "Available Providers",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        // =====================================================
        // 🔥 SEARCH BAR
        // =====================================================

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
            },
            label = {
                Text(
                    "Search service (e.g. Electrician)"
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // =====================================================
        // 🔥 EMPTY STATE
        // =====================================================
        // 🔥 FILTER LOGIC
        val filteredProviders = providers.filter {
            it.serviceType.contains(searchQuery, ignoreCase = true) ||
                    it.name.contains(searchQuery, ignoreCase = true)
        }

        if (filteredProviders.isEmpty()) {

            Box(
                modifier = Modifier.fillMaxSize()
            ) {

                Text(
                    text = "No providers found"
                )
            }

        } else {

            // =================================================
            // 🔥 PROVIDER LIST
            // =================================================

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {

                items(filteredProviders) { provider ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            // =============================
                            // 🔥 PROVIDER IMAGE
                            // =============================

                            AsyncImage(
                                model = provider.profileImageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            // =============================
                            // 🔥 PROVIDER NAME
                            // =============================

                            Text(
                                text = provider.name,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            // =============================
                            // 🔥 SERVICE TYPE
                            // =============================

                            Text(
                                text = provider.serviceType,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (filteredProviders.isEmpty()) {
                                Text("No providers found")
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(filteredProviders) { provider ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                    provider.name,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                Text(
                                                    provider.serviceType,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )

                                                Spacer(
                                                    modifier = Modifier.height(6.dp)
                                                )

                                                // =============================
                                                // 🔥 EXPERIENCE
                                                // =============================

                                                Text(
                                                    text = "Experience: ${provider.experience}"
                                                )

                                                Spacer(
                                                    modifier = Modifier.height(6.dp)
                                                )

                                                // =============================
                                                // 🔥 DESCRIPTION
                                                // =============================

                                                Text(
                                                    text = provider.description
                                                )

                                                Spacer(
                                                    modifier = Modifier.height(16.dp)
                                                )

                                                // =============================
                                                // 🔥 BOOK BUTTON
                                                // =============================

                                                Button(
                                                    onClick = {
                                                        onBookClick(provider.id)
                                                    },
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {

                                                    Text("Book Now")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}