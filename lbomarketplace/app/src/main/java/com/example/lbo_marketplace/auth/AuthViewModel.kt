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

    private val authManager = FirebaseAuthManager()

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    // 🔥 REAL-TIME LISTENER
    private var providerListener: ListenerRegistration? = null

    // ---------------- REGISTER ----------------
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = authManager.register(name, email, password)

            _authState.value = result.fold(
                onSuccess = {
                    startProviderListener(it.first)
                    AuthState.Authenticated(it.first, it.second)
                },
                onFailure = { AuthState.Error(it.message ?: "Error") }
            )
        }
    }

    // ---------------- LOGIN ----------------
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = authManager.login(email, password)

            _authState.value = result.fold(
                onSuccess = {
                    startProviderListener(it.first)
                    AuthState.Authenticated(it.first, it.second)
                },
                onFailure = { AuthState.Error(it.message ?: "Error") }
            )
        }
    }

    // ---------------- SESSION ----------------
    fun checkSession() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = authManager.checkSession()

            _authState.value = result.fold(
                onSuccess = {
                    startProviderListener(it.first)
                    AuthState.Authenticated(it.first, it.second)
                },
                onFailure = { AuthState.Unauthenticated }
            )
        }
    }

    // ---------------- LISTENER ----------------
    private fun startProviderListener(userId: String) {
        val db = FirebaseFirestore.getInstance()

        providerListener?.remove()

        providerListener = db.collection("provider_requests")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val status = snapshot.documents[0].getString("status")

                    when (status) {
                        "PENDING" -> {
                            _authState.value = AuthState.ProviderPending
                        }
                        "APPROVED" -> {
                            _authState.value = AuthState.Authenticated(userId, "SERVICE_PROVIDER")
                        }
                        "REJECTED" -> {
                            _authState.value = AuthState.Authenticated(userId, "USER")
                        }
                    }
                }
            } // ← closes addSnapshotListener lambda
    }         // ← closes startProviderListener

    // ---------------- LOGOUT ----------------
    fun logout() {
        providerListener?.remove()
        authManager.logout()
        _authState.value = AuthState.Unauthenticated
    }
}