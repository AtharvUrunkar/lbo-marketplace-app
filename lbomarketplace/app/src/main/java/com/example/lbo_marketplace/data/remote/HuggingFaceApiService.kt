package com.example.lbo_marketplace.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit interface for the Hugging Face Router API.
 * Uses the OpenAI-compatible /v1/chat/completions endpoint.
 * Model: meta-llama/Llama-3.2-1B-Instruct (confirmed working on free tier).
 */
interface HuggingFaceApiService {

    @POST("v1/chat/completions")
    suspend fun generateResponse(
        @Header("Authorization") authHeader: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}

// ==================== Request Models ====================

data class ChatCompletionRequest(
    val model: String = "meta-llama/Llama-3.2-1B-Instruct",
    val messages: List<ChatCompletionMessage>,

    @SerializedName("max_tokens")
    val maxTokens: Int = 512,

    val temperature: Float = 0.7f,

    val stream: Boolean = false
)

data class ChatCompletionMessage(
    val role: String,   // "system", "user", or "assistant"
    val content: String
)

// ==================== Response Models ====================

data class ChatCompletionResponse(
    val id: String?,
    val choices: List<ChatChoice>
)

data class ChatChoice(
    val index: Int,
    val message: ChatCompletionMessage,

    @SerializedName("finish_reason")
    val finishReason: String?
)
