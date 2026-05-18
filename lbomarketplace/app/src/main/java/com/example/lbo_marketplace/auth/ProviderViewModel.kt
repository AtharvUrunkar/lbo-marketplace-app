package com.example.lbo_marketplace.auth

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lbo_marketplace.data.model.Provider
import com.example.lbo_marketplace.data.repository.CloudinaryRepository
import com.example.lbo_marketplace.data.repository.ProviderRepository
import kotlinx.coroutines.launch

class ProviderViewModel : ViewModel() {

    // =========================================================
    // 🔥 REPOSITORIES
    // =========================================================

    private val repo =
        ProviderRepository()

    private val cloudinaryRepo =
        CloudinaryRepository()

    // =========================================================
    // 🔥 APPLY STATE
    // =========================================================

    var applyState by mutableStateOf("")
        private set

    // =========================================================
    // 🔥 PROVIDERS LIST
    // =========================================================

    var providers by mutableStateOf<List<Provider>>(
        emptyList()
    )
        private set

    // 🔥 LOADING STATE FOR SKELETONS
    var isLoading by mutableStateOf(false)
        private set

    // =========================================================
    // 🔥 FETCH PROVIDERS
    // =========================================================

    fun fetchProviders() {

        viewModelScope.launch {

            val result =
                repo.getApprovedProviders()

            providers =
                result.getOrElse {
                    emptyList()
                }
        }
    }

    // =========================================================
    // 🔥 FILE SIZE CHECK
    // =========================================================

    private fun getFileSizeInMB(
        context: Context,
        uri: Uri
    ): Double {

        val cursor =
            context.contentResolver
                .openFileDescriptor(
                    uri,
                    "r"
                )

        val size =
            cursor?.statSize ?: 0L

        cursor?.close()

        return size.toDouble() /
                (1024 * 1024)
    }

    // =========================================================
    // 🔥 APPLY PROVIDER
    // =========================================================

    fun applyWithDetails(
        context: Context,
        userId: String,
        email: String,
        name: String,
        serviceType: String,
        description: String,
        experience: String,
        latitude: Double,
        longitude: Double,
        verificationDocUri: Uri
    ) {

        viewModelScope.launch {

            try {

                // =============================================
                // 🔥 DOCUMENT SIZE CHECK
                // =============================================

                val documentSize =
                    getFileSizeInMB(
                        context,
                        verificationDocUri
                    )

                // 🔥 4MB LIMIT
                if (documentSize > 4) {

                    applyState =
                        "Document exceeds 4 MB limit"

                    return@launch
                }

                // =============================================
                // 🔥 DOCUMENT UPLOAD
                // =============================================

                applyState =
                    "Uploading verification document..."

                val documentFile =
                    cloudinaryRepo.uriToFile(
                        context,
                        verificationDocUri
                    )

                val documentResult =
                    cloudinaryRepo.uploadFile(
                        documentFile
                    )

                if (documentResult.isFailure) {

                    applyState =
                        documentResult
                            .exceptionOrNull()
                            ?.message
                            ?: "Document upload failed"

                    return@launch
                }

                val documentUrl =
                    documentResult.getOrNull()
                        ?: ""

                // =============================================
                // 🔥 SAVE TO FIRESTORE
                // =============================================

                applyState =
                    "Submitting application..."

                val result =
                    repo.applyForProviderWithDetails(
                        userId = userId,
                        email = email,
                        name = name,
                        serviceType = serviceType,
                        description = description,
                        experience = experience,
                        latitude = latitude,
                        longitude = longitude,
                        verificationDocUrl = documentUrl
                    )

                applyState =
                    result.fold(

                        onSuccess = {

                            "Application submitted successfully"
                        },

                        onFailure = {

                            it.message ?: "Failed"
                        }
                    )

            } catch (e: Exception) {

                applyState =
                    e.message
                        ?: "Something went wrong"
            }
        }
    }
}