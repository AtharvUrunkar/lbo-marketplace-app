package com.example.lbo_marketplace.data.repository

import com.example.lbo_marketplace.data.model.Provider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProviderRepository {

    private val db = FirebaseFirestore.getInstance()

    // =========================================================
    // 🔥 APPLY FOR PROVIDER WITH FULL DETAILS
    // =========================================================

    suspend fun applyForProviderWithDetails(
        userId: String,
        email: String,
        name: String,
        serviceType: String,
        description: String,
        experience: String,
        latitude: Double,
        longitude: Double,
        verificationDocUrl: String
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

                // 🔥 CLOUDINARY URLS

                "verificationDocUrl" to verificationDocUrl,

                // 🔥 STATUS
                "status" to "PENDING",

                // 🔥 TIMESTAMP
                "createdAt" to System.currentTimeMillis()
            )

            db.collection("provider_requests")
                .document(userId)
                .set(data)
                .await()

            Result.success(
                "Application submitted successfully"
            )

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    // =========================================================
    // 🔥 FETCH APPROVED PROVIDERS
    // =========================================================

    suspend fun getApprovedProviders():
            Result<List<Provider>> {

        return try {

            val snapshot = db.collection("provider_requests")
                .whereEqualTo("status", "APPROVED")
                .get()
                .await()

            val providers = snapshot.documents.mapNotNull { doc ->

                Provider(
                    id = doc.id,

                    name = doc.getString("name") ?: "",

                    serviceType =
                        doc.getString("serviceType") ?: "",

                    description =
                        doc.getString("description") ?: "",

                    experience =
                        doc.getString("experience") ?: "",

                    latitude =
                        doc.getDouble("latitude") ?: 0.0,

                    longitude =
                        doc.getDouble("longitude") ?: 0.0,


                    verificationDocUrl =
                        doc.getString("verificationDocUrl") ?: "",

                    rating =
                        doc.getDouble("rating") ?: 0.0
                )
            }

            Result.success(providers)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}