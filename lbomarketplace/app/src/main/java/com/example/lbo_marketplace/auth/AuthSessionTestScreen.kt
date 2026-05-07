package com.example.lbo_marketplace.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AuthSessionTestScreen(viewModel: AuthViewModel = viewModel()) {

    val context = LocalContext.current
    val state = viewModel.authState.value

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {

        TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
        Spacer(modifier = Modifier.height(8.dp))

        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        Spacer(modifier = Modifier.height(8.dp))

        TextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                Toast.makeText(context, "All fields required", Toast.LENGTH_SHORT).show()
                return@Button
            }
            viewModel.register(name, email, password)
        }) {
            Text("Register")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(context, "Email & Password required", Toast.LENGTH_SHORT).show()
                return@Button
            }
            viewModel.login(email, password)
        }) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (state) {

            is AuthState.Loading -> CircularProgressIndicator()

            is AuthState.Authenticated ->
                Text("Welcome UID: ${state.uid}, Role: ${state.role}")

            is AuthState.Error ->
                Text("Error: ${state.message}")

            is AuthState.Unauthenticated ->
                Text("Please Login")

            else -> {}
        }
    }
}