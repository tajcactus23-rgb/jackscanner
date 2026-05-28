package com.jackscanner.ui.screens.puzzle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jackscanner.ui.theme.BlueMeanieTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PuzzleScreen(viewModel: PuzzleViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = BlueMeanieTheme.colors
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
    ) {
        Text(
            text = "PUZZLE TOOL",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.primary,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        // Range Sliders
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Range", color = colors.textPrimary, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Start: ${uiState.startIndex}", color = colors.textSecondary, fontSize = 12.sp)
                        Slider(value = uiState.startIndex.toFloat(), onValueChange = { viewModel.setRange(it.toInt(), uiState.endIndex) }, valueRange = 0f..10000f, modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("End: ${uiState.endIndex}", color = colors.textSecondary, fontSize = 12.sp)
                        Slider(value = uiState.endIndex.toFloat(), onValueChange = { viewModel.setRange(uiState.startIndex, it.toInt()) }, valueRange = 0f..10000f, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
        
        // Method Selector
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = colors.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Method", color = colors.textPrimary, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PuzzleMethod.entries.take(3).forEach { method ->
                        FilterChip(modifier = Modifier.weight(1f), selected = uiState.selectedMethod == method, onClick = { viewModel.setMethod(method) }, label = { Text(method.displayName, fontSize = 10.sp) })
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PuzzleMethod.entries.drop(3).forEach { method ->
                        FilterChip(modifier = Modifier.weight(1f), selected = uiState.selectedMethod == method, onClick = { viewModel.setMethod(method) }, label = { Text(method.displayName, fontSize = 10.sp) })
                    }
                }
            }
        }
        
        // Start/Stop Button
        Button(onClick = { if (uiState.isSolving) viewModel.stopSolving() else viewModel.startSolving() }, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = if (uiState.isSolving) colors.statusDanger else colors.primary)) {
            Text(if (uiState.isSolving) "STOP" else "START SOLVING", fontWeight = FontWeight.Bold)
        }
        
        // Progress
        if (uiState.isSolving || uiState.progress > 0) {
            LinearProgressIndicator(progress = { uiState.progress }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
            Text("${uiState.checkedCount} checked | Index: ${uiState.currentIndex}", color = colors.textSecondary, fontSize = 12.sp)
        }
        
        // Current Output Log
        Card(modifier = Modifier.fillMaxWidth().height(120.dp).padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = colors.surface)) {
            Text(uiState.currentMethodOutput.ifEmpty { "Output will appear here..." }, modifier = Modifier.padding(12.dp), color = if (uiState.currentMethodOutput.contains("FOUND")) colors.primary else colors.textSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        }
        
        // Stats
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatItem("Found", "${uiState.results.count { it.found }}")
            StatItem("Checked", "${uiState.checkedCount}")
            StatItem("Total Solved", "${uiState.solvedPuzzles.size}")
        }
        
        // Solved Puzzles List
        Text("Solved Puzzles (${uiState.solvedPuzzles.size})", color = colors.textPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        LazyColumn {
            items(uiState.solvedPuzzles.sortedByDescending { it.index }.take(20)) { puzzle ->
                SolvedPuzzleItem(puzzle)
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    val colors = BlueMeanieTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = colors.primary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(label, color = colors.textTertiary, fontSize = 10.sp)
    }
}

@Composable
private fun SolvedPuzzleItem(puzzle: SolvedPuzzle) {
    val colors = BlueMeanieTheme.colors
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = colors.surface)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("#${puzzle.index}", color = colors.primary, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(puzzle.method.displayName, color = colors.textPrimary, fontSize = 12.sp)
                Text("${puzzle.balance} BTC | ${puzzle.transactions} txns", color = colors.textSecondary, fontSize = 11.sp)
            }
            Text(sdf.format(Date(puzzle.foundAt)), color = colors.textTertiary, fontSize = 10.sp)
        }
    }
}
