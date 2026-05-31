@file:OptIn(ExperimentalMaterial3Api::class)

package com.jackscanner.ui.screens.puzzle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jackscanner.ui.theme.BlueMeanieTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BitcoinPuzzleScreen(
    viewModel: BitcoinPuzzleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = BlueMeanieTheme.colors
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "PUZZLE",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.primary,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Puzzle Selector
            item {
                PuzzleSelectorCard(uiState, viewModel)
            }
            
            // Search Methods
            item {
                SearchMethodsCard(uiState, viewModel)
            }
            
            // Range Sliders
            item {
                RangeSlidersCard(uiState, viewModel)
            }
            
            // Speed & Filter Settings
            item {
                SettingsCard(uiState, viewModel)
            }
            
            // Control Buttons
            item {
                ControlButtonsCard(uiState, viewModel)
            }
            
            // Progress & Stats
            item {
                ProgressStatsCard(uiState)
            }
            
            // Results
            item {
                ResultsCard(uiState)
            }
            
            // Visualization
            item {
                VisualizationCard(uiState)
            }
        }
    }
}

@Composable
private fun PuzzleSelectorCard(uiState: PuzzleUiState, viewModel: BitcoinPuzzleViewModel) {
    val colors = BlueMeanieTheme.colors
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SELECT PUZZLE",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1, 2, 3, 4, 5, 10, 50, 100).forEach { puzzle ->
                    FilterChip(
                        selected = uiState.puzzleNumber == puzzle,
                        onClick = { viewModel.setPuzzle(puzzle) },
                        label = { Text("P$puzzle") }
                    )
                }
            }
            
            Text(
                text = "Puzzle #${uiState.puzzleNumber}: Range ${uiState.startKey.take(10)}... to ${uiState.endKey.take(10)}...",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun SearchMethodsCard(uiState: PuzzleUiState, viewModel: BitcoinPuzzleViewModel) {
    val colors = BlueMeanieTheme.colors
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SEARCH METHOD",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SearchMethod.entries.forEach { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (uiState.selectedMethod == method) colors.primary.copy(alpha = 0.2f)
                                else colors.background
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.selectedMethod == method,
                            onClick = { viewModel.setSearchMethod(method) }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = method.name.replace("_", " "),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = getMethodDescription(method),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textTertiary
                            )
                        }
                        Icon(
                            imageVector = getMethodIcon(method),
                            contentDescription = null,
                            tint = colors.primary
                        )
                    }
                }
            }
        }
    }
}

private fun getMethodDescription(method: SearchMethod) = when (method) {
    SearchMethod.SEQUENTIAL -> "Check each key in order from start to end"
    SearchMethod.RANDOM -> "Randomly select keys within the range"
    SearchMethod.FIBONACCI -> "Use Fibonacci sequence to skip through keys"
    SearchMethod.BINARY_SEARCH -> "Divide range in half each step"
}

@Composable
private fun getMethodIcon(method: SearchMethod) = when (method) {
    SearchMethod.SEQUENTIAL -> Icons.Default.KeyboardArrowRight
    SearchMethod.RANDOM -> Icons.Default.Shuffle
    SearchMethod.FIBONACCI -> Icons.Default.Calculate
    SearchMethod.BINARY_SEARCH -> Icons.Default.CallSplit
}

@Composable
private fun RangeSlidersCard(uiState: PuzzleUiState, viewModel: BitcoinPuzzleViewModel) {
    val colors = BlueMeanieTheme.colors
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "KEY RANGE",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Start Key Input
            OutlinedTextField(
                value = uiState.startKey,
                onValueChange = { viewModel.setStartKey(it) },
                label = { Text("Start Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // End Key Input
            OutlinedTextField(
                value = uiState.endKey,
                onValueChange = { viewModel.setEndKey(it) },
                label = { Text("End Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Indicator
            Text(
                text = "Current: ${uiState.currentKey}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SettingsCard(uiState: PuzzleUiState, viewModel: BitcoinPuzzleViewModel) {
    val colors = BlueMeanieTheme.colors
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SETTINGS",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Speed Slider
            Text(
                text = "Speed: ${if (uiState.delayMs == 0L) "MAX" else "${uiState.delayMs}ms"}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary
            )
            Slider(
                value = uiState.delayMs.toFloat(),
                onValueChange = { viewModel.setDelay(it.toLong()) },
                valueRange = 0f..500f,
                steps = 9,
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Show only wins:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = uiState.showOnlyWins,
                    onCheckedChange = { viewModel.setShowOnlyWins(it) }
                )
            }
        }
    }
}

@Composable
private fun ControlButtonsCard(uiState: PuzzleUiState, viewModel: BitcoinPuzzleViewModel) {
    val colors = BlueMeanieTheme.colors
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Start/Stop Button
        Button(
            onClick = { 
                if (uiState.isSearching) viewModel.stopSearch() else viewModel.startSearch()
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (uiState.isSearching) colors.statusDanger else colors.primary
            )
        ) {
            Icon(
                imageVector = if (uiState.isSearching) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (uiState.isSearching) "STOP" else "START")
        }
        
        // Reset Button
        OutlinedButton(
            onClick = { viewModel.resetSearch() },
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("RESET")
        }
    }
}

@Composable
private fun ProgressStatsCard(uiState: PuzzleUiState) {
    val colors = BlueMeanieTheme.colors
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SEARCH PROGRESS",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress Bar
            LinearProgressIndicator(
                progress = uiState.progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = colors.primary,
                trackColor = colors.background
            )
            Text(
                text = "${(uiState.progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
                modifier = Modifier.align(Alignment.End)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn("CHECKS", uiState.keysChecked.toString())
                StatColumn("CHECKS/SEC", uiState.checksPerSecond.toString())
                StatColumn("TOTAL", uiState.totalChecked.toString())
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    val colors = BlueMeanieTheme.colors
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
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
private fun ResultsCard(uiState: PuzzleUiState) {
    val colors = BlueMeanieTheme.colors
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (uiState.foundKey != null) colors.statusActive.copy(alpha = 0.2f)
            else colors.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "RESULTS",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            if (uiState.foundKey != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colors.statusActive
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "KEY FOUND!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.statusActive
                    )
                }
                
                Text(
                    text = "Key: ${uiState.foundKey}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Text(
                    text = "No key found yet. Keep searching!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textTertiary
                )
            }
            
            if (uiState.balance > 0 || uiState.transactionCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatColumn("BALANCE", "${uiState.balance} BTC")
                    StatColumn("TXNS", uiState.transactionCount.toString())
                }
            }
        }
    }
}

@Composable
private fun VisualizationCard(uiState: PuzzleUiState) {
    val colors = BlueMeanieTheme.colors
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CHECK VISUALIZATION",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary
                )
                Text(
                    text = "${uiState.recentChecks.size} checks shown",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (uiState.recentChecks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No checks performed yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textTertiary
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    uiState.recentChecks.take(50).forEach { check ->
                        CheckResultRow(check)
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckResultRow(check: CheckResult) {
    val colors = BlueMeanieTheme.colors
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    
    val (bgColor, icon, textColor) = when (check.result) {
        CheckOutcome.FOUND_ACTIVE -> Triple(colors.statusActive.copy(alpha = 0.3f), Icons.Default.EmojiEvents, colors.statusActive)
        CheckOutcome.HAS_BALANCE -> Triple(colors.statusWarning.copy(alpha = 0.3f), Icons.Default.AttachMoney, colors.statusWarning)
        CheckOutcome.EMPTY -> Triple(colors.background, Icons.Default.RemoveCircleOutline, colors.textTertiary)
        CheckOutcome.ERROR -> Triple(colors.statusDanger.copy(alpha = 0.2f), Icons.Default.Error, colors.statusDanger)
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = check.key,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = check.method.name.take(4),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = timeFormat.format(Date(check.timestamp)),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary
        )
        
        if (check.balance != null && check.balance > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${check.balance} BTC",
                style = MaterialTheme.typography.labelSmall,
                color = colors.statusWarning,
                fontWeight = FontWeight.Bold
            )
        }
    }
}