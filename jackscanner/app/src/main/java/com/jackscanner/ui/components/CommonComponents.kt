package com.jackscanner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jackscanner.ui.theme.BlueMeanieTheme

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = BlueMeanieTheme.colors
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = colors.border,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.card
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick ?: {}
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    val colors = BlueMeanieTheme.colors
    
    GlassCard(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = colors.textTertiary,
                letterSpacing = 1.sp
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
fun RadarAnimation(
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = BlueMeanieTheme.colors
    
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    
    val outerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "outerAlpha"
    )
    
    val middleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "middleAlpha"
    )
    
    val innerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "innerAlpha"
    )
    
    val scanLineRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanRotation"
    )
    
    Box(
        modifier = modifier
            .size(200.dp)
            .drawBehind {
                val center = Offset(size.width / 2, size.height / 2)
                val maxRadius = size.width / 2
                
                if (isScanning) {
                    // Outer ring
                    drawCircle(
                        color = colors.primary.copy(alpha = outerAlpha),
                        radius = maxRadius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    
                    // Middle ring
                    drawCircle(
                        color = colors.primary.copy(alpha = middleAlpha),
                        radius = maxRadius * 0.7f,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    
                    // Inner ring
                    drawCircle(
                        color = colors.primary.copy(alpha = innerAlpha),
                        radius = maxRadius * 0.4f,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    
                    // Center dot
                    drawCircle(
                        color = colors.primary,
                        radius = 6.dp.toPx(),
                        center = center
                    )
                    
                    // Scan line
                    val scanLength = maxRadius * 0.9f
                    val angle = Math.toRadians(scanLineRotation.toDouble())
                    val startX = center.x
                    val startY = center.y
                    val endX = center.x + (scanLength * kotlin.math.cos(angle)).toFloat()
                    val endY = center.y + (scanLength * kotlin.math.sin(angle)).toFloat()
                    
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colors.primary.copy(alpha = 0.8f),
                                colors.primary.copy(alpha = 0f)
                            ),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY)
                        ),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 3.dp.toPx()
                    )
                } else {
                    // Static idle state
                    drawCircle(
                        color = colors.textTertiary.copy(alpha = 0.3f),
                        radius = maxRadius,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = colors.textTertiary.copy(alpha = 0.2f),
                        radius = maxRadius * 0.7f,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = colors.textTertiary.copy(alpha = 0.1f),
                        radius = maxRadius * 0.4f,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = colors.textTertiary,
                        radius = 4.dp.toPx(),
                        center = center
                    )
                }
            }
    )
}

@Composable
fun DetectionNotice(
    modifier: Modifier = Modifier
) {
    val colors = BlueMeanieTheme.colors
    
    GlassCard(modifier = modifier) {
        Column {
            Text(
                text = "Detection Notice",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.statusWarning
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "This detection represents a Bluetooth Low Energy advertisement received by your device and matched against BlueMeanie detection criteria.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Detection locations represent the position of the detecting device when the advertisement was received and should not be interpreted as the exact position of the detected device.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Bluetooth reception range varies significantly depending on hardware, obstacles, interference and environmental conditions.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
fun StatusBadge(
    status: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = BlueMeanieTheme.colors
    val backgroundColor = if (isActive) colors.statusActive.copy(alpha = 0.2f) else colors.textTertiary.copy(alpha = 0.2f)
    val textColor = if (isActive) colors.statusActive else colors.textTertiary
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = if (isActive) "● $status" else "○ $status",
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    val colors = BlueMeanieTheme.colors
    
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = colors.textTertiary,
        letterSpacing = 1.5.sp,
        modifier = modifier.padding(vertical = 8.dp)
    )
}