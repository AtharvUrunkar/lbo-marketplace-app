package com.example.lbo_marketplace.ui.screens.user

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lbo_marketplace.auth.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileTab(
    authViewModel: AuthViewModel,
    onApplyClick: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + scaleIn()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Profile", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Email: ${user?.email ?: "N/A"}")

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = {
                onApplyClick()
            }) {
                Text("Become a Service Provider")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = {
                authViewModel.logout()
            }) {
                Text("Logout")
            }
        }
    }
}
