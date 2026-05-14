package com.example.lbo_marketplace.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lbo_marketplace.auth.ProviderViewModel
import com.example.lbo_marketplace.data.model.Provider

@Composable
fun HomeTab(
    viewModel: ProviderViewModel = viewModel()
) {

    val providers = viewModel.providers

    // 🔥 Fetch providers once
    LaunchedEffect(Unit) {
        viewModel.fetchProviders()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("👋 Welcome", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Top Providers ⭐", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(10.dp))

        if (providers.isEmpty()) {
            Text("No providers available")
        }

        // 🔥 TAKE TOP 3 (you can improve later)
        val topProviders = providers.take(3)

        topProviders.forEach { provider: Provider ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
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

                    Text("⭐ Top Rated") //   placeholder (later dynamic)
                }
            }
        }
    }
}