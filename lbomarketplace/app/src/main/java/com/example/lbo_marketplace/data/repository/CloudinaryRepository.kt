package com.example.lbo_marketplace.data.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File

class CloudinaryRepository {

    // =========================================================
    // 🔥 HTTP CLIENT
    // =========================================================

    private val client = OkHttpClient()

    // =========================================================
    // 🔥 CLOUDINARY CONFIG
    // =========================================================

    private val cloudName =
        "dx756qj7x"

    private val uploadPreset =
        "lbo_unsigned_upload"

    // =========================================================
    // 🔥 UPLOAD FILE
    // =========================================================

    suspend fun uploadFile(
        file: File
    ): Result<String> {

        return withContext(Dispatchers.IO) {

            try {

                // =============================================
                // 🔥 DETECT RESOURCE TYPE
                // =============================================

                val resourceType =
                    if (
                        file.extension.equals(
                            "pdf",
                            true
                        )
                    ) {
                        "raw"
                    } else {
                        "image"
                    }

                // =============================================
                // 🔥 CLOUDINARY URL
                // =============================================

                val url =
                    "https://api.cloudinary.com/v1_1/$cloudName/$resourceType/upload"

                println(
                    "UPLOAD URL: $url"
                )

                println(
                    "FILE EXTENSION: ${file.extension}"
                )

                // =============================================
                // 🔥 MIME TYPE
                // =============================================

                val mediaType =
                    when {

                        file.extension.equals(
                            "pdf",
                            true
                        ) -> "application/pdf"

                        file.extension.equals(
                            "jpg",
                            true
                        ) ||
                                file.extension.equals(
                                    "jpeg",
                                    true
                                ) -> "image/jpeg"

                        file.extension.equals(
                            "png",
                            true
                        ) -> "image/png"

                        else -> "*/*"
                    }

                // =============================================
                // 🔥 MULTIPART BODY
                // =============================================

                val requestBody =
                    MultipartBody.Builder()
                        .setType(MultipartBody.FORM)

                        .addFormDataPart(
                            "file",
                            file.name,

                            file.asRequestBody(
                                mediaType.toMediaTypeOrNull()
                            )
                        )

                        .addFormDataPart(
                            "upload_preset",
                            uploadPreset
                        )

                        .build()

                // =============================================
                // 🔥 REQUEST
                // =============================================

                val request =
                    Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build()

                // =============================================
                // 🔥 EXECUTE REQUEST
                // =============================================

                val response =
                    client.newCall(request).execute()

                val responseData =
                    response.body?.string()

                response.close()

                // =============================================
                // 🔥 EMPTY RESPONSE
                // =============================================

                if (responseData == null) {

                    return@withContext Result.failure(
                        Exception(
                            "Empty response from Cloudinary"
                        )
                    )
                }

// =============================================
// 🔥 PARSE JSON
// =============================================

                val json = JSONObject(responseData)

                if (!json.has("secure_url")) {
                    println("CLOUDINARY ERROR: $json")
                    return@withContext Result.failure(
                        Exception(json.toString())
                    )
                }

// ✅ Use secure_url EXACTLY as returned — no modifications
                val uploadedUrl = json.getString("secure_url")

                println("UPLOAD SUCCESS: $uploadedUrl")

                Result.success(uploadedUrl)

            } catch (e: Exception) {

                e.printStackTrace()

                Result.failure(e)
            }
        }
    }

    // =========================================================
    // 🔥 URI → FILE
    // =========================================================

    fun uriToFile(
        context: Context,
        uri: Uri
    ): File {

        // =============================================
        // 🔥 GET ORIGINAL FILE NAME
        // =============================================

        var fileName = "temp_file"

        val cursor =
            context.contentResolver.query(
                uri,
                null,
                null,
                null,
                null
            )

        cursor?.use {

            val nameIndex =
                it.getColumnIndex(
                    android.provider.OpenableColumns.DISPLAY_NAME
                )

            if (
                it.moveToFirst() &&
                nameIndex != -1
            ) {

                fileName =
                    it.getString(nameIndex)
            }
        }

        println(
            "ORIGINAL FILE NAME: $fileName"
        )

        // =============================================
        // 🔥 GET EXTENSION
        // =============================================

        val extension =
            fileName.substringAfterLast(
                ".",
                "tmp"
            )

        println(
            "DETECTED EXTENSION: $extension"
        )

        // =============================================
        // 🔥 CREATE TEMP FILE
        // =============================================

        val tempFile =
            File.createTempFile(
                "upload_",
                ".$extension",
                context.cacheDir
            )

        // =============================================
        // 🔥 COPY CONTENT
        // =============================================

        context.contentResolver
            .openInputStream(uri)
            ?.use { inputStream ->

                tempFile.outputStream()
                    .use { outputStream ->

                        inputStream.copyTo(
                            outputStream
                        )
                    }
            }

        println(
            "FINAL FILE EXTENSION: ${tempFile.extension}"
        )

        return tempFile
    }
}