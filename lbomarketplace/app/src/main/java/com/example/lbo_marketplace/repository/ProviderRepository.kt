package com.example.lbo_marketplace.data.repository

import com.example.lbo_marketplace.data.model.Provider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProviderRepository {

    private val db =
        FirebaseFirestore.getInstance()

    // =====================================================
    // 🔥 APPLY PROVIDER
    // =====================================================

    suspend fun applyForProviderWithDetails(

        userId: String,

        email: String,

        name: String,

        serviceType: String,

        description: String,

        experience: String,

        latitude: Double,

        longitude: Double,

        city: String,

        area: String,

        fullAddress: String,

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

                "city" to city,

                "area" to area,

                "fullAddress" to fullAddress,

                "verificationDocUrl" to verificationDocUrl,

                "status" to "PENDING",

                "createdAt" to
                        System.currentTimeMillis(),

                "rating" to 0.0
            )

            db.collection(
                "provider_requests"
            )
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

    // =====================================================
    // 🔥 FETCH APPROVED PROVIDERS
    // =====================================================

    suspend fun getApprovedProviders():
            Result<List<Provider>> {

        return try {

            val snapshot =

                db.collection(
                    "provider_requests"
                )
                    .whereEqualTo(
                        "status",
                        "APPROVED"
                    )
                    .get()
                    .await()

            val providers =

                snapshot.documents.mapNotNull { doc ->

                    var pImage =
                        doc.getString(
                            "profileImageUrl"
                        )

                    if (pImage.isNullOrBlank()) {

                        pImage =
                            doc.getString(
                                "profileImage"
                            )
                    }

                    if (pImage.isNullOrBlank()) {

                        try {

                            val userDoc =

                                db.collection("users")
                                    .document(doc.id)
                                    .get()
                                    .await()

                            pImage =
                                userDoc.getString(
                                    "profileImageUrl"
                                )

                        } catch (e: Exception) {

                            // ignore
                        }
                    }

                    Provider(

                        id =
                            doc.getString("userId")
                                ?: doc.id,

                        name =
                            doc.getString("name")
                                ?: "",

                        serviceType =
                            doc.getString(
                                "serviceType"
                            ) ?: "",

                        description =
                            doc.getString(
                                "description"
                            ) ?: "",

                        experience =
                            doc.getString(
                                "experience"
                            ) ?: "",

                        latitude =
                            doc.getDouble(
                                "latitude"
                            ) ?: 0.0,

                        longitude =
                            doc.getDouble(
                                "longitude"
                            ) ?: 0.0,

                        city =
                            doc.getString(
                                "city"
                            ) ?: "",

                        area =
                            doc.getString(
                                "area"
                            ) ?: "",

                        fullAddress =
                            doc.getString(
                                "fullAddress"
                            ) ?: "",

                        verificationDocUrl =
                            doc.getString(
                                "verificationDocUrl"
                            ) ?: "",

                        profileImageUrl =
                            pImage ?: "",

                        profileImage =
                            pImage,

                        rating =
                            doc.getDouble(
                                "rating"
                            ) ?: 0.0
                    )
                }

            Result.success(
                providers
            )

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    // =====================================================
    // 🔥 FETCH SINGLE PROVIDER
    // =====================================================

    suspend fun getProviderDetails(
        userId: String
    ): Result<Provider> {

        return try {

            val doc =

                db.collection(
                    "provider_requests"
                )
                    .document(userId)
                    .get()
                    .await()

            if (!doc.exists()) {

                return Result.failure(

                    Exception(
                        "Provider profile not found"
                    )
                )
            }

            var pImage =
                doc.getString(
                    "profileImageUrl"
                )

            if (pImage.isNullOrBlank()) {

                pImage =
                    doc.getString(
                        "profileImage"
                    )
            }

            if (pImage.isNullOrBlank()) {

                try {

                    val userDoc =

                        db.collection("users")
                            .document(userId)
                            .get()
                            .await()

                    pImage =
                        userDoc.getString(
                            "profileImageUrl"
                        )

                } catch (e: Exception) {

                    // ignore
                }
            }

            val provider = Provider(

                id =
                    doc.getString("userId")
                        ?: doc.id,

                name =
                    doc.getString("name")
                        ?: "",

                serviceType =
                    doc.getString(
                        "serviceType"
                    ) ?: "",

                description =
                    doc.getString(
                        "description"
                    ) ?: "",

                experience =
                    doc.getString(
                        "experience"
                    ) ?: "",

                latitude =
                    doc.getDouble(
                        "latitude"
                    ) ?: 0.0,

                longitude =
                    doc.getDouble(
                        "longitude"
                    ) ?: 0.0,

                city =
                    doc.getString(
                        "city"
                    ) ?: "",

                area =
                    doc.getString(
                        "area"
                    ) ?: "",

                fullAddress =
                    doc.getString(
                        "fullAddress"
                    ) ?: "",

                verificationDocUrl =
                    doc.getString(
                        "verificationDocUrl"
                    ) ?: "",

                profileImageUrl =
                    pImage ?: "",

                profileImage =
                    pImage,

                rating =
                    doc.getDouble(
                        "rating"
                    ) ?: 0.0
            )

            Result.success(
                provider
            )

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    // =====================================================
    // 🔥 UPDATE PROVIDER
    // =====================================================

    suspend fun updateProviderDetails(

        userId: String,

        updates: Map<String, Any>

    ): Result<String> {

        return try {

            db.collection(
                "provider_requests"
            )
                .document(userId)
                .update(updates)
                .await()

            Result.success(
                "Profile updated successfully"
            )

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}