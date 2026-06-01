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
    
    // Wallet Address
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
class BitcoinPuzzleViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(PuzzleUiState())
    val uiState: StateFlow<PuzzleUiState> = _uiState.asStateFlow()
    
    private var searchJob: Job? = null
    
    private val knownPuzzles = mapOf(
        1 to Pair("1048576", "2097152"),        // 2^20 to 2^21
        2 to Pair("2097152", "4194304"),
        3 to Pair("4194304", "8388608"),
        4 to Pair("8388608", "16777216"),
        5 to Pair("16777216", "33554432"),
        6 to Pair("33554432", "67108864"),
        7 to Pair("67108864", "134217728"),
        8 to Pair("134217728", "268435456"),
        9 to Pair("268435456", "536870912"),
        10 to Pair("536870912", "1073741824"),
        11 to Pair("1073741824", "2147483648"),
        12 to Pair("2147483648", "4294967296"),
        13 to Pair("4294967296", "8589934592"),
        14 to Pair("8589934592", "17179869184"),
        15 to Pair("17179869184", "34359738368"),
        16 to Pair("34359738368", "68719476736"),
        17 to Pair("68719476736", "137438953472"),
        18 to Pair("137438953472", "274877906944"),
        19 to Pair("274877906944", "549755813888"),
        20 to Pair("549755813888", "1099511627776"),
        21 to Pair("1099511627776", "2199023255552"),
        22 to Pair("2199023255552", "4398046511104"),
        23 to Pair("4398046511104", "8796093022208"),
        24 to Pair("8796093022208", "17592186044416"),
        25 to Pair("17592186044416", "35184372088832"),
        26 to Pair("35184372088832", "70368744177664"),
        27 to Pair("70368744177664", "140737488355328"),
        28 to Pair("140737488355328", "281474976710656"),
        29 to Pair("281474976710656", "562949953421312"),
        30 to Pair("562949953421312", "1125899906842624"),
        31 to Pair("1125899906842624", "2251799813685248"),
        32 to Pair("2251799813685248", "4503599627370496"),
        33 to Pair("4503599627370496", "9007199254740992"),
        34 to Pair("9007199254740992", "18014398509481984"),
        35 to Pair("18014398509481984", "36028797018963968"),
        36 to Pair("36028797018963968", "72057594037927936"),
        37 to Pair("72057594037927936", "144115188075855872"),
        38 to Pair("144115188075855872", "288230376151711744"),
        39 to Pair("288230376151711744", "576460752303423488"),
        40 to Pair("576460752303423488", "1152921504606846976"),
        41 to Pair("1152921504606846976", "2305843009213693952"),
        42 to Pair("2305843009213693952", "4611686018427387904"),
        43 to Pair("4611686018427387904", "9223372036854775808"),
        44 to Pair("9223372036854775808", "18446744073709551616"),
        45 to Pair("18446744073709551616", "36893488147419103232"),
        46 to Pair("36893488147419103232", "73786976294838206464"),
        47 to Pair("73786976294838206464", "147573952589676412928"),
        48 to Pair("147573952589676412928", "295147905179352825856"),
        49 to Pair("295147905179352825856", "590295810358705651712"),
        50 to Pair("590295810358705651712", "1180591620717411303424"),
        51 to Pair("1180591620717411303424", "2361183241434822606848"),
        52 to Pair("2361183241434822606848", "4722366482869645213696"),
        53 to Pair("4722366482869645213696", "9444732965739290427392"),
        54 to Pair("9444732965739290427392", "18889465931478580854784"),
        55 to Pair("18889465931478580854784", "37778931862957161709568"),
        56 to Pair("37778931862957161709568", "75557863725911473519136"),
        57 to Pair("75557863725911473519136", "151115727451828947038272"),
        58 to Pair("151115727451828947038272", "302231454903657894076544"),
        59 to Pair("302231454903657894076544", "604462909807315788153088"),
        60 to Pair("604462909807315788153088", "1208925819614629176306176"),
        61 to Pair("1208925819614629176306176", "2417851639229258352612352"),
        62 to Pair("2417851639229258352612352", "4835703278458516705224704"),
        63 to Pair("4835703278458516705224704", "9671406556917033410449408"),
        64 to Pair("9671406556917033410449408", "19342813113834066820898816"),
        65 to Pair("19342813113834066820898816", "38685626227668135641797632"),
        66 to Pair("38685626227668135641797632", "77371252455336271283595264"),
        67 to Pair("77371252455336271283595264", "154742504910672542567190528"),
        68 to Pair("154742504910672542567190528", "309485009821345085134381056"),
        69 to Pair("309485009821345085134381056", "618970019642690170268762112"),
        70 to Pair("618970019642690170268762112", "1237940039285380340537524224"),
        71 to Pair("1237940039285380340537524224", "2475880078570760681075048448")
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
    
    fun setWalletAddress(address: String) {
        _uiState.update { it.copy(walletAddress = address) }
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