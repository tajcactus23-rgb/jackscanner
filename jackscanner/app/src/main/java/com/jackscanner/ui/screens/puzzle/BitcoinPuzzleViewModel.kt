package com.jackscanner.ui.screens.puzzle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigInteger
import javax.inject.Inject
import kotlin.random.Random

data class PuzzleUiState(
    // Search Range
    val startKey: String = "1",
    val endKey: String = "115792089237316195423570985008687907852837564279074904382605163141518161494337",
    val currentKey: BigInteger = BigInteger.ZERO,
    
    // Method Selection
    val selectedMethod: SearchMethod = SearchMethod.SEQUENTIAL,
    val fibonacciStep: BigInteger = BigInteger("1134903170"),
    
    // Search Status
    val isSearching: Boolean = false,
    val keysChecked: Long = 0,
    val checksPerSecond: Int = 0,
    val progress: Float = 0f,
    
    // Results
    val foundKey: String? = null,
    val balance: Double = 0.0,
    val transactionCount: Int = 0,
    val puzzleNumber: Int = 0,
    
    // Visualization
    val recentChecks: List<CheckResult> = emptyList(),
    val totalChecked: Long = 0,
    
    // Settings
    val delayMs: Long = 0,
    val showOnlyWins: Boolean = false
)

enum class SearchMethod {
    SEQUENTIAL,
    RANDOM,
    FIBONACCI,
    BINARY_SEARCH
}

data class CheckResult(
    val key: String,
    val timestamp: Long,
    val method: SearchMethod,
    val result: CheckOutcome,
    val balance: Double? = null
)

enum class CheckOutcome {
    EMPTY,
    HAS_BALANCE,
    FOUND_ACTIVE,
    ERROR
}

@HiltViewModel
class BitcoinPuzzleViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(PuzzleUiState())
    val uiState: StateFlow<PuzzleUiState> = _uiState.asStateFlow()
    
    private var searchJob: Job? = null
    
    private val knownPuzzles = mapOf(
        1 to Pair("1", "10"),
        2 to Pair("10", "100"),
        3 to Pair("100", "1000"),
        4 to Pair("1000", "10000"),
        5 to Pair("10000", "100000"),
        10 to Pair("100000", "1000000"),
        50 to Pair("1000000000", "10000000000"),
        100 to Pair("100000000000", "1000000000000")
    )
    
    init {
        val puzzle = knownPuzzles[1]!!
        _uiState.update {
            it.copy(
                startKey = puzzle.first,
                endKey = puzzle.second,
                currentKey = BigInteger(puzzle.first),
                puzzleNumber = 1
            )
        }
    }
    
    fun setPuzzle(puzzleNum: Int) {
        val puzzle = knownPuzzles[puzzleNum] ?: knownPuzzles[1]!!
        _uiState.update {
            it.copy(
                startKey = puzzle.first,
                endKey = puzzle.second,
                currentKey = BigInteger(puzzle.first),
                puzzleNumber = puzzleNum,
                foundKey = null,
                keysChecked = 0,
                totalChecked = 0,
                recentChecks = emptyList()
            )
        }
    }
    
    fun setSearchMethod(method: SearchMethod) {
        _uiState.update { it.copy(selectedMethod = method) }
    }
    
    fun setDelay(delayMs: Long) {
        _uiState.update { it.copy(delayMs = delayMs) }
    }
    
    fun setShowOnlyWins(showWins: Boolean) {
        _uiState.update { it.copy(showOnlyWins = showWins) }
    }
    
    fun setStartKey(key: String) {
        _uiState.update { 
            it.copy(
                startKey = key,
                currentKey = try { BigInteger(key) } catch (e: Exception) { it.currentKey }
            )
        }
    }
    
    fun setEndKey(key: String) {
        _uiState.update { it.copy(endKey = key) }
    }
    
    fun startSearch() {
        if (_uiState.value.isSearching) return
        
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, foundKey = null) }
            
            val state = _uiState.value
            var start = BigInteger(state.startKey)
            val end = BigInteger(state.endKey)
            var count = 0L
            var lastUpdate = System.currentTimeMillis()
            var checksThisSecond = 0
            
            while (_uiState.value.isSearching && start <= end) {
                val currentKey = when (state.selectedMethod) {
                    SearchMethod.SEQUENTIAL -> start
                    SearchMethod.RANDOM -> {
                        val range = end - start
                        if (range > BigInteger.ZERO && range < BigInteger(Long.MAX_VALUE.toString())) {
                            start + BigInteger(Random.nextLong(range.toLong()))
                        } else start
                    }
                    SearchMethod.FIBONACCI -> {
                        val range = end - start
                        if (range > BigInteger.ZERO) {
                            val step = state.fibonacciStep % (range + BigInteger.ONE)
                            start + step.multiply(BigInteger.valueOf(count))
                        } else start
                    }
                    SearchMethod.BINARY_SEARCH -> {
                        if (count % 2 == 0L) (start + end) / 2 else start
                    }
                }
                
                val result = simulateCheck(currentKey.toString())
                val checkResult = CheckResult(
                    key = formatKey(currentKey),
                    timestamp = System.currentTimeMillis(),
                    method = state.selectedMethod,
                    result = result.outcome,
                    balance = result.balance
                )
                
                _uiState.update { currentState ->
                    val newChecks = listOf(checkResult) + currentState.recentChecks.take(99)
                    val filtered = if (state.showOnlyWins && result.outcome == CheckOutcome.EMPTY) {
                        newChecks.filter { it.result != CheckOutcome.EMPTY }
                    } else newChecks
                    currentState.copy(
                        recentChecks = filtered,
                        keysChecked = count + 1,
                        currentKey = currentKey,
                        balance = result.balance ?: currentState.balance
                    )
                }
                
                count++
                checksThisSecond++
                val now = System.currentTimeMillis()
                if (now - lastUpdate >= 1000) {
                    _uiState.update { it.copy(checksPerSecond = checksThisSecond) }
                    checksThisSecond = 0
                    lastUpdate = now
                }
                
                val range = end - BigInteger(state.startKey)
                val progress = if (range > BigInteger.ZERO) {
                    ((currentKey - BigInteger(state.startKey)).toDouble() / range.toDouble()).toFloat()
                } else 0f
                _uiState.update { it.copy(progress = progress.coerceIn(0f, 1f), totalChecked = count) }
                
                start = start.add(BigInteger.ONE)
                
                if (state.delayMs > 0) {
                    delay(state.delayMs)
                }
                
                if (result.outcome == CheckOutcome.FOUND_ACTIVE) {
                    _uiState.update { 
                        it.copy(
                            foundKey = currentKey.toString(),
                            balance = result.balance ?: 0.0,
                            transactionCount = result.transactions ?: 0
                        )
                    }
                    break
                }
            }
            
            _uiState.update { it.copy(isSearching = false) }
        }
    }
    
    fun stopSearch() {
        searchJob?.cancel()
        _uiState.update { it.copy(isSearching = false) }
    }
    
    fun resetSearch() {
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                isSearching = false,
                foundKey = null,
                keysChecked = 0,
                currentKey = BigInteger(it.startKey),
                recentChecks = emptyList(),
                progress = 0f,
                checksPerSecond = 0
            )
        }
    }
    
    private fun simulateCheck(key: String): CheckSimulationResult {
        val hash = key.hashCode()
        return when {
            hash % 10000 == 0 -> CheckSimulationResult(
                outcome = CheckOutcome.FOUND_ACTIVE,
                balance = (hash % 100).toDouble() / 10.0,
                transactions = hash % 50
            )
            hash % 1000 == 0 -> CheckSimulationResult(
                outcome = CheckOutcome.HAS_BALANCE,
                balance = (hash % 10).toDouble() / 10.0,
                transactions = hash % 10
            )
            else -> CheckSimulationResult(outcome = CheckOutcome.EMPTY)
        }
    }
    
    private data class CheckSimulationResult(
        val outcome: CheckOutcome,
        val balance: Double? = null,
        val transactions: Int? = null
    )
    
    private fun formatKey(key: BigInteger): String {
        val str = key.toString()
        return if (str.length > 8) str.take(4) + "..." + str.takeLast(4) else str
    }
}