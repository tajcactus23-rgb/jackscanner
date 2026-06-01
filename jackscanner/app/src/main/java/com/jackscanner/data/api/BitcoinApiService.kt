package com.jackscanner.data.api

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Bitcoin blockchain API interface for Blockstream.info
 * Provides access to address balance, transaction history, and UTXO data
 */
interface BitcoinApiService {

    /**
     * Get address information including balance and transaction counts
     * @param address Bitcoin address (base58 or bech32)
     * @return AddressInfo with chain_stats and mempool_stats
     */
    @GET("address/{address}")
    suspend fun getAddressInfo(@Path("address") address: String): AddressInfoResponse

    /**
     * Get all transactions for an address
     * @param address Bitcoin address
     * @return List of transaction details
     */
    @GET("address/{address}/txs")
    suspend fun getAddressTransactions(@Path("address") address: String): List<TransactionResponse>

    /**
     * Get UTXO (Unspent Outputs) for an address
     * @param address Bitcoin address
     * @return List of UTXOs with value and transaction details
     */
    @GET("address/{address}/utxo")
    suspend fun getAddressUtxo(@Path("address") address: String): List<UtxoResponse>
}

/**
 * Address information from blockchain - Blockstream uses snake_case
 */
data class AddressInfoResponse(
    val address: String,
    val chain_stats: ChainStats,
    val mempool_stats: MempoolStats
)

data class ChainStats(
    val funded_txo_count: Int = 0,
    val funded_txo_sum: Long = 0,
    val spent_txo_count: Int = 0,
    val spent_txo_sum: Long = 0,
    val tx_count: Int = 0
)

data class MempoolStats(
    val funded_txo_count: Int = 0,
    val funded_txo_sum: Long = 0,
    val spent_txo_count: Int = 0,
    val spent_txo_sum: Long = 0,
    val tx_count: Int = 0
)

/**
 * Transaction data
 */
data class TransactionResponse(
    val txid: String,
    val version: Int,
    val locktime: Int,
    val size: Int,
    val weight: Int,
    val fee: Long,
    val vin: List<TransactionInput>,
    val vout: List<TransactionOutput>,
    val status: TransactionStatus
)

data class TransactionInput(
    val txid: String?,
    val vout: Int?,
    val prevout: PreviousOutput?,
    val scriptsig: String?,
    val is_coinbase: Boolean = false,
    val sequence: Long
)

data class PreviousOutput(
    val scriptpubkey: String,
    val scriptpubkey_address: String?,
    val scriptpubkey_type: String,
    val value: Long
)

data class TransactionOutput(
    val scriptpubkey: String,
    val scriptpubkey_address: String?,
    val scriptpubkey_type: String,
    val value: Long
)

data class TransactionStatus(
    val confirmed: Boolean,
    val block_height: Int?,
    val block_hash: String?,
    val block_time: Long?
)

/**
 * UTXO (Unspent Transaction Output)
 */
data class UtxoResponse(
    val txid: String,
    val vout: Int,
    val value: Long,
    val status: UtxoStatus
)

data class UtxoStatus(
    val confirmed: Boolean,
    val block_height: Int?,
    val block_hash: String?,
    val block_time: Long?
)