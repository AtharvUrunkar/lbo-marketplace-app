package com.example.lbo_marketplace.data.model

data class Provider(

    val id: String = "",

    val name: String = "",

    val serviceType: String = "",

    // 🔥 NEW FIELDS
    val description: String = "",

    val experience: String = "",

    val latitude: Double = 0.0,

    val longitude: Double = 0.0,

    val profileImageUrl: String = "",

    val verificationDocUrl: String = "",
    val profileImage: String? = null, // 🔥 Added for profile images
    val rating: Double = 0.0 // 🔥 Added for rating
)