package com.jackscanner.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class BitcoinApiService @Inject constructor() {

    data class PuzzleAddress(
        val index: Int,
        val address: String,
        val balance: Double,
        val txCount: Int,
        val totalReceived: Double,
        val totalSent: Double,
        val lastActivity: Long,
        val isSolved: Boolean
    )

    data class BlockchainResult(
        val found: Boolean,
        val puzzle: PuzzleAddress?,
        val error: String?
    )

    suspend fun checkAddress(address: String): BlockchainResult = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://blockchain.info/rawaddr/$address?limit=1")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            try {
                val responseCode = connection.responseCode
                if (responseCode != 200) {
                    return@withContext BlockchainResult(false, null, "HTTP $responseCode")
                }

                val body = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(body)

                val balanceSats = json.getLong("final_balance")
                val totalReceivedSats = json.getLong("total_received")
                val totalSentSats = json.getLong("total_sent")
                val txCount = json.getInt("n_tx")
                val latestTx = if (json.has("txs") && json.getJSONArray("txs").length() > 0) {
                    json.getJSONArray("txs").getJSONObject(0)
                } else null
                val lastActivity = latestTx?.let { 
                    it.getLong("time") * 1000 
                } ?: System.currentTimeMillis()

                BlockchainResult(
                    found = true,
                    puzzle = PuzzleAddress(
                        index = -1,
                        address = address,
                        balance = balanceSats / 100_000_000.0,
                        txCount = txCount,
                        totalReceived = totalReceivedSats / 100_000_000.0,
                        totalSent = totalSentSats / 100_000_000.0,
                        lastActivity = lastActivity,
                        isSolved = balanceSats > 0
                    ),
                    error = null
                )
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            BlockchainResult(false, null, e.message ?: "Unknown error")
        }
    }

    suspend fun checkMultiple(addresses: List<String>): List<Pair<String, BlockchainResult>> = withContext(Dispatchers.IO) {
        addresses.map { address ->
            address to checkAddress(address)
        }
    }

    companion object {
        fun indexToPuzzleAddress(index: Int): String {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val input = "Bitcoin Puzzle #$index"
            val hashBytes = digest.digest(input.toByteArray())
            val hashHex = hashBytes.joinToString("") { "%02x".format(it) }
            return "1" + hashHex.take(24)
        }
    }
}
