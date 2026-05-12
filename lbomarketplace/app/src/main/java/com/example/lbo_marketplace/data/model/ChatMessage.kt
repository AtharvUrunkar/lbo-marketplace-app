package com.example.lbo_marketplace.data.model

import java.util.UUID

/**
 * Represents a single chat message in the AI chatbot conversation.
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
