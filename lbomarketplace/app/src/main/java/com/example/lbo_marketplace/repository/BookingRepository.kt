package com.example.lbo_marketplace.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BookingRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun createBooking(data: Map<String, Any>): Result<String> {
        return try {
            db.collection("bookings").add(data).await()
            Result.success("Booking Created")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}