package com.example.lbo_marketplace.repository

import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class NotificationRepository {

    private val client = OkHttpClient()

    // 🔥 YOUR ONESIGNAL APP ID

    private val appId =
        "ea563567-0403-4d31-af1a-235130fbbd6b"

    // 🔥 REST API KEY

    private val restApiKey =
        "os_v2_app_5jldkzyeangtdly2enitb655no3iit4a42zu5ge3pilwovaicafj57ww7mgcu6a3zvyo2odguhtiu6hvpynx76jiamz3fuofjyrwvai"

    // =====================================================
    // SEND NOTIFICATION
    // =====================================================

    fun sendNotification(

        playerId: String,

        title: String,

        message: String
    ) {

        try {

            val jsonBody = JSONObject().apply {

                put("app_id", appId)

                put(

                    "include_player_ids",

                    JSONArray().put(playerId)
                )

                put(

                    "headings",

                    JSONObject().put(
                        "en",
                        title
                    )
                )

                put(

                    "contents",

                    JSONObject().put(
                        "en",
                        message
                    )
                )
            }

            val body =

                jsonBody.toString()

                    .toRequestBody(

                        "application/json; charset=utf-8"

                            .toMediaTypeOrNull()
                    )

            val request = Request.Builder()

                .url(
                    "https://onesignal.com/api/v1/notifications"
                )

                .addHeader(
                    "Authorization",
                    "Basic $restApiKey"
                )

                .addHeader(
                    "Content-Type",
                    "application/json"
                )

                .post(body)

                .build()

            Thread {

                try {

                    val response =
                        client.newCall(request)
                            .execute()

                    Log.d(

                        "OneSignal",

                        "Notification Sent: ${response.body?.string()}"
                    )

                } catch (e: Exception) {

                    Log.e(

                        "OneSignal",

                        "Send Failed",

                        e
                    )
                }

            }.start()

        } catch (e: Exception) {

            Log.e(

                "OneSignal",

                "JSON Error",

                e
            )
        }
    }
}