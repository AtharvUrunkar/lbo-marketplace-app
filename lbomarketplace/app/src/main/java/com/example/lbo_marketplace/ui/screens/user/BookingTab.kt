package com.example.lbo_marketplace.ui.screens.user

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Booking Tab.
 * 
 * UPDATES:
 * - Header: Integrated MOVABLE header at the top of the scrollable content.
 */
@Composable
fun BookingTab(
    header: @Composable () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(scrollState)
        ) {
            // ✅ MOVABLE HEADER
            header()

            Column(modifier = Modifier.padding(16.dp)) {
                Text("My Bookings", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                Text("No bookings yet")
            }
        }
    }
}
