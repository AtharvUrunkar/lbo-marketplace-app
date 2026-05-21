package com.example.lbo_marketplace.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class BookingRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun createBooking(data: Map<String, Any>): Result<String> {
        return try {
            val docRef = db.collection("bookings").document()
            val bookingId = docRef.id
            val bookingData = data.toMutableMap()
            bookingData["bookingId"] = bookingId
            docRef.set(bookingData).await()
            Result.success("Booking Created")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCustomerBookings(customerId: String): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = db.collection("bookings")
                .whereEqualTo("customerId", customerId)
                .get().await()

            val bookings = snapshot.documents.mapNotNull { it.data }
                .sortedByDescending { it["createdAt"] as? Long ?: 0L }
            Result.success(bookings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProviderBookings(providerId: String): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = db.collection("bookings")
                .whereEqualTo("providerId", providerId)
                .get().await()

            val bookings = snapshot.documents.mapNotNull { it.data }
                .sortedByDescending { it["createdAt"] as? Long ?: 0L }
            Result.success(bookings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBookingStatus(bookingId: String, status: String): Result<String> {
        return try {
            db.collection("bookings").document(bookingId)
                .update("status", status).await()
            Result.success("Booking $status")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeBookingWithFeedback(
        bookingId: String,
        providerId: String,
        rating: Float,
        feedback: String
    ): Result<String> {
        return try {
            // Update booking document
            db.collection("bookings").document(bookingId).update(
                mapOf(
                    "status" to "COMPLETED",
                    "rating" to rating.toDouble(),
                    "feedback" to feedback
                )
            ).await()

            // Fetch all completed bookings for this provider to recalculate rating
            val snapshot = db.collection("bookings")
                .whereEqualTo("providerId", providerId)
                .whereEqualTo("status", "COMPLETED")
                .get().await()
                
            val completedBookings = snapshot.documents.mapNotNull { it.data }
            var totalRating = 0.0
            var count = 0
            
            for (b in completedBookings) {
                val r = (b["rating"] as? Number)?.toDouble()
                if (r != null) {
                    totalRating += r
                    count++
                }
            }
            
            val newAvgRating = if (count > 0) totalRating / count else 0.0
            
            // Update provider profile
            db.collection("provider_requests").document(providerId).update(
                "rating", newAvgRating
            ).await()

            Result.success("Feedback submitted")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}