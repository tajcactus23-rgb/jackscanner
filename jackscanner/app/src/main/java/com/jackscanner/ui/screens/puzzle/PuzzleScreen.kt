package com.jackscanner.ui.screens.puzzle

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

val NeonRed = Color(0xFFFF0040)
val NeonBlue = Color(0xFF0080FF)
val NeonDark = Color(0xFF0A0A12)
val NeonPurple = Color(0xFFCC00FF)
val AlertYellow = Color(0xFFFFFF00)

@Composable
fun PuzzleScreen(viewModel: PuzzleViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "siren")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    val backgroundGradient = if (uiState.isSearching) {
        Brush.verticalGradient(colors = listOf(
            NeonDark.copy(alpha = pulseAlpha),
            NeonRed.copy(alpha = 0.1f * pulseAlpha),
            NeonDark,
            NeonBlue.copy(alpha = 0.1f * pulseAlpha)
        ))
    } else {
        Brush.verticalGradient(colors = listOf(NeonDark, NeonDark.copy(alpha = 0.95f)))
    }

    Column(modifier = Modifier.fillMaxSize().background(backgroundGradient).padding(16.dp).verticalScroll(rememberScrollState())) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.fillMaxWidth().height(48.dp).offset(y = 8.dp).background(
                Brush.horizontalGradient(colors = listOf(
                    NeonBlue.copy(alpha = 0.3f * pulseAlpha),
                    NeonPurple.copy(alpha = 0.2f),
                    NeonRed.copy(alpha = 0.3f * pulseAlpha)
                )), shape = RoundedCornerShape(8.dp)).blur(20.dp))
            Text("PUZZLE SCANNER", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 4.sp)
        }

        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent), border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.5f))) {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatBox("INDEX", "${uiState.currentIndex}", NeonBlue, pulseAlpha)
                StatBox("CHECKED", "${uiState.checkedCount}", NeonRed, pulseAlpha)
                StatBox("FOUND", "${uiState.results.count { it.found }}", NeonPurple, pulseAlpha)
                StatBox("TOTAL BTC", String.format("%.4f", uiState.totalBtcRecovered), AlertYellow, pulseAlpha)
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).shadow(8.dp, RoundedCornerShape(12.dp)), colors = CardDefaults.cardColors(containerColor = NeonDark.copy(alpha = 0.9f)), border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.3f))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("RANGE SCAN", color = NeonBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 2.sp)
                    Text("${uiState.startIndex} - ${uiState.endIndex}", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("START", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                        Slider(value = uiState.startIndex.toFloat(), onValueChange = { viewModel.setRange(it.toInt(), uiState.endIndex) }, valueRange = 0f..30000f, colors = SliderDefaults.colors(thumbColor = NeonBlue, activeTrackColor = NeonBlue))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("END", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                        Slider(value = uiState.endIndex.toFloat(), onValueChange = { viewModel.setRange(uiState.startIndex, it.toInt()) }, valueRange = 0f..30000f, colors = SliderDefaults.colors(thumbColor = NeonRed, activeTrackColor = NeonRed))
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).shadow(8.dp, RoundedCornerShape(12.dp)), colors = CardDefaults.cardColors(containerColor = NeonDark.copy(alpha = 0.9f)), border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.3f))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("SEARCH METHOD", color = NeonPurple, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PuzzleMethod.entries.take(3).forEach { method ->
                        MethodChip(method, uiState.selectedMethod == method, uiState.isSearching) { viewModel.setMethod(method) }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PuzzleMethod.entries.drop(3).forEach { method ->
                        MethodChip(method, uiState.selectedMethod == method, uiState.isSearching) { viewModel.setMethod(method) }
                    }
                }
            }
        }

        Button(onClick = { if (uiState.isSearching) viewModel.stopSearch() else viewModel.startSearch() }, modifier = Modifier.fillMaxWidth().height(56.dp).shadow(if (uiState.isSearching) 16.dp else 8.dp, RoundedCornerShape(12.dp)), colors = ButtonDefaults.buttonColors(containerColor = if (uiState.isSearching) NeonRed else NeonBlue), shape = RoundedCornerShape(12.dp)) {
            Text(text = if (uiState.isSearching) "■ STOP" else "▶ START SCAN", fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 2.sp)
        }

        if (uiState.isSearching || uiState.progress > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(progress = { uiState.progress }, modifier = Modifier.fillMaxWidth().height(8.dp), color = NeonRed, trackColor = NeonDark)
        }

        Card(modifier = Modifier.fillMaxWidth().height(160.dp).padding(vertical = 12.dp).shadow(12.dp, RoundedCornerShape(12.dp)), colors = CardDefaults.cardColors(containerColor = Color(0xFF050508)), border = BorderStroke(1.dp, if (uiState.results.lastOrNull()?.found == true) NeonRed.copy(alpha = 0.8f) else NeonBlue.copy(alpha = 0.3f))) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("OUTPUT", color = NeonBlue, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 2.sp)
                    if (uiState.isSearching) { Box(modifier = Modifier.size(8.dp).background(NeonRed, RoundedCornerShape(4.dp))) }
                }
                Divider(color = NeonBlue.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                Text(text = uiState.outputLog.ifEmpty { "> Waiting for scan...\n> Select range and method above\n> Press START to begin" }, color = if (uiState.outputLog.contains("MATCH")) NeonRed else Color(0xFF00FF00), fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.verticalScroll(rememberScrollState()))
            }
        }

        Text("BITCOIN PUZZLE DATABASE", color = NeonBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 2.sp, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        LazyColumn {
            items(uiState.realPuzzles.sortedByDescending { it.index }.take(15)) { puzzle ->
                val statusColor = when (puzzle.status) {
                    PuzzleStatus.SOLVED -> NeonRed
                    PuzzleStatus.ACTIVE -> NeonBlue
                    PuzzleStatus.MONITORING -> NeonPurple
                    PuzzleStatus.UNSOLVED -> Color(0xFF666666)
                }
                val statusLabel = when (puzzle.status) {
                    PuzzleStatus.SOLVED -> "SOLVED"
                    PuzzleStatus.ACTIVE -> "ACTIVE"
                    PuzzleStatus.MONITORING -> "WATCH"
                    PuzzleStatus.UNSOLVED -> "OPEN"
                }

                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).shadow(4.dp, RoundedCornerShape(8.dp)), colors = CardDefaults.cardColors(containerColor = NeonDark.copy(alpha = 0.95f)), border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                            Text("#${puzzle.index}", color = statusColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(statusLabel, color = statusColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${puzzle.btcBalance} BTC", color = AlertYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Text(puzzle.address.take(20) + "...", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                            Text("${puzzle.txCount} txns | ${sdf.format(Date(puzzle.lastActivity))}", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, color: Color, pulse: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, letterSpacing = 1.sp)
    }
}

@Composable
private fun MethodChip(method: PuzzleMethod, selected: Boolean, disabled: Boolean, onClick: () -> Unit) {
    val bgColor = when {
        selected && method == PuzzleMethod.SEQUENTIAL -> NeonBlue
        selected && method == PuzzleMethod.RANDOM -> NeonRed
        selected -> NeonPurple
        else -> NeonDark
    }
    Surface(onClick = { if (!disabled) onClick() }, modifier = Modifier.weight(1f), color = bgColor.copy(alpha = if (selected) 1f else 0.3f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, bgColor.copy(alpha = if (selected) 1f else 0.3f))) {
        Text(text = method.displayName, color = if (selected) Color.White else Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.padding(vertical = 8.dp), letterSpacing = 1.sp)
    }
}
