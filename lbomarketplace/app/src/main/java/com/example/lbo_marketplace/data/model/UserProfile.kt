package com.example.lbo_marketplace.data.model

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "USER",
    val active: Boolean = true
)