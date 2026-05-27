package com.example.lbo_marketplace.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    // =====================================================
    // 🔥 AUTH MANAGER
    // =====================================================

    private val authManager =
        FirebaseAuthManager()

    // 🔥 PROVIDER SWITCH: Allows registered service providers to enter customer mode to browse and book other providers.
    val isProviderInCustomerMode = mutableStateOf(false)

    // =====================================================
    // 🔥 FIRESTORE
    // =====================================================

    private val db =
        FirebaseFirestore.getInstance()

    // =====================================================
    // 🔥 AUTH STATE
    // =====================================================

    private val _authState =

        mutableStateOf<AuthState>(
            AuthState.Idle
        )

    val authState:
            State<AuthState> = _authState

    // =====================================================
    // 🔥 PROVIDER LISTENER
    // =====================================================

    private var providerListener:
            ListenerRegistration? = null

    /**
     * Handles User Registration.
     * 
     * LOGIC:
     * 1. Sets state to [AuthState.Loading] to show the GIF.
     * 2. Calls [FirebaseAuthManager.register].
     * 3. On success, starts a Firestore listener and updates state to Authenticated.
     * 4. On failure, updates state to Error.
     * 
     * REFERENCED IN: [AuthSessionTestScreen] (Register Button)
     */
    fun register(
        name: String, 
        email: String, 
        password: String,
        phoneNumber: String = "",
        pincode: String = "",
        latitude: Double = 0.0,
        longitude: Double = 0.0
    ) {
        viewModelScope.launch {

            _authState.value =
                AuthState.Loading

            val result =

                authManager.register(

                    name,

                    email,

                    password, phoneNumber, pincode, latitude, longitude
                )

            _authState.value =

                result.fold(

                    onSuccess = {

                        startProviderListener(
                            it.first
                        )

                        AuthState.Authenticated(

                            it.first,

                            it.second
                        )
                    },

                    onFailure = {

                        AuthState.Error(

                            it.message
                                ?: "Error"
                        )
                    }
                )
        }
    }

    // =====================================================
    // 🔥 LOGIN
    // =====================================================

    fun login(

        email: String,

        password: String
    ) {

        viewModelScope.launch {

            _authState.value =
                AuthState.Loading

            val result =

                authManager.login(
                    email,
                    password
                )

            _authState.value =

                result.fold(

                    onSuccess = {

                        startProviderListener(
                            it.first
                        )

                        AuthState.Authenticated(

                            it.first,

                            it.second
                        )
                    },

                    onFailure = {

                        AuthState.Error(

                            it.message
                                ?: "Error"
                        )
                    }
                )
        }
    }

    // =====================================================
    // 🔥 CHECK SESSION
    // =====================================================

    fun checkSession() {

        viewModelScope.launch {

            _authState.value =
                AuthState.Loading

            val result =
                authManager.checkSession()

            _authState.value =

                result.fold(

                    onSuccess = {

                        startProviderListener(
                            it.first
                        )

                        AuthState.Authenticated(

                            it.first,

                            it.second
                        )
                    },

                    onFailure = {

                        AuthState.Unauthenticated
                    }
                )
        }
    }

    // =====================================================
    // 🔥 PROVIDER LISTENER
    // =====================================================

    private fun startProviderListener(
        userId: String
    ) {

        providerListener?.remove()

        providerListener =

            db.collection(
                "provider_requests"
            )

                .whereEqualTo(
                    "userId",
                    userId
                )

                .addSnapshotListener {

                        snapshot,
                        _ ->

                    if (
                        snapshot != null &&
                        !snapshot.isEmpty
                    ) {

                        val status =

                            snapshot.documents[0]
                                .getString(
                                    "status"
                                )

                        when (status) {

                            "PENDING" -> {

                                _authState.value =
                                    AuthState.ProviderPending
                            }

                            "APPROVED" -> {

                                _authState.value =

                                    AuthState.Authenticated(

                                        userId,

                                        "SERVICE_PROVIDER"
                                    )
                            }

                            "REJECTED" -> {

                                _authState.value =

                                    AuthState.Authenticated(

                                        userId,

                                        "USER"
                                    )
                            }
                        }
                    }
                }
    }

    // =====================================================
    // 🔥 UPDATE USER LOCATION
    // =====================================================

    fun updateUserLocation(

        userId: String,

        latitude: Double,

        longitude: Double,

        city: String,

        area: String,

        fullAddress: String
    ) {

        viewModelScope.launch {

            try {

                val updates = mapOf(

                    "latitude" to latitude,

                    "longitude" to longitude,

                    "city" to city,

                    "area" to area,

                    "fullAddress" to fullAddress,

                    "locationEnabled" to true
                )

                db.collection("users")

                    .document(userId)

                    .update(updates)

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }
    }

    // =====================================================
    // 🔥 FETCH USER LOCATION
    // =====================================================

    fun fetchUserLocation(

        userId: String,

        onResult: (
            Double,
            Double,
            String,
            String,
            String
        ) -> Unit
    ) {

        viewModelScope.launch {

            try {

                val document =

                    db.collection("users")

                        .document(userId)

                        .get()

                document.addOnSuccessListener {

                    val latitude =

                        it.getDouble(
                            "latitude"
                        ) ?: 0.0

                    val longitude =

                        it.getDouble(
                            "longitude"
                        ) ?: 0.0

                    val city =

                        it.getString(
                            "city"
                        ) ?: ""

                    val area =

                        it.getString(
                            "area"
                        ) ?: ""

                    val fullAddress =

                        it.getString(
                            "fullAddress"
                        ) ?: ""

                    onResult(

                        latitude,

                        longitude,

                        city,

                        area,

                        fullAddress
                    )
                }

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }
    }

    // =====================================================
    // 🔥 LOGOUT
    // =====================================================

    fun logout() {

        providerListener?.remove()

        authManager.logout()

        _authState.value =
            AuthState.Unauthenticated
    }
}