package com.example.lbo_marketplace.ui.screens.user

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lbo_marketplace.R

/**
 * Community (Notifications) Screen for User.
 * 
 * UPDATES:
 * - Header: Integrated MOVABLE header at the top of the list.
 * - Guaranteed #FFFFFF background.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityTab(
    header: @Composable () -> Unit = {}
) {
    // Mock Notifications Data
    var notifications by remember {
        mutableStateOf(
            listOf(
                NotificationData("1", "starryskies23", "Started following you", "1d", true),
                NotificationData("2", "nebulanomad", "Liked your post", "1d", true, thumbnail = R.drawable.logo),
                NotificationData("3", "emberecho", "Liked your comment", "2d", true, subText = "Happy birthday!!! 🥳🎉"),
                NotificationData("4", "lunavoyager", "Started following you", "3d", true),
                NotificationData("5", "shadowlynx", "Commented on your post", "4d", true, subText = "i'm going in september. what about you?", thumbnail = R.drawable.logo)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ✅ MOVABLE HEADER
        header()

        Text(
            text = "Community",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxSize().background(Color.White)) {
            items(notifications, key = { it.id }) { item ->
                val dismissState = rememberSwipeToDismissBoxState()
                
                if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                    LaunchedEffect(item.id) {
                        notifications = notifications.filter { it.id != item.id }
                    }
                }

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        val color = when (dismissState.dismissDirection) {
                            SwipeToDismissBoxValue.EndToStart -> Color.Red
                            else -> Color.Transparent
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color)
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                        }
                    }
                ) {
                    NotificationItem(item)
                }
            }
        }
    }
}

data class NotificationData(
    val id: String,
    val name: String,
    val action: String,
    val time: String,
    val isUnread: Boolean,
    val subText: String? = null,
    val thumbnail: Int? = null
)

@Composable
fun NotificationItem(data: NotificationData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (data.isUnread) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.Red))
            } else {
                Spacer(modifier = Modifier.width(6.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFF8F8F8)), contentAlignment = Alignment.Center) {
                Text(data.name.take(1).uppercase(), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row {
                    Text(text = data.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = data.time, color = Color.Gray, fontSize = 14.sp)
                }
                Text(text = data.action, color = Color.Gray, fontSize = 14.sp)
                if (data.subText != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.padding(start = 8.dp)) {
                        Box(modifier = Modifier.matchParentSize().background(Color.Transparent)) {
                           Box(modifier = Modifier.fillMaxHeight().width(2.dp).background(Color(0xFFE0E0E0)))
                        }
                        Text(text = data.subText, fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp), color = Color.DarkGray)
                    }
                }
            }
            if (data.thumbnail != null) {
                androidx.compose.foundation.Image(painter = painterResource(id = data.thumbnail), contentDescription = null, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
            }
        }
    }
}
