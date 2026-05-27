package com.example.lbo_marketplace.data.repository

import com.example.lbo_marketplace.BuildConfig
import com.example.lbo_marketplace.data.remote.ChatCompletionMessage
import com.example.lbo_marketplace.data.remote.ChatCompletionRequest
import com.example.lbo_marketplace.data.remote.RetrofitClient

/**
 * Repository that handles AI chat interactions via the Hugging Face Inference Providers API.
 * Uses the OpenAI-compatible /v1/chat/completions format with Qwen2.5-1.5B-Instruct.
 */
class ChatRepository {

    private val apiService = RetrofitClient.huggingFaceApi
    private val apiKey = BuildConfig.HF_API_KEY

    companion object {
        private const val SYSTEM_PROMPT =
            "You are an assistant inside a service marketplace app called LBO. " +
            "Help users with bookings, providers, services, account issues, and general questions. " +
            "Answer clearly and professionally."
    }

    /**
     * Sends a user message to the Hugging Face API and returns the AI response.
     *
     * @param userMessage The user's input text.
     * @param conversationHistory Previous messages for context (optional).
     * @return Result containing the AI response string or an error.
     */
    suspend fun getAiResponse(
        userMessage: String,
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): Result<String> {
        return try {
            // Build messages in OpenAI chat format
            val messages = buildChatMessages(userMessage, conversationHistory)

            val request = ChatCompletionRequest(messages = messages)
            val authHeader = "Bearer $apiKey"

            val response = apiService.generateResponse(authHeader, request)

            if (response.choices.isNotEmpty()) {
                val content = response.choices[0].message.content.trim()
                if (content.isBlank()) {
                    Result.failure(Exception("Received an empty response. Please try again."))
                } else {
                    Result.success(content)
                }
            } else {
                Result.failure(Exception("No response from AI. Please try again."))
            }
        } catch (e: retrofit2.HttpException) {
            val errorBody = try {
                e.response()?.errorBody()?.string() ?: ""
            } catch (_: Exception) { "" }

            when (e.code()) {
                401 -> Result.failure(Exception("Invalid API key. Please check your Hugging Face token."))
                403 -> Result.failure(Exception("Access denied. Please verify your API permissions."))
                422 -> Result.failure(Exception("Model is not available. Please try again later."))
                429 -> Result.failure(Exception("Rate limit exceeded. Please wait a moment and try again."))
                503 -> Result.failure(Exception("Model is loading. Please try again in a few seconds."))
                else -> Result.failure(Exception("Server error (${e.code()}): $errorBody"))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("Request timed out. Please check your connection and try again."))
        } catch (e: java.io.IOException) {
            Result.failure(Exception("Network error. Please check your internet connection."))
        } catch (e: Exception) {
            Result.failure(Exception("Something went wrong: ${e.localizedMessage}"))
        }
    }

    /**
     * Builds the messages list in OpenAI chat completion format.
     * Includes system prompt, conversation history, and the current user message.
     */
    private fun buildChatMessages(
        userMessage: String,
        conversationHistory: List<Pair<String, String>>
    ): List<ChatCompletionMessage> {
        val messages = mutableListOf<ChatCompletionMessage>()

        // System prompt
        messages.add(
            ChatCompletionMessage(role = "system", content = SYSTEM_PROMPT)
        )

        // Previous conversation turns
        for ((userMsg, aiMsg) in conversationHistory) {
            messages.add(ChatCompletionMessage(role = "user", content = userMsg))
            messages.add(ChatCompletionMessage(role = "assistant", content = aiMsg))
        }

        // Current user message
        messages.add(ChatCompletionMessage(role = "user", content = userMessage))

        return messages
    }
}
