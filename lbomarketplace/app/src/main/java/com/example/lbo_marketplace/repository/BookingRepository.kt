package com.example.lbo_marketplace.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import com.example.lbo_marketplace.repository.NotificationRepository

class BookingRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun createBooking(
        data: Map<String, Any>
    ): Result<String> {

        return try {

            val docRef =

                db.collection("bookings")
                    .document()

            val bookingId = docRef.id

            val bookingData =
                data.toMutableMap()

            bookingData["bookingId"] =
                bookingId

            // =====================================================
            // 🔥 FETCH PROVIDER INFO
            // =====================================================

            val inputProviderId =

                data["providerId"]
                        as? String ?: ""

            var providerPlayerId = ""

            if (inputProviderId.isNotEmpty()) {

                try {

                    val pDoc =

                        db.collection(
                            "provider_requests"
                        )

                            .document(
                                inputProviderId
                            )

                            .get()

                            .await()

                    if (pDoc.exists()) {

                        val actualUid =

                            pDoc.getString(
                                "userId"
                            ) ?: inputProviderId

                        val pEmail =

                            pDoc.getString(
                                "email"
                            ) ?: ""

                        bookingData["providerUid"] =
                            actualUid

                        if (pEmail.isNotEmpty()) {

                            bookingData["providerEmail"] =
                                pEmail
                        }

                        // =====================================================
                        // 🔥 FETCH PROVIDER PLAYER ID
                        // =====================================================

                        try {

                            val userDoc =

                                db.collection("users")

                                    .document(actualUid)

                                    .get()

                                    .await()

                            providerPlayerId =

                                userDoc.getString(
                                    "oneSignalPlayerId"
                                ) ?: ""

                        } catch (e: Exception) {

                            e.printStackTrace()
                        }
                    }

                } catch (e: Exception) {

                    e.printStackTrace()
                }
            }

            // =====================================================
            // 🔥 SAVE BOOKING
            // =====================================================

            docRef.set(bookingData)
                .await()

            // =====================================================
            // 🔥 SEND PROVIDER NOTIFICATION
            // =====================================================

            if (providerPlayerId.isNotEmpty()) {

                try {

                    val notificationRepository =
                        NotificationRepository()

                    val customerName =

                        data["customerName"]
                                as? String ?: "New User"

                    notificationRepository
                        .sendNotification(

                            providerPlayerId,

                            "New Booking Request 🚀",

                            "$customerName sent you a booking request"
                        )

                } catch (e: Exception) {

                    e.printStackTrace()
                }
            }

            Result.success(
                "Booking Created"
            )

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
            // Resolve provider email for email-based fallback query
            var providerEmail = ""
            try {
                val userDoc = db.collection("users").document(providerId).get().await()
                providerEmail = userDoc.getString("email") ?: ""
            } catch (e: Exception) {
                // ignore
            }
            if (providerEmail.isEmpty()) {
                try {
                    val reqDoc = db.collection("provider_requests").document(providerId).get().await()
                    providerEmail = reqDoc.getString("email") ?: ""
                } catch (e: Exception) {
                    // ignore
                }
            }

            // 1. Query by providerId (standard UID or random approved ID)
            val query1 = db.collection("bookings")
                .whereEqualTo("providerId", providerId)
                .get().await().documents.mapNotNull { it.data }

            // 2. Query by resolved providerUid field (authentic UID fallback)
            val query2 = db.collection("bookings")
                .whereEqualTo("providerUid", providerId)
                .get().await().documents.mapNotNull { it.data }

            // 3. Query by providerEmail field (email fallback)
            val query3 = if (providerEmail.isNotEmpty()) {
                db.collection("bookings")
                    .whereEqualTo("providerEmail", providerEmail)
                    .get().await().documents.mapNotNull { it.data }
            } else {
                emptyList()
            }

            // Merge queries, deduplicate by bookingId, and sort by createdAt descending
            val mergedBookings = (query1 + query2 + query3)
                .distinctBy { it["bookingId"] as? String ?: it.hashCode().toString() }
                .sortedByDescending { it["createdAt"] as? Long ?: 0L }

            Result.success(mergedBookings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBookingStatus(bookingId: String, status: String): Result<String> {
        return try {
            db.collection("bookings").document(bookingId).update("status", status).await()
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