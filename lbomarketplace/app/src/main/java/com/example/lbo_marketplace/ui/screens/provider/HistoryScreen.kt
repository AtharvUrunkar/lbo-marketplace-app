package com.example.lbo_marketplace.ui.screens.provider

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HistoryScreen() {

    Column(modifier = Modifier.padding(16.dp)) {

        Text("Work History", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(10.dp))

        Text("No completed jobs yet")
    }
}