package com.jackscanner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jackscanner.ui.theme.BlueMeanieTheme

/**
 * Dev Badge Component - Shows a golden "DEV" badge
 */
@Composable
fun DevBadge(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = Color(0xFFFFD700).copy(alpha = 0.2f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "⚡",
                fontSize = 10.sp,
                color = Color(0xFFFFD700)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "DEV",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700)
            )
        }
    }
}

/**
 * Animated Dev Avatar with golden border
 */
@Composable
fun DevAvatar(
    initial: String,
    modifier: Modifier = Modifier,
    showDevBorder: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dev_avatar")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    val colors = BlueMeanieTheme.colors
    
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0xFFFFD700).copy(alpha = 0.3f))
            .then(
                if (showDevBorder) {
                    Modifier.drawBehind {
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFFD700),
                                    Color(0xFFFF8C00),
                                    Color(0xFFFFD700)
                                )
                            ),
                            cornerRadius = CornerRadius(size.minDimension / 2, size.minDimension / 2)
                        )
                    }
                } else Modifier
            )
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = if (showDevBorder) Color(0xFFFFD700) else colors.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Gradient/Colored Username Text
 */
@Composable
fun GradientUsername(
    username: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = listOf(
        Color(0xFFFF6B6B),
        Color(0xFFFFD93D),
        Color(0xFF6BCB77),
        Color(0xFF4D96FF),
        Color(0xFF9B59B6)
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )
    
    if (enabled) {
        val gradientBrush = Brush.horizontalGradient(
            colors = colors,
            startX = offset * 200f,
            endX = offset * 200f + 100f
        )
        
        Text(
            text = username,
            modifier = modifier.drawBehind {
                drawRect(brush = gradientBrush)
            },
            color = Color.White, // Base color, gradient shows through
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Cursive
        )
    } else {
        Text(
            text = username,
            modifier = modifier,
            color = BlueMeanieTheme.colors.textPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Chat Message Container with Dev Border
 */
@Composable
fun DevChatBubble(
    modifier: Modifier = Modifier,
    showDevBorder: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = BlueMeanieTheme.colors
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (showDevBorder) {
                    Modifier
                        .drawBehind {
                            drawRoundRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFFD700),
                                        Color(0xFFFF8C00),
                                        Color(0xFFFFD700)
                                    )
                                ),
                                cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx())
                            )
                        }
                        .padding(2.dp)
                } else Modifier
            )
    ) {
        GlassCard(content = content)
    }
}

/**
 * Special Dev Reaction Button
 */
@Composable
fun DevReactionButton(
    emoji: String,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDev: Boolean = false
) {
    val colors = BlueMeanieTheme.colors
    
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = if (isDev) Color(0xFFFFD700).copy(alpha = 0.3f) else colors.primary.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp),
        border = if (isDev) {
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
        } else null
    ) {
        Text(
            text = "$emoji $count",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isDev) FontWeight.Bold else FontWeight.Normal,
            color = if (isDev) Color(0xFFFFD700) else colors.textPrimary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

/**
 * Feature Flag Toggle Card - Styled toggle for dev control panel
 */
@Composable
fun FeatureFlagCard(
    title: String,
    subtitle: String,
    emoji: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BlueMeanieTheme.colors
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        shape = RoundedCornerShape(12.dp),
        color = if (checked) colors.statusActive.copy(alpha = 0.1f) else colors.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (checked) colors.statusActive.copy(alpha = 0.3f) else colors.border
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textTertiary
                    )
                }
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.statusActive,
                    checkedTrackColor = colors.statusActive.copy(alpha = 0.5f),
                    uncheckedThumbColor = colors.textTertiary,
                    uncheckedTrackColor = colors.border
                )
            )
        }
    }
}
