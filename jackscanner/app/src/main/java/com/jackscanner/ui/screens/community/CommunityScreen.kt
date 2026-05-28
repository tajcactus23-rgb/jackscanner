package com.jackscanner.ui.screens.community

import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jackscanner.domain.model.UserRank
import com.jackscanner.ui.components.GlassCard
import com.jackscanner.ui.components.GradientUsername
import com.jackscanner.ui.theme.BlueMeanieTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CommunityScreen(
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = BlueMeanieTheme.colors
    var messageText by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "COMMUNITY",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Connect with other scanners",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary
                )
            }
            
            // Profile Avatar
            IconButton(onClick = { viewModel.toggleProfileCard() }) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = colors.primary
                    )
                }
            }
        }
        
        // Profile Card (if visible)
        uiState.userProfile?.let { profile ->
            if (uiState.showProfileCard) {
                ProfileCard(profile = profile)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Chat Messages or Empty State
        if (uiState.messages.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "💬",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Messages Yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "Start a conversation with the community",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textTertiary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.messages) { message ->
                    ChatMessageItem(
                        message = message,
                        onReaction = { reaction -> viewModel.addReaction(message.id, reaction) }
                    )
                }
            }
        }
        
        // Message Input
        MessageInput(
            value = messageText,
            onValueChange = { messageText = it },
            onSend = {
                if (messageText.isNotBlank()) {
                    viewModel.sendMessage(messageText)
                    messageText = ""
                }
            }
        )
        
        // Bottom spacing
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun ProfileCard(profile: com.jackscanner.domain.model.UserProfile) {
    val colors = BlueMeanieTheme.colors
    
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.username.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profile.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    if (profile.isAnonymous) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(Anonymous)",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textTertiary
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profile.rank.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.statusGold,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "${profile.reputationScore} pts",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProfileStat("DETECTIONS", profile.detectionCount.toString())
            ProfileStat("MESSAGES", profile.messages.toString())
            ProfileStat("RANK", profile.rank.displayName)
        }
    }
}

@Composable
private fun ProfileStat(label: String, value: String) {
    val colors = BlueMeanieTheme.colors
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary
        )
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    onReaction: (String) -> Unit,
    showDevBadge: Boolean = false,
    showColoredUsername: Boolean = false,
    showChatBorder: Boolean = false
) {
    val colors = BlueMeanieTheme.colors
    val isDevMessage = message.isDevMessage
    
    Box(modifier = Modifier.fillMaxWidth()) {
        // Golden border for dev messages
        if (showChatBorder && isDevMessage) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp)
                    .drawBehind {
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFFD700),
                                    Color(0xFFFF8C00),
                                    Color(0xFFFFD700)
                                )
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(22.dp.toPx(), 22.dp.toPx())
                        )
                    }
            )
        }
        
        GlassCard {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar with golden glow for dev
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDevMessage) Color(0xFFFFD700).copy(alpha = 0.3f)
                                else colors.primary.copy(alpha = 0.2f)
                            )
                            .then(
                                if (showDevBadge && isDevMessage) {
                                    Modifier.drawBehind {
                                        drawCircle(
                                            brush = Brush.linearGradient(
                                                colors = listOf(Color(0xFFFFD700), Color(0xFFFF8C00))
                                            )
                                        )
                                    }
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (message.isAnonymous) "?" else message.username.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isDevMessage) Color(0xFFFFD700) else colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Username with optional gradient
                            if (showColoredUsername && isDevMessage) {
                                GradientUsername(username = message.username, enabled = true)
                            } else {
                                Text(
                                    text = if (message.isAnonymous) "Anonymous" else message.username,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }
                            
                            // Dev Badge
                            if (showDevBadge && isDevMessage) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFFFD700).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "DEV",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFFFFD700),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formatTime(message.timestamp),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textTertiary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary
                )

                // Reactions with dev styling
                if (message.reactions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        message.reactions.forEach { (emoji, count) ->
                            Surface(
                                modifier = Modifier.clickable { onReaction(emoji) },
                                color = if (isDevMessage) Color(0xFFFFD700).copy(alpha = 0.2f)
                                        else colors.primary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "$emoji $count",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isDevMessage) Color(0xFFFFD700) else colors.textPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val colors = BlueMeanieTheme.colors
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    text = "Type a message...",
                    color = colors.textTertiary
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.border,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary
            ),
            shape = RoundedCornerShape(24.dp),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        IconButton(
            onClick = onSend,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colors.primary)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = colors.background
            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

