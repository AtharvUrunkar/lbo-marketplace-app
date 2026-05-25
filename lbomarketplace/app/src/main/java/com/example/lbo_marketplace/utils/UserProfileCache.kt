package com.example.lbo_marketplace.utils

import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe global memory cache for recently retrieved user profile images.
 * Helps minimize Firestore database reads and reduces network overhead during active sessions.
 */
object UserProfileCache {
    private val imageCache = ConcurrentHashMap<String, String>()

    fun getProfileImage(userId: String): String? {
        return imageCache[userId]
    }

    fun putProfileImage(userId: String, url: String) {
        if (url.isNotEmpty()) {
            imageCache[userId] = url
        }
    }
}
