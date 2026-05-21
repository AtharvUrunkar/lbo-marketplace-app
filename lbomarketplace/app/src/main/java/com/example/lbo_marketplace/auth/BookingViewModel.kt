package com.example.lbo_marketplace.booking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lbo_marketplace.data.repository.BookingRepository
import kotlinx.coroutines.launch

class BookingViewModel : ViewModel() {

    private val repo = BookingRepository()

    var state by mutableStateOf("")
        private set

    var customerBookings by mutableStateOf<List<Map<String, Any>>>(emptyList())
        private set

    var providerBookings by mutableStateOf<List<Map<String, Any>>>(emptyList())
        private set

    fun book(
        customerId: String,
        customerName: String,
        customerPhone: String,
        providerId: String,
        providerName: String,
        problemTitle: String,
        problemDescription: String,
        address: String,
        preferredDate: String,
        preferredTime: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            state = "Loading..."

            val data = hashMapOf(
                "customerId" to customerId,
                "customerName" to customerName,
                "customerPhone" to customerPhone,
                "providerId" to providerId,
                "providerName" to providerName,
                "problemTitle" to problemTitle,
                "problemDescription" to problemDescription,
                "address" to address,
                "preferredDate" to preferredDate,
                "preferredTime" to preferredTime,
                "status" to "PENDING",
                "createdAt" to System.currentTimeMillis()
            )

            val result = repo.createBooking(data)

            state = result.fold(
                onSuccess = { 
                    onSuccess()
                    it 
                },
                onFailure = { it.message ?: "Error" }
            )
        }
    }

    fun loadCustomerBookings(customerId: String) {
        viewModelScope.launch {
            val result = repo.getCustomerBookings(customerId)
            customerBookings = result.getOrElse { emptyList() }
        }
    }

    fun loadProviderBookings(providerId: String) {
        viewModelScope.launch {
            val result = repo.getProviderBookings(providerId)
            providerBookings = result.getOrElse { emptyList() }
        }
    }

    fun updateBookingStatus(bookingId: String, status: String, providerId: String, customerId: String? = null) {
        viewModelScope.launch {
            repo.updateBookingStatus(bookingId, status)
            loadProviderBookings(providerId)
            customerId?.let { loadCustomerBookings(it) }
        }
    }

    fun completeBookingWithFeedback(
        bookingId: String,
        providerId: String,
        customerId: String,
        rating: Float,
        feedback: String
    ) {
        viewModelScope.launch {
            repo.completeBookingWithFeedback(bookingId, providerId, rating, feedback)
            loadCustomerBookings(customerId)
        }
    }
}