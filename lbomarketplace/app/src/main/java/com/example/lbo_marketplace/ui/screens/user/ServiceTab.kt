package com.example.lbo_marketplace.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lbo_marketplace.auth.ProviderViewModel

@Composable
fun ServicesTab(
    onBookClick: (String) -> Unit,
    viewModel: ProviderViewModel = viewModel()
) {

    val providers = viewModel.providers

    // 🔥 ADD HERE (TOP INSIDE COMPOSABLE)
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchProviders()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // 🔥 SEARCH BAR (ADD BELOW TITLE)
        Text("Available Providers", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search service (e.g. Electrician)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔥 FILTER LOGIC
        val filteredProviders = providers.filter {
            it.serviceType.contains(searchQuery, ignoreCase = true)
        }

        if (filteredProviders.isEmpty()) {
            Text("No providers found")
        }

        filteredProviders.forEach { provider ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text(provider.name)
                    Text(provider.serviceType)

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(onClick = {
                        onBookClick(provider.id)
                    }) {
                        Text("Book Now")
                    }
                }
            }
        }
    }
}