package com.example.lbo_marketplace.repository

// Import Android logging utility
import android.util.Log
// Import BuildConfig to securely retrieve our API credentials
import com.example.lbo_marketplace.BuildConfig
// Import extension function to parse string to media type
import okhttp3.MediaType.Companion.toMediaTypeOrNull
// Import HTTP client library
import okhttp3.OkHttpClient
// Import HTTP Request builder
import okhttp3.Request
// Import extension function to convert string to RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
// Import JSON Array class to represent list of target players
import org.json.JSONArray
// Import JSON Object class to build our payload body
import org.json.JSONObject

// Repository responsible for handling OneSignal notification network calls
class NotificationRepository {

    // Instantiate a reusable OkHttpClient instance for performing HTTP calls
    private val client = OkHttpClient()

    // Retrieve the OneSignal App ID securely from the generated BuildConfig
    private val appId = BuildConfig.ONESIGNAL_APP_ID

    // Retrieve the OneSignal REST API Key securely from the generated BuildConfig
    private val restApiKey = BuildConfig.ONESIGNAL_REST_API_KEY

    // Function to trigger a push notification to a specific recipient (identified by playerId)
    fun sendNotification(
        // The target device registration identifier (Player ID)
        playerId: String,
        // The title of the push notification
        title: String,
        // The message body of the push notification
        message: String
    ) {
        try {
            // Build the JSON payload representing the OneSignal notification parameters
            val jsonBody = JSONObject().apply {
                // Attach the OneSignal App ID to authenticate the application scope
                put("app_id", appId)

                // Attach the target device player ID(s) in a JSON list format
                put(
                    "include_player_ids",
                    JSONArray().put(playerId)
                )

                // Define notification title under the English locale key ("en")
                put(
                    "headings",
                    JSONObject().put("en", title)
                )

                // Define notification message body under the English locale key ("en")
                put(
                    "contents",
                    JSONObject().put("en", message)
                )
            }

            // Convert the JSON payload object to a request body formatted as UTF-8 JSON
            val body = jsonBody.toString().toRequestBody(
                "application/json; charset=utf-8".toMediaTypeOrNull()
            )

            // Construct the HTTP POST request to OneSignal's REST API endpoint
            val request = Request.Builder()
                // Set target API endpoint URL
                .url("https://onesignal.com/api/v1/notifications")
                // Authorize using the REST API key (Key is the modern standard)
                .addHeader("Authorization", "Key $restApiKey")
                // Explicitly declare JSON payload media type in header
                .addHeader("Content-Type", "application/json")
                // Define request method as POST with the JSON body
                .post(body)
                // Build the final request object
                .build()

            // Spawn a new background thread to execute the synchronous network call safely
            Thread {
                try {
                    // Execute the network request and retrieve the response
                    val response = client.newCall(request).execute()

                    // Log response payload/status for debugging and audit purposes
                    Log.d(
                        "OneSignal",
                        "Notification Sent: ${response.body?.string()}"
                    )
                } catch (e: Exception) {
                    // Log execution failure if the network request fails
                    Log.e(
                        "OneSignal",
                        "Send Failed",
                        e
                    )
                }
            }.start() // Start execution of the spawned background thread

        } catch (e: Exception) {
            // Catch and log any JSON building exception defensively
            Log.e(
                "OneSignal",
                "JSON Error",
                e
            )
        }
    }

    // Function to trigger a push notification to all subscribed users
    fun sendNotificationToAll(
        // The title of the push notification
        title: String,
        // The message body of the push notification
        message: String
    ) {
        try {
            // Build the JSON payload representing the OneSignal notification parameters
            val jsonBody = JSONObject().apply {
                // Attach the OneSignal App ID to authenticate the application scope
                put("app_id", appId)

                // Attach the "Subscribed Users" segment to target all subscribed devices
                put(
                    "included_segments",
                    JSONArray().put("Subscribed Users")
                )

                // Define notification title under the English locale key ("en")
                put(
                    "headings",
                    JSONObject().put("en", title)
                )

                // Define notification message body under the English locale key ("en")
                put(
                    "contents",
                    JSONObject().put("en", message)
                )
            }

            // Convert the JSON payload object to a request body formatted as UTF-8 JSON
            val body = jsonBody.toString().toRequestBody(
                "application/json; charset=utf-8".toMediaTypeOrNull()
            )

            // Construct the HTTP POST request to OneSignal's REST API endpoint
            val request = Request.Builder()
                // Set target API endpoint URL
                .url("https://onesignal.com/api/v1/notifications")
                // Authorize using Key scheme in headers (modern standard)
                .addHeader("Authorization", "Key $restApiKey")
                // Explicitly declare JSON payload media type in header
                .addHeader("Content-Type", "application/json")
                // Define request method as POST with the JSON body
                .post(body)
                // Build the final request object
                .build()

            // Spawn a new background thread to execute the synchronous network call safely
            Thread {
                try {
                    // Execute the network request and retrieve the response
                    val response = client.newCall(request).execute()

                    // Log response payload/status for debugging and audit purposes
                    Log.d(
                        "OneSignal",
                        "Notification Sent to All: ${response.body?.string()}"
                    )
                } catch (e: Exception) {
                    // Log execution failure if the network request fails
                    Log.e(
                        "OneSignal",
                        "Send to All Failed",
                        e
                    )
                }
            }.start()

        } catch (e: Exception) {
            // Catch and log any JSON building exception defensively
            Log.e(
                "OneSignal",
                "JSON Error in sendToAll",
                e
            )
        }
    }
}