package com.example.lbo_marketplace.repository

import com.example.lbo_marketplace.data.model.Provider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProviderRepository {

    private val db = FirebaseFirestore.getInstance()

    // =========================================================
    // 🔥 SUBMIT PROVIDER REQUEST
    // =========================================================

    suspend fun submitProviderRequest(provider: Provider): Result<String> {
        return try {
            val data = mapOf(
                "userId" to provider.id,
                "name" to provider.name,
                "serviceType" to provider.serviceType,
                "description" to provider.description,
                "experience" to provider.experience,
                "latitude" to provider.latitude,
                "longitude" to provider.longitude,
                "verificationDocUrl" to provider.verificationDocUrl,
                "profileImageUrl" to provider.profileImageUrl,
                "status" to "PENDING", // Initial state
                "rating" to 0.0,
                "lastProfileUpdate" to 0L
            )

            db.collection("provider_requests")
                .document(provider.id)
                .set(data)
                .await()

            Result.success("Request submitted successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================
    // 🔥 FETCH ALL APPROVED PROVIDERS
    // =========================================================

    suspend fun fetchAllApprovedProviders(): Result<List<Provider>> {
        return try {
            val querySnapshot = db.collection("provider_requests")
                .whereEqualTo("status", "APPROVED")
                .get()
                .await()

            val providerList = querySnapshot.documents.mapNotNull { doc ->
                var pImage = doc.getString("profileImageUrl")
                if (pImage.isNullOrBlank()) {
                    pImage = doc.getString("profileImage")
                }
                if (pImage.isNullOrBlank()) {
                    try {
                        val userDoc = db.collection("users").document(doc.id).get().await()
                        pImage = userDoc.getString("profileImageUrl")
                    } catch (e: Exception) {
                        // ignore
                    }
                }

                Provider(
                    id = doc.getString("userId") ?: doc.id,
                    name = doc.getString("name") ?: "",
                    serviceType = doc.getString("serviceType") ?: "",
                    description = doc.getString("description") ?: "",
                    experience = doc.getString("experience") ?: "",
                    latitude = doc.getDouble("latitude") ?: 0.0,
                    longitude = doc.getDouble("longitude") ?: 0.0,
                    verificationDocUrl = doc.getString("verificationDocUrl") ?: "",
                    profileImageUrl = pImage ?: "",
                    profileImage = pImage,
                    rating = doc.getDouble("rating") ?: 0.0
                )
            }
            Result.success(providerList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================
    // 🔥 FETCH SINGLE PROVIDER DETAILS
    // =========================================================

    suspend fun getProviderDetails(userId: String): Result<Provider> {
        return try {
            val doc = db.collection("provider_requests").document(userId).get().await()
            if (!doc.exists()) {
                return Result.failure(Exception("Provider profile not found"))
            }

            var pImage = doc.getString("profileImageUrl")
            if (pImage.isNullOrBlank()) {
                pImage = doc.getString("profileImage")
            }
            if (pImage.isNullOrBlank()) {
                try {
                    val userDoc = db.collection("users").document(userId).get().await()
                    pImage = userDoc.getString("profileImageUrl")
                } catch (e: Exception) {
                    // ignore
                }
            }

            val provider = Provider(
                id = doc.getString("userId") ?: doc.id,
                name = doc.getString("name") ?: "",
                serviceType = doc.getString("serviceType") ?: "",
                description = doc.getString("description") ?: "",
                experience = doc.getString("experience") ?: "",
                latitude = doc.getDouble("latitude") ?: 0.0,
                longitude = doc.getDouble("longitude") ?: 0.0,
                verificationDocUrl = doc.getString("verificationDocUrl") ?: "",
                profileImageUrl = pImage ?: "",
                profileImage = pImage,
                rating = doc.getDouble("rating") ?: 0.0
            )
            Result.success(provider)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================
    // 🔥 UPDATE PROVIDER DETAILS
    // =========================================================

    suspend fun updateProviderDetails(userId: String, updates: Map<String, Any>): Result<String> {
        return try {
            db.collection("provider_requests").document(userId).update(updates).await()
            val updatedName = updates["name"] as? String
            if (!updatedName.isNullOrBlank()) {
                db.collection("users").document(userId).update("name", updatedName).await()
            }
            Result.success("Profile updated successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}