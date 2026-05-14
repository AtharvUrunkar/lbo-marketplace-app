package com.example.lbo_marketplace.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lbo_marketplace.auth.*
import com.example.lbo_marketplace.ui.screens.user.UserMainScreen
import com.example.lbo_marketplace.ui.screens.provider.ProviderDashboard

@Composable
fun AppNavigation(viewModel: AuthViewModel = viewModel()) {

    val state = viewModel.authState.value

    // 🔥 SESSION CHECK
    LaunchedEffect(Unit) {

        viewModel.checkSession()
    }

    Surface {

        when (state) {

            is AuthState.Loading -> {
                CircularProgressIndicator()
            }

            is AuthState.Unauthenticated -> {
                AuthSessionTestScreen(viewModel)
            }

            is AuthState.Authenticated -> {

                when (state.role) {

                    // 🔥 USER FLOW (UPDATED)
                    "USER" -> UserMainScreen(viewModel)

                    // 🔥 PROVIDER FLOW
                    "SERVICE_PROVIDER" -> ProviderDashboard()

                    // ❌ REMOVE ADMIN FROM ANDROID
                    "ADMIN" -> {
                        Text("Admin panel available on web")
                    }

                    else -> {
                        AuthSessionTestScreen(viewModel)
                    }
                }
            }

            is AuthState.Error -> {
                Text("Error: ${state.message}")
            }
            is AuthState.ProviderPending -> {
                Text("⏳ Your application is under review")
            }

            else -> {}
        }
    }
}