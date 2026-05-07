package com.example.lbo_marketplace.data.firestore

import com.example.lbo_marketplace.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun createUserProfile(userProfile: UserProfile) {
        usersCollection
            .document(userProfile.uid)
            .set(userProfile)
            .await()
    }

    suspend fun getUserProfile(uid: String): UserProfile? {
        val snapshot = usersCollection.document(uid).get().await()
        return snapshot.toObject(UserProfile::class.java)
    }
}
