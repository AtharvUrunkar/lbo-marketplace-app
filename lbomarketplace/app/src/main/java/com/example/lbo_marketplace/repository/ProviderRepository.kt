package com.example.lbo_marketplace.data.repository

import com.example.lbo_marketplace.data.model.Provider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProviderRepository {

    private val db = FirebaseFirestore.getInstance()

    // 🔥 APPLY FUNCTION (you already have)
    suspend fun applyForProviderWithDetails(
        userId: String,
        email: String,
        name: String,
        serviceType: String,
        description: String,
        experience: String,
        latitude: Double,
        longitude: Double
    ): Result<String> {
        return try {
            val data = hashMapOf(
                "userId" to userId,
                "email" to email,
                "name" to name,
                "serviceType" to serviceType,
                "description" to description,
                "experience" to experience,
                "latitude" to latitude,
                "longitude" to longitude,
                "status" to "PENDING"
            )

            db.collection("provider_requests").add(data).await()

            Result.success("Application Submitted")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔥 THIS IS WHAT YOU ARE MISSING
    suspend fun getApprovedProviders(): List<Provider> {

        val snapshot = db.collection("provider_requests")
            .whereEqualTo("status", "APPROVED")
            .get()
            .await()

        return snapshot.documents.map {
            Provider(
                id = it.id,
                name = it.getString("name") ?: "",
                serviceType = it.getString("serviceType") ?: "",
                status = it.getString("status") ?: ""
            )
        }
    }
}