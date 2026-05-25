package com.jackscanner.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jackscanner.ui.components.GlassCard
import com.jackscanner.ui.theme.BlueMeanieTheme
import com.jackscanner.ui.theme.sp

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = BlueMeanieTheme.colors
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress Indicator
        Spacer(modifier = Modifier.height(48.dp))
        
        LinearProgressIndicator(
            progress = { (uiState.currentStep + 1).toFloat() / uiState.totalSteps },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = colors.primary,
            trackColor = colors.surface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Step ${uiState.currentStep + 1} of ${uiState.totalSteps}",
            style = MaterialTheme.typography.labelMedium,
            color = colors.textTertiary
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Content
        AnimatedContent(
            targetState = uiState.currentStep,
            transitionSpec = {
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                slideOutHorizontally { width -> -width } + fadeOut()
            },
            label = "onboarding_content"
        ) { step ->
            when (step) {
                0 -> WelcomeStep(onNext = { viewModel.nextStep() })
                1 -> PermissionStep(
                    title = "Nearby Devices",
                    description = "Required for BLE scanning",
                    icon = Icons.Default.Bluetooth,
                    whyRequired = "BlueMeanie needs Bluetooth access to detect Axon devices through BLE advertisements.",
                    howUsed = "Your device scans for BLE signals and matches them against known Axon device signatures.",
                    privacyImpact = "BlueMeanie only receives advertising data from nearby BLE devices. No personal data is transmitted.",
                    onGrant = { viewModel.grantBluetooth(); viewModel.nextStep() }
                )
                2 -> PermissionStep(
                    title = "Notifications",
                    description = "Alert you when devices are detected",
                    icon = Icons.Default.Notifications,
                    whyRequired = "Notifications alert you when an Axon device is detected.",
                    howUsed = "When a device is detected, you'll receive a notification with details.",
                    privacyImpact = "Notifications are generated locally on your device. No data is sent to external servers.",
                    onGrant = { viewModel.grantNotifications(); viewModel.nextStep() }
                )
                3 -> PermissionStep(
                    title = "Location",
                    description = "Required for BLE scanning on older Android",
                    icon = Icons.Default.LocationOn,
                    whyRequired = "Android 12 and earlier require location permission for BLE scanning.",
                    howUsed = "Location permission allows the scanner to detect BLE devices in your area.",
                    privacyImpact = "Your location is never shared with other users or servers.",
                    onGrant = { viewModel.grantLocation(); viewModel.nextStep() }
                )
                4 -> PermissionStep(
                    title = "Background Location",
                    description = "For continuous monitoring",
                    icon = Icons.Default.MyLocation,
                    whyRequired = "Background location enables continuous scanning even when the app is closed.",
                    howUsed = "BlueMeanie can monitor for Axon devices while running in the background.",
                    privacyImpact = "Location data stays on your device and is only used for detection purposes.",
                    onGrant = { viewModel.grantBackgroundLocation(); viewModel.nextStep() }
                )
                5 -> UsernameStep(
                    username = uiState.username,
                    isAnonymous = uiState.isAnonymous,
                    autoRotate = uiState.autoRotateUsername,
                    onUsernameChange = { viewModel.setUsername(it) },
                    onAnonymousChange = { viewModel.setAnonymous(it) },
                    onAutoRotateChange = { viewModel.setAutoRotateUsername(it) },
                    onNext = { viewModel.nextStep() }
                )
                6 -> CompleteStep(onComplete = {
                    viewModel.completeOnboarding()
                    onComplete()
                })
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Skip button for permission steps
        if (uiState.currentStep in 1..4) {
            TextButton(onClick = { viewModel.nextStep() }) {
                Text(
                    text = "Skip for now",
                    color = colors.textTertiary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    val colors = BlueMeanieTheme.colors
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Animated Logo
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(colors.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎯",
                style = MaterialTheme.typography.displayLarge
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Welcome to",
            style = MaterialTheme.typography.titleMedium,
            color = colors.textSecondary
        )
        
        Text(
            text = "BlueMeanie",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = colors.primary,
            letterSpacing = 2.sp
        )
        
        Text(
            text = "Axon Device Scanner",
            style = MaterialTheme.typography.titleSmall,
            color = colors.textTertiary
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "Detecting BLE advertisements\nMatching Axon signatures\nProtecting your privacy",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 28.dp
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
        ) {
            Text(
                text = "GET STARTED",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun PermissionStep(
    title: String,
    description: String,
    icon: ImageVector,
    whyRequired: String,
    howUsed: String,
    privacyImpact: String,
    onGrant: () -> Unit
) {
    val colors = BlueMeanieTheme.colors
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(colors.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textTertiary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Permission details
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PermissionDetail("Why Required?", whyRequired)
                HorizontalDivider(color = colors.border)
                PermissionDetail("How Used", howUsed)
                HorizontalDivider(color = colors.border)
                PermissionDetail("Privacy Impact", privacyImpact)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onGrant,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
        ) {
            Text(
                text = "GRANT PERMISSION",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun PermissionDetail(title: String, content: String) {
    val colors = BlueMeanieTheme.colors
    
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = colors.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun UsernameStep(
    username: String,
    isAnonymous: Boolean,
    autoRotate: Boolean,
    onUsernameChange: (String) -> Unit,
    onAnonymousChange: (Boolean) -> Unit,
    onAutoRotateChange: (Boolean) -> Unit,
    onNext: () -> Unit
) {
    val colors = BlueMeanieTheme.colors
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Choose Your Identity",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Username (optional)") },
            enabled = !isAnonymous,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.border,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                disabledTextColor = colors.textTertiary,
                disabledBorderColor = colors.border
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        GlassCard {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Anonymous Mode",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Hide your identity from other users",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textTertiary
                        )
                    }
                    Switch(
                        checked = isAnonymous,
                        onCheckedChange = onAnonymousChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.primary,
                            checkedTrackColor = colors.primary.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
        
        if (isAnonymous) {
            Spacer(modifier = Modifier.height(16.dp))
            
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-rotate Username",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Automatically rotate username every 24 hours",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textTertiary
                        )
                    }
                    Switch(
                        checked = autoRotate,
                        onCheckedChange = onAutoRotateChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.primary,
                            checkedTrackColor = colors.primary.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
        ) {
            Text(
                text = "CONTINUE",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun CompleteStep(onComplete: () -> Unit) {
    val colors = BlueMeanieTheme.colors
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(colors.statusActive.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = colors.statusActive,
                modifier = Modifier.size(64.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "You're All Set!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "BlueMeanie is ready to scan for\nAxon devices in your area.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
        ) {
            Text(
                text = "START SCANNING",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

