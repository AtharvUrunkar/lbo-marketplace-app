package com.example.lbo_marketplace.data.repository

import com.example.lbo_marketplace.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {

    // =====================================================
    // 🔥 FIRESTORE
    // =====================================================

    private val db =
        FirebaseFirestore.getInstance()

    // =====================================================
    // 🔥 CREATE USER PROFILE
    // =====================================================

    suspend fun createUserProfile(

        userProfile: UserProfile

    ): Result<String> {

        return try {

            db.collection("users")

                .document(userProfile.uid)

                .set(userProfile)

                .await()

            Result.success(
                "User profile created successfully"
            )

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    // =====================================================
    // 🔥 FETCH USER PROFILE
    // =====================================================

    suspend fun getUserProfile(

        userId: String

    ): Result<UserProfile> {

        return try {

            val document =

                db.collection("users")

                    .document(userId)

                    .get()

                    .await()

            if (!document.exists()) {

                return Result.failure(

                    Exception(
                        "User profile not found"
                    )
                )
            }

            val userProfile =

                UserProfile(

                    uid =
                        document.getString("uid")
                            ?: "",

                    name =
                        document.getString("name")
                            ?: "",

                    email =
                        document.getString("email")
                            ?: "",

                    phone =
                        document.getString("phone")
                            ?: "",

                    profileImageUrl =
                        document.getString(
                            "profileImageUrl"
                        ) ?: "",

                    role =
                        document.getString("role")
                            ?: "USER",

                    active =
                        document.getBoolean(
                            "active"
                        ) ?: true,

                    providerApproved =
                        document.getBoolean(
                            "providerApproved"
                        ) ?: false,

                    latitude =
                        document.getDouble(
                            "latitude"
                        ) ?: 0.0,

                    longitude =
                        document.getDouble(
                            "longitude"
                        ) ?: 0.0,

                    city =
                        document.getString(
                            "city"
                        ) ?: "",

                    area =
                        document.getString(
                            "area"
                        ) ?: "",

                    fullAddress =
                        document.getString(
                            "fullAddress"
                        ) ?: "",

                    locationEnabled =
                        document.getBoolean(
                            "locationEnabled"
                        ) ?: false,

                    preferredRadiusKm =
                        document.getLong(
                            "preferredRadiusKm"
                        )?.toInt() ?: 18,

                    createdAt =
                        document.getLong(
                            "createdAt"
                        ) ?: System.currentTimeMillis()
                )

            Result.success(
                userProfile
            )

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    // =====================================================
    // 🔥 UPDATE USER LOCATION
    // =====================================================

    suspend fun updateUserLocation(

        userId: String,

        latitude: Double,

        longitude: Double,

        city: String,

        area: String,

        fullAddress: String

    ): Result<String> {

        return try {

            val updates = mapOf(

                "latitude" to latitude,

                "longitude" to longitude,

                "city" to city,

                "area" to area,

                "fullAddress" to fullAddress,

                "locationEnabled" to true
            )

            db.collection("users")

                .document(userId)

                .update(updates)

                .await()

            Result.success(
                "Location updated successfully"
            )

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    // =====================================================
    // 🔥 UPDATE USER PROFILE
    // =====================================================

    suspend fun updateUserProfile(

        userId: String,

        updates: Map<String, Any>

    ): Result<String> {

        return try {

            db.collection("users")

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

    // =====================================================
    // 🔥 UPDATE PROFILE IMAGE
    // =====================================================

    suspend fun updateProfileImage(

        userId: String,

        imageUrl: String

    ): Result<String> {

        return try {

            db.collection("users")

                .document(userId)

                .update(
                    "profileImageUrl",
                    imageUrl
                )

                .await()

            Result.success(
                "Profile image updated"
            )

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    // =====================================================
    // 🔥 FETCH ALL USERS
    // =====================================================

    suspend fun getAllUsers():
            Result<List<UserProfile>> {

        return try {

            val snapshot =

                db.collection("users")

                    .get()

                    .await()

            val users =

                snapshot.documents.mapNotNull {

                    try {

                        UserProfile(

                            uid =
                                it.getString("uid")
                                    ?: "",

                            name =
                                it.getString("name")
                                    ?: "",

                            email =
                                it.getString("email")
                                    ?: "",

                            phone =
                                it.getString("phone")
                                    ?: "",

                            profileImageUrl =
                                it.getString(
                                    "profileImageUrl"
                                ) ?: "",

                            role =
                                it.getString("role")
                                    ?: "USER",

                            active =
                                it.getBoolean(
                                    "active"
                                ) ?: true,

                            providerApproved =
                                it.getBoolean(
                                    "providerApproved"
                                ) ?: false,

                            latitude =
                                it.getDouble(
                                    "latitude"
                                ) ?: 0.0,

                            longitude =
                                it.getDouble(
                                    "longitude"
                                ) ?: 0.0,

                            city =
                                it.getString(
                                    "city"
                                ) ?: "",

                            area =
                                it.getString(
                                    "area"
                                ) ?: "",

                            fullAddress =
                                it.getString(
                                    "fullAddress"
                                ) ?: "",

                            locationEnabled =
                                it.getBoolean(
                                    "locationEnabled"
                                ) ?: false,

                            preferredRadiusKm =
                                it.getLong(
                                    "preferredRadiusKm"
                                )?.toInt() ?: 18,

                            createdAt =
                                it.getLong(
                                    "createdAt"
                                ) ?: System.currentTimeMillis()
                        )

                    } catch (e: Exception) {

                        null
                    }
                }

            Result.success(
                users
            )

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    // =====================================================
    // 🔥 DELETE USER
    // =====================================================

    suspend fun deleteUser(

        userId: String

    ): Result<String> {

        return try {

            db.collection("users")

                .document(userId)

                .delete()

                .await()

            Result.success(
                "User deleted successfully"
            )

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}