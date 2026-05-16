package com.example.lbo_marketplace.data.model

data class Provider(
    val id: String = "",
    val name: String = "",
    val serviceType: String = "",
    val status: String = "",
    val profileImage: String? = null // 🔥 Added for profile images
)