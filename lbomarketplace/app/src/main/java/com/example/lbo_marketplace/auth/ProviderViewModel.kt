package com.example.lbo_marketplace.auth

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lbo_marketplace.data.model.Provider
import com.example.lbo_marketplace.data.repository.ProviderRepository
import kotlinx.coroutines.launch

class ProviderViewModel : ViewModel() {

    private val repo = ProviderRepository()

    // 🔥 APPLY STATE
    var applyState by mutableStateOf("")
        private set

    // 🔥 PROVIDER LIST
    var providers by mutableStateOf<List<Provider>>(emptyList())
        private set

    // 🔥 LOADING STATE FOR SKELETONS
    var isLoading by mutableStateOf(false)
        private set

    // ---------------- APPLY FUNCTION ----------------
    fun applyWithDetails(
        userId: String,
        email: String,
        name: String,
        serviceType: String,
        description: String,
        experience: String,
        latitude: Double,
        longitude: Double
    ) {
        viewModelScope.launch {
            applyState = "Loading..."

            val result = repo.applyForProviderWithDetails(
                userId,
                email,
                name,
                serviceType,
                description,
                experience,
                latitude,
                longitude
            )

            applyState = result.fold(
                onSuccess = { it },
                onFailure = { it.message ?: "Error" }
            )
        }
    }

    // ---------------- FETCH PROVIDERS ----------------
    fun fetchProviders() {
        viewModelScope.launch {
            isLoading = true
            providers = repo.getApprovedProviders()
            isLoading = false
        }
    }
}