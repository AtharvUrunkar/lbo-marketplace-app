package com.example.lbo_marketplace.ui.screens.provider

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WorkScreen() {

    Column(
        modifier = Modifier.padding(16.dp)
    ) {

        Text(
            text = "Current Work",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        Text(
            text = "No active jobs yet"
        )
    }
}