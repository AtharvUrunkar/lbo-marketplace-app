package com.example.lbo_marketplace.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // REGISTER
    suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<Pair<String, String>> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("UID null"))

            val userMap = hashMapOf(
                "uid" to uid,
                "name" to name,
                "email" to email,
                "role" to "USER",
                "active" to true
            )

            db.collection("users").document(uid).set(userMap).await()

            Result.success(uid to "USER")

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // LOGIN
    suspend fun login(email: String, password: String): Result<Pair<String, String>> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("UID null"))

            val snapshot = db.collection("users").document(uid).get().await()

            if (!snapshot.exists()) {
                return Result.failure(Exception("User data not found"))
            }

            val role = snapshot.getString("role")
                ?: return Result.failure(Exception("Role missing"))

                        Result.success(uid to role)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // SESSION CHECK
    suspend fun checkSession(): Result<Pair<String, String>> {
        val user = auth.currentUser ?: return Result.failure(Exception("Not logged in"))

        return try {
            val snapshot = db.collection("users").document(user.uid).get().await()

            if (!snapshot.exists()) {
                return Result.failure(Exception("User data not found"))
            }

            val role = snapshot.getString("role")
                ?: return Result.failure(Exception("Role missing"))

            Result.success(user.uid to role)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // LOGOUT
    fun logout() {
        auth.signOut()
    }
}