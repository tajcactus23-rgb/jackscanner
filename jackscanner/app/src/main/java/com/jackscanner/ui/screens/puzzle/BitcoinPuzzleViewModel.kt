package com.jackscanner.ui.screens.puzzle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jackscanner.data.api.BitcoinApiService
import com.jackscanner.data.model.BitcoinPuzzle
import com.jackscanner.data.model.BitcoinPuzzleDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigInteger
import javax.inject.Inject
import kotlin.random.Random

/**
 * UI State for Bitcoin Puzzle screen
 */
data class PuzzleUiState(
    // Current puzzle from database
    val currentPuzzle: BitcoinPuzzle? = null,
    val puzzleNumber: Int = 1,
    
    // Real blockchain data
    val blockchainBalance: Long = 0,           // Balance in satoshis
    val blockchainTransactions: Int = 0,        // Transaction count from blockchain
    val isLoadingBlockchain: Boolean = false,  // Loading indicator
    val blockchainError: String? = null,        // Error message if API fails
    
    // Search Range (from puzzle)
    val startKey: String = "1048576",
    val endKey: String = "2097152",
    val currentKey: BigInteger = BigInteger.ZERO,
    
    // User wallet address
    val walletAddress: String = "",
    
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
    val balance: Double? = null,
    val transactionCount: Int? = null
)

enum class CheckOutcome {
    EMPTY,
    HAS_BALANCE,
    FOUND_ACTIVE,
    ERROR
}

@HiltViewModel
class BitcoinPuzzleViewModel @Inject constructor(
    private val bitcoinApiService: BitcoinApiService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PuzzleUiState())
    val uiState: StateFlow<PuzzleUiState> = _uiState.asStateFlow()
    
    private var searchJob: Job? = null
    
    init {
        loadPuzzle(1)
    }
    
    /**
     * Load puzzle from database and fetch real blockchain data
     */
    fun loadPuzzle(puzzleNum: Int) {
        val puzzle = BitcoinPuzzleDatabase.getPuzzle(puzzleNum) ?: BitcoinPuzzleDatabase.puzzles.first()
        
        _uiState.update {
            it.copy(
                currentPuzzle = puzzle,
                puzzleNumber = puzzle.number,
                startKey = puzzle.keyStartDecimal,
                endKey = puzzle.keyEndDecimal,
                currentKey = BigInteger(puzzle.keyStartDecimal),
                foundKey = null,
                keysChecked = 0,
                totalChecked = 0,
                recentChecks = emptyList(),
                isLoadingBlockchain = true,
                blockchainError = null
            )
        }
        
        // Fetch real blockchain data for this address
        fetchBlockchainData(puzzle.address)
    }
    
    /**
     * Fetch real balance and transaction count from Bitcoin blockchain
     */
    private fun fetchBlockchainData(address: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingBlockchain = true, blockchainError = null) }
            
            try {
                val response = withContext(Dispatchers.IO) {
                    bitcoinApiService.getAddressInfo(address)
                }
                
                // Calculate balance in satoshis (funded - spent)
                val balanceSatoshis = response.chain_stats.funded_txo_sum - response.chain_stats.spent_txo_sum
                
                _uiState.update {
                    it.copy(
                        blockchainBalance = balanceSatoshis,
                        blockchainTransactions = response.chain_stats.tx_count,
                        isLoadingBlockchain = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingBlockchain = false,
                        blockchainError = "Failed to fetch blockchain data: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun setPuzzle(puzzleNum: Int) {
        searchJob?.cancel()
        loadPuzzle(puzzleNum)
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
    
    fun setWalletAddress(address: String) {
        _uiState.update { it.copy(walletAddress = address) }
    }
    
    /**
     * Start the key search simulation
     * NOTE: Real BTC puzzle solving requires GPU farms, not mobile devices
     * This shows what the UI would look like while searching
     */
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
                        if (range > BigInteger.ZERO && range < BigInteger.valueOf(Long.MAX_VALUE)) {
                            start + BigInteger.valueOf(Random.nextLong(range.toLong()))
                        } else start
                    }
                    SearchMethod.FIBONACCI -> {
                        val range = end - start
                        if (range > BigInteger.ZERO) {
                            val step = state.fibonacciStep % (range + BigInteger.valueOf(1))
                            start + step.multiply(BigInteger.valueOf(count))
                        } else start
                    }
                    SearchMethod.BINARY_SEARCH -> {
                        if (count % 2 == 0L) (start + end) / BigInteger.valueOf(2) else start
                    }
                }
                
                val result = simulateCheck(currentKey.toString())
                val checkResult = CheckResult(
                    key = formatKey(currentKey),
                    timestamp = System.currentTimeMillis(),
                    method = state.selectedMethod,
                    result = result.outcome,
                    balance = result.balance,
                    transactionCount = result.transactions
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
                        balance = result.balance ?: currentState.balance,
                        transactionCount = result.transactions ?: currentState.transactionCount
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
    
    /**
     * Simulation for UI demonstration
     * Real puzzle solving requires GPU clusters, not mobile
     */
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
    
    /**
     * Get formatted balance in BTC
     */
    fun getBalanceBtc(): Double = _uiState.value.blockchainBalance / 100_000_000.0
}