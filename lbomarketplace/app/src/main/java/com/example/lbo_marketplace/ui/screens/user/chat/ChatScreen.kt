package com.example.lbo_marketplace.ui.screens.user.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lbo_marketplace.data.model.ChatMessage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ==================== 🎨 Color Palette ====================

private val ChatPrimary = Color(0xFF6C63FF)
private val ChatPrimaryDark = Color(0xFF5A52E0)
private val ChatSurface = Color(0xFFF0EEFF)
private val ChatUserBubble = Color(0xFF6C63FF)
private val ChatAiBubble = Color(0xFFF5F5FA)
private val ChatBackground = Color.White // ✅ FIXED: PURE WHITE
private val ChatInputBg = Color.White    // ✅ FIXED: PURE WHITE
private val ChatTimestamp = Color(0xFF9E9E9E)

/**
 * Main chat screen composable with modern design.
 * Provides real-time messaging, typing indicator, and smooth scrolling.
 * 
 * UPDATES:
 * - Forced Full White Background (#FFFFFF).
 * - Standardized UI to match Marketplace theme.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val messages = viewModel.messages
    val isLoading = viewModel.isLoading
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(colors = listOf(ChatPrimary, ChatPrimaryDark))),
                            contentAlignment = Alignment.Center
                        ) { Text("🤖", fontSize = 18.sp) }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("LBO Assistant", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(if (isLoading) "Typing..." else "Online", fontSize = 12.sp, color = if (isLoading) ChatPrimary else Color(0xFF4CAF50))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black) }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearChat() }) { Icon(Icons.Default.DeleteOutline, contentDescription = "Clear chat", tint = Color.Black) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = ChatBackground
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                    ) { ChatBubble(message = message) }
                }
                if (isLoading) { item { TypingIndicator() } }
            }

            if (messages.size <= 1 && !isLoading) { QuickActions { suggestion -> viewModel.sendMessage(suggestion) } }

            ChatInputBar(
                inputText = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                        coroutineScope.launch { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1) }
                    }
                },
                isLoading = isLoading
            )
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
        Box(modifier = Modifier.widthIn(max = 300.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if (isUser) 16.dp else 4.dp, bottomEnd = if (isUser) 4.dp else 16.dp)).background(if (isUser) ChatUserBubble else ChatAiBubble).padding(12.dp)) {
            Text(text = message.content, color = if (isUser) Color.White else Color.Black, fontSize = 15.sp, lineHeight = 22.sp)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = formatTimestamp(message.timestamp), fontSize = 11.sp, color = ChatTimestamp, modifier = Modifier.padding(horizontal = 4.dp))
    }
}

@Composable
private fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    Row(modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp).clip(RoundedCornerShape(16.dp)).background(ChatAiBubble).padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { index ->
            val alpha by infiniteTransition.animateFloat(initialValue = 0.3f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(600, easing = LinearEasing), repeatMode = RepeatMode.Reverse, initialStartOffset = StartOffset(index * 200)), label = "dot$index")
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ChatPrimary.copy(alpha = alpha)))
        }
    }
}

@Composable
private fun QuickActions(onSuggestionClick: (String) -> Unit) {
    val suggestions = listOf("How do I book a service?", "Show available providers", "Help with my account", "How does LBO work?")
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Suggested questions", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
        suggestions.chunked(2).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { suggestion ->
                    SuggestionChip(onClick = { onSuggestionClick(suggestion) }, label = { Text(suggestion, fontSize = 12.sp, maxLines = 1) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp), border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = ChatPrimary.copy(alpha = 0.3f)), colors = SuggestionChipDefaults.suggestionChipColors(containerColor = ChatSurface))
                }
                if (rowItems.size < 2) Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun ChatInputBar(inputText: String, onTextChange: (String) -> Unit, onSend: () -> Unit, isLoading: Boolean) {
    Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 8.dp) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = inputText, onValueChange = onTextChange, modifier = Modifier.weight(1f), placeholder = { Text("Ask me anything...", color = Color.Gray) }, shape = RoundedCornerShape(24.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ChatPrimary, unfocusedBorderColor = Color(0xFFE0E0E0), focusedContainerColor = Color(0xFFF8F8FF), unfocusedContainerColor = Color(0xFFF8F8FF)), maxLines = 3, enabled = !isLoading)
            Spacer(modifier = Modifier.width(8.dp))
            FilledIconButton(onClick = onSend, enabled = inputText.isNotBlank() && !isLoading, modifier = Modifier.size(48.dp), shape = CircleShape, colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.Black, disabledContainerColor = Color(0xFFD0D0D0))) { // ✅ Send button BLACK
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
