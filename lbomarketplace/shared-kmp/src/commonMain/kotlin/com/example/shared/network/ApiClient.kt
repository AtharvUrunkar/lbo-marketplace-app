package com.example.shared.network

import io.ktor.client.call.body
import io.ktor.client.request.get

class ApiClient {

    private val client = httpClient()

    suspend fun testCall(): String {
        return client
            .get("https://httpbin.org/get")
            .body()
    }
}