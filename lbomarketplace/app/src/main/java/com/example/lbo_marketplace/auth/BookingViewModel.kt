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

    fun book(
        userId: String,
        providerId: String,
        problem: String,
        address: String
    ) {

        viewModelScope.launch {

            state = "Loading..." // 🔥 UX improvement

            val data = hashMapOf(
                "userId" to userId,
                "providerId" to providerId,
                "problem" to problem,
                "address" to address,
                "status" to "PENDING",
                "price" to 300
            )

            val result = repo.createBooking(data)

            state = result.fold(
                onSuccess = { it },
                onFailure = { it.message ?: "Error" }
            )
        }
    }
}