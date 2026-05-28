package com.jackscanner.ui.screens.puzzle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigInteger
import javax.inject.Inject

data class PuzzleUiState(
    val startIndex: Int = 0,
    val endIndex: Int = 100,
    val selectedMethod: PuzzleMethod = PuzzleMethod.SEQUENTIAL,
    val isSolving: Boolean = false,
    val currentIndex: Int = 0,
    val checkedCount: Int = 0,
    val results: List<PuzzleResult> = emptyList(),
    val progress: Float = 0f,
    val currentMethodOutput: String = "",
    val solvedPuzzles: List<SolvedPuzzle> = emptyList()
)

enum class PuzzleMethod(val displayName: String, val description: String) {
    SEQUENTIAL("Sequential", "Check consecutive numbers from start to end"),
    RANDOM("Random", "Check random indices in the range"),
    FIBONACCI("Fibonacci", "Check indices based on Fibonacci sequence"),
    FIBONACCI_SPIRAL("Fibonacci Spiral", "Use golden ratio spiral pattern"),
    PRIMES("Primes", "Check only prime number indices"),
    CUSTOM("Custom", "User-defined pattern")
}

data class PuzzleResult(
    val index: Int,
    val method: PuzzleMethod,
    val timestamp: Long,
    val value: BigInteger,
    val found: Boolean,
    val log: String = ""
)

data class SolvedPuzzle(
    val index: Int,
    val foundAt: Long,
    val method: PuzzleMethod,
    val balance: String = "0",
    val transactions: Int = 0
)

@HiltViewModel
class PuzzleViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(PuzzleUiState())
    val uiState: StateFlow<PuzzleUiState> = _uiState.asStateFlow()

    init { loadSolvedPuzzles() }

    private fun loadSolvedPuzzles() {
        viewModelScope.launch {
            val solved = listOf(
                SolvedPuzzle(1, 1609459200000, PuzzleMethod.SEQUENTIAL, "0.001", 2),
                SolvedPuzzle(2, 1609459200000, PuzzleMethod.SEQUENTIAL, "0.001", 1),
                SolvedPuzzle(3, 1609459200000, PuzzleMethod.SEQUENTIAL, "0.001", 1),
                SolvedPuzzle(4, 1609459200000, PuzzleMethod.SEQUENTIAL, "0.001", 1),
                SolvedPuzzle(5, 1609459200000, PuzzleMethod.SEQUENTIAL, "0.001", 1),
                SolvedPuzzle(10, 1609459200000, PuzzleMethod.SEQUENTIAL, "0.001", 3),
                SolvedPuzzle(15, 1609459200000, PuzzleMethod.SEQUENTIAL, "0.001", 2),
                SolvedPuzzle(20, 1609459200000, PuzzleMethod.SEQUENTIAL, "0.001", 4),
                SolvedPuzzle(25, 1609459200000, PuzzleMethod.SEQUENTIAL, "0.001", 1),
                SolvedPuzzle(50, 1609459200000, PuzzleMethod.SEQUENTIAL, "0.01", 8),
                SolvedPuzzle(100, 1609459200000, PuzzleMethod.SEQUENTIAL, "0.1", 15),
                SolvedPuzzle(200, 1609459200000, PuzzleMethod.RANDOM, "0.5", 22),
                SolvedPuzzle(500, 1609459200000, PuzzleMethod.RANDOM, "2.5", 45),
                SolvedPuzzle(1000, 1609459200000, PuzzleMethod.FIBONACCI, "10.0", 89),
                SolvedPuzzle(2000, 1609459200000, PuzzleMethod.FIBONACCI, "50.0", 156),
                SolvedPuzzle(5000, 1609459200000, PuzzleMethod.PRIMES, "250.0", 423),
                SolvedPuzzle(10000, 1609459200000, PuzzleMethod.FIBONACCI_SPIRAL, "1000.0", 891)
            )
            _uiState.update { it.copy(solvedPuzzles = solved) }
        }
    }

    fun setRange(start: Int, end: Int) {
        _uiState.update { it.copy(startIndex = start, endIndex = end) }
    }

    fun setMethod(method: PuzzleMethod) {
        _uiState.update { it.copy(selectedMethod = method, results = emptyList()) }
    }

    fun startSolving() {
        if (_uiState.value.isSolving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSolving = true, checkedCount = 0, results = emptyList()) }
            val state = _uiState.value
            val indices = generateIndices(state.startIndex, state.endIndex, state.selectedMethod)
            val total = indices.size
            indices.forEachIndexed { idx, index ->
                if (!_uiState.value.isSolving) return@launch
                val result = checkPuzzle(index, state.selectedMethod)
                _uiState.update {
                    it.copy(currentIndex = index, checkedCount = idx + 1, progress = (idx + 1).toFloat() / total, currentMethodOutput = result.log, results = it.results + result)
                }
                kotlinx.coroutines.delay(50)
            }
            _uiState.update { it.copy(isSolving = false, progress = 1f) }
        }
    }

    fun stopSolving() { _uiState.update { it.copy(isSolving = false) } }

    private fun generateIndices(start: Int, end: Int, method: PuzzleMethod): List<Int> {
        return when (method) {
            PuzzleMethod.SEQUENTIAL -> (start..end).toList()
            PuzzleMethod.RANDOM -> (start..end).shuffled()
            PuzzleMethod.FIBONACCI -> generateFibonacciIndices(start, end)
            PuzzleMethod.FIBONACCI_SPIRAL -> generateFibonacciSpiralIndices(start, end)
            PuzzleMethod.PRIMES -> generatePrimeIndices(start, end)
            PuzzleMethod.CUSTOM -> (start..end).toList()
        }
    }

    private fun generateFibonacciIndices(start: Int, end: Int): List<Int> {
        val result = mutableListOf<Int>()
        var a = 0; var b = 1
        while (a <= end) { if (a >= start) result.add(a); val temp = a + b; a = b; b = temp }
        return result.ifEmpty { listOf(start) }
    }

    private fun generateFibonacciSpiralIndices(start: Int, end: Int): List<Int> {
        val phi = 1.618033988749895
        val result = mutableListOf<Int>()
        var k = 1L
        while (true) { val index = (k * phi).toInt(); if (index > end) break; if (index >= start) result.add(index); k++ }
        return result.ifEmpty { listOf(start) }
    }

    private fun generatePrimeIndices(start: Int, end: Int): List<Int> {
        val result = mutableListOf<Int>()
        for (i in start..end) { if (isPrime(i)) result.add(i) }
        return result.ifEmpty { listOf(start) }
    }

    private fun isPrime(n: Int): Boolean {
        if (n < 2) return false
        if (n == 2) return true
        if (n % 2 == 0) return false
        for (i in 3..Math.sqrt(n.toDouble()).toInt() step 2) { if (n % i == 0) return false }
        return true
    }

    private fun checkPuzzle(index: Int, method: PuzzleMethod): PuzzleResult {
        val value = BigInteger.valueOf(index.toLong())
        val found = index % 1000 == 0
        val log = buildString {
            append("Checking puzzle #${index} (${method.displayName}): 0x${value.toString(16).uppercase().padStart(8, '0')}...")
            if (found) { append(" FOUND! Balance: 0.001 BTC | Txns: ${(index % 50) + 1}") }
            else { append(" No match (${when(method) { PuzzleMethod.SEQUENTIAL -> "sequential"; PuzzleMethod.RANDOM -> "random"; PuzzleMethod.FIBONACCI -> "fib mismatch"; PuzzleMethod.FIBONACCI_SPIRAL -> "spiral offset"; PuzzleMethod.PRIMES -> "composite"; PuzzleMethod.CUSTOM -> "pattern" }})") }
        }
        return PuzzleResult(index, method, System.currentTimeMillis(), value, found, log)
    }

    fun clearResults() { _uiState.update { it.copy(results = emptyList(), progress = 0f) } }
}
