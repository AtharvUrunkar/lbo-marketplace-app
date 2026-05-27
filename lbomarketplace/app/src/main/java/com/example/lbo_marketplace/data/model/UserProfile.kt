package com.example.lbo_marketplace.data.model

data class UserProfile(

    // =====================================================
    // 🔥 BASIC INFO
    // =====================================================

    val uid: String = "",

    val name: String = "",

    val email: String = "",

    // =====================================================
    // 🔥 PHONE
    // =====================================================

    val phone: String = "",

    // =====================================================
    // 🔥 PROFILE IMAGE
    // =====================================================

    val profileImageUrl: String = "",

    // =====================================================
    // 🔥 ROLE
    // =====================================================

    val role: String = "USER",

    // =====================================================
    // 🔥 ACCOUNT STATUS
    // =====================================================

    val active: Boolean = true,

    // =====================================================
    // 🔥 PROVIDER STATUS
    // =====================================================

    val providerApproved: Boolean = false,

    // =====================================================
    // 🔥 GPS LOCATION
    // =====================================================

    val latitude: Double = 0.0,

    val longitude: Double = 0.0,

    // =====================================================
    // 🔥 GEO LOCATION
    // =====================================================

    val city: String = "",

    val area: String = "",

    val fullAddress: String = "",

    // =====================================================
    // 🔥 OPTIONAL LOCATION SETTINGS
    // =====================================================

    // User enabled nearby provider discovery
    val locationEnabled: Boolean = false,

    // Maximum provider search radius
    val preferredRadiusKm: Int = 18,

    // =====================================================
    // 🔥 TIMESTAMP
    // =====================================================

    val createdAt: Long =
        System.currentTimeMillis()
)