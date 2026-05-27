package com.example.lbo_marketplace.ui.screens.user.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lbo_marketplace.data.model.ChatMessage
import com.example.lbo_marketplace.data.repository.ChatRepository
import kotlinx.coroutines.launch

/**
 * ViewModel managing the AI chatbot state.
 * Handles message sending, loading states, error handling, and conversation history.
 */
class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()

    // Observable state
    var messages by mutableStateOf<List<ChatMessage>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Conversation history for multi-turn context
    private val conversationHistory = mutableListOf<Pair<String, String>>()

    init {
        // Add welcome message on init
        addWelcomeMessage()
    }

    private fun addWelcomeMessage() {
        val welcomeMsg = ChatMessage(
            content = "👋 Hi! I'm your LBO Assistant.\n\nI can help you with:\n• Finding & booking services\n• Provider questions\n• Account support\n• General queries\n\nHow can I help you today?",
            isUser = false
        )
        messages = listOf(welcomeMsg)
    }

    /**
     * Sends a user message and fetches the AI response.
     */
    fun sendMessage(text: String) {
        if (text.isBlank() || isLoading) return

        // Clear any previous error
        errorMessage = null

        // Add user message
        val userMessage = ChatMessage(
            content = text.trim(),
            isUser = true
        )
        messages = messages + userMessage

        // Show loading state
        isLoading = true

        viewModelScope.launch {
            val result = repository.getAiResponse(
                userMessage = text.trim(),
                conversationHistory = conversationHistory.toList()
            )

            result.fold(
                onSuccess = { response ->
                    // Add AI response
                    val aiMessage = ChatMessage(
                        content = response,
                        isUser = false
                    )
                    messages = messages + aiMessage

                    // Update conversation history for context
                    conversationHistory.add(Pair(text.trim(), response))

                    // Keep only last 5 turns to avoid prompt length issues
                    if (conversationHistory.size > 5) {
                        conversationHistory.removeAt(0)
                    }
                },
                onFailure = { error ->
                    errorMessage = error.message

                    // Add error message as AI response
                    val errorMsg = ChatMessage(
                        content = "⚠️ ${error.message}",
                        isUser = false
                    )
                    messages = messages + errorMsg
                }
            )

            isLoading = false
        }
    }

    /**
     * Clears the chat and resets conversation history.
     */
    fun clearChat() {
        messages = emptyList()
        conversationHistory.clear()
        errorMessage = null
        addWelcomeMessage()
    }
}
