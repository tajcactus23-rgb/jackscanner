package com.jackscanner.data.network

object BitcoinAddressValidator {
    private val legacyRegex = Regex("^[13][a-km-zA-HJ-NP-Z1-9]{25,34}$")
    private val p2shRegex = Regex("^3[a-km-zA-HJ-NP-Z1-9]{25,34}$")
    private val bech32Regex = Regex("^(bc1qp?|BC1QP?)[a-zA-HJ-NP-Z0-9]{25,89}$")

    fun validate(address: String): String? {
        if (address.isBlank()) return "Address cannot be empty"
        if (address.length < 26 || address.length > 62) return "Invalid address length"
        
        return when {
            legacyRegex.matches(address) -> null
            p2shRegex.matches(address) -> null
            bech32Regex.matches(address.lowercase()) -> null
            else -> "Invalid Bitcoin address format"
        }
    }

    fun isValid(address: String): Boolean = validate(address) == null
}
