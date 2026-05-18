package com.example.lbo_marketplace.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing Authentication State and User Sessions.
 * 
 * WHAT IT DOES:
 * - Orchestrates login, registration, and session persistence.
 * - Listens for real-time changes in provider request status via Firestore.
 * - Exposes [authState] to the UI to trigger navigation changes.
 * 
 * REFERENCED IN: [AppNavigation], [AuthSessionTestScreen], [UserMainScreen], [ProviderDashboard]
 */
class AuthViewModel : ViewModel() {

    private val authManager = FirebaseAuthManager()

    // The single source of truth for the app's authentication state
    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    // 🔥 REAL-TIME LISTENER: Tracks if a provider's status changes from PENDING to APPROVED/REJECTED.
    private var providerListener: ListenerRegistration? = null

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

    /**
     * Handles User Login.
     * 
     * LOGIC:
     * 1. Sets state to [AuthState.Loading] to show the GIF.
     * 2. Calls [FirebaseAuthManager.login].
     * 3. On success, starts Firestore listener and updates state.
     * 
     * REFERENCED IN: [AuthSessionTestScreen] (Login Button)
     */
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

    /**
     * Checks if a user is already logged in (Persistence).
     * 
     * LOGIC:
     * 1. Sets state to [AuthState.Loading] (this triggers the Logo GIF).
     * 2. Checks Firebase local cache for an active session.
     * 3. If found, starts Firestore listener and navigates to Dashboard.
     * 4. If not found, sets state to Unauthenticated (navigates to Login).
     * 
     * REFERENCED IN: [AppNavigation] (LaunchedEffect on App Start)
     */
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

    /**
     * Real-time Firestore Listener for Provider Status.
     * 
     * LOGIC:
     * - Listens to the "provider_requests" collection for the current user's ID.
     * - If status is "PENDING" -> State becomes [AuthState.ProviderPending].
     * - If status is "APPROVED" -> State becomes [AuthState.Authenticated] with role "SERVICE_PROVIDER".
     * - If status is "REJECTED" -> State becomes [AuthState.Authenticated] with role "USER".
     * 
     * REFERENCED IN: [register], [login], [checkSession]
     */
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
            }
    }

    /**
     * Clears the current user session and listeners.
     * 
     * REFERENCED IN: [ProfileTab], [ProfileScreen] (Logout Button)
     */
    fun logout() {
        providerListener?.remove()
        authManager.logout()
        _authState.value = AuthState.Unauthenticated
    }
}