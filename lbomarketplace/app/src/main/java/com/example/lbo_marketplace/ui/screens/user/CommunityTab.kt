package com.example.lbo_marketplace.ui.screens.user

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CommunityTab() {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Community", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Coming soon: Discussions, groups, help")
        }
    }
}
