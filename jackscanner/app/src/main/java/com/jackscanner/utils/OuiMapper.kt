package com.jackscanner.utils

/**
 * OUI (Organization Unique Identifier) lookup for Axon devices
 * 
 * Known OUI prefixes for Axon/Taser devices:
 * - 00:25:DF (original Axon OUI)
 * - FC:A9:E8 (additional observed)
 * 
 * Reference: IEEE OUI database, FCC filings X4GS01506, X4GS00947
 */
object OuiMapper {
    
    // Primary Axon OUI - used for Axon body cameras, TASER devices
    val AXON_OUI = "00:25:DF"
    
    // Additional observed AXON prefixes from FCC/community captures
    val KNOWN_AXON_OUIS = listOf(
        "00:25:DF", // Primary Axon OUI
        "FC:A9:E8", // Additional observed
        "X4G",     // FCC Grantee code prefix
    )
    
    // FCC Grantee codes for Axon
    val FCC_GRANTEE_CODES = listOf(
        "X4G"  // Axon Enterprise Holdings
    )
    
    /**
     * Check if MAC address matches known Axon OUI prefix
     */
    fun isAxonMac(macAddress: String): Boolean {
        val macUpper = macAddress.uppercase().replace("-", ":").replace("_", ":")
        val prefix6 = if (macUpper.length >= 8) macUpper.substring(0, 8) else return false
        
        // Check first 6 chars (OUI)
        val oui = prefix6.substring(0, 6)
        
        // Also check X4G grantee code
        return AXON_OUI.equals(oui, true) || 
               KNOWN_AXON_OUIS.any { prefix6.startsWith(it.replace(":", "")) }
    }
    
    /**
     * Check device name for Axon patterns
     */
    fun isAxonName(deviceName: String?): Boolean {
        if (deviceName.isNullOrBlank()) return false
        
        val nameUpper = deviceName.uppercase()
        
        // Known Axon device patterns
        val axonPatterns = listOf(
            "AXON",
            "TASER",
            "AXON_BODY", 
            "AXON_CAMERA",
            "AXON_SIGNAL",
            "AXON_SIDEARM"
        )
        
        return axonPatterns.any { nameUpper.contains(it) }
    }
    
    /**
     * Combined check - is this an Axon device?
     */
    fun isAxonDevice(macAddress: String, deviceName: String?, rssi: Int): Boolean {
        // Primary check: MAC address matches Axon OUI
        if (isAxonMac(macAddress)) return true
        
        // Secondary check: device name contains Axon patterns  
        if (isAxonName(deviceName)) return true
        
        // Weak signal doesn't guarantee it's NOT Axon - only used for scoring
        return false
    }
}