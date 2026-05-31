package com.jackscanner.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Unique vibration patterns for stealth mode alerts.
 * These patterns are designed to be distinctive and attention-grabbing
 * even when the phone is in a pocket or bag.
 */
@Singleton
class VibrationPatterns @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    /**
     * All available vibration patterns for detection alerts.
     * Each pattern has a unique rhythm designed for stealth attention.
     */
    val patterns = listOf(
        VibrationPattern(
            id = "heartbeat",
            name = "Heartbeat",
            description = "Two quick pulses like a heartbeat",
            pattern = longArrayOf(0, 100, 100, 100),
            amplitudes = intArrayOf(0, 255, 0, 255),
            recommended = true
        ),
        VibrationPattern(
            id = "morse_sos",
            name = "SOS Alert",
            description = "Classic distress signal pattern",
            pattern = longArrayOf(0, 100, 50, 100, 50, 100, 200, 50, 200, 50, 200, 50, 100, 50, 100),
            amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0),
            recommended = false
        ),
        VibrationPattern(
            id = "radar_pulse",
            name = "Radar Pulse",
            description = "Expanding radar wave effect",
            pattern = longArrayOf(0, 50, 150, 50, 100, 50, 150, 50, 200),
            amplitudes = intArrayOf(0, 100, 200, 100, 150, 100, 200, 100, 255),
            recommended = true
        ),
        VibrationPattern(
            id = "siren",
            name = "Siren",
            description = "Up and down alarm style",
            pattern = longArrayOf(0, 200, 100, 50, 200, 100, 50, 200, 100, 50, 200),
            amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0),
            recommended = false
        ),
        VibrationPattern(
            id = "triple_tap",
            name = "Triple Tap",
            description = "Three distinct attention grabs",
            pattern = longArrayOf(0, 150, 100, 150, 100, 150),
            amplitudes = intArrayOf(0, 255, 0, 255, 0, 255),
            recommended = true
        ),
        VibrationPattern(
            id = "accelerating",
            name = "Accelerating",
            description = "Speed increases for urgency",
            pattern = longArrayOf(0, 100, 80, 90, 70, 80, 60, 70, 50, 60, 40),
            amplitudes = intArrayOf(0, 200, 210, 220, 230, 240, 250, 255, 255, 255, 255),
            recommended = true
        ),
        VibrationPattern(
            id = "staccato",
            name = "Staccato",
            description = "Rapid fire dot pattern",
            pattern = longArrayOf(0, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30),
            amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0),
            recommended = false
        ),
        VibrationPattern(
            id = "wave",
            name = "Ocean Wave",
            description = "Gentle but persistent wave",
            pattern = longArrayOf(0, 80, 40, 80, 40, 80, 40, 80, 40, 80, 40, 80, 40, 80),
            amplitudes = intArrayOf(0, 180, 120, 180, 120, 180, 120, 180, 120, 180, 120, 180, 120, 180),
            recommended = false
        ),
        VibrationPattern(
            id = "binary_code",
            name = "Binary Code",
            description = "01 pattern for tech feel",
            pattern = longArrayOf(0, 50, 30, 100, 30, 50, 30, 100, 30, 50, 30, 100),
            amplitudes = intArrayOf(0, 255, 0, 200, 0, 255, 0, 200, 0, 255, 0, 200),
            recommended = false
        ),
        VibrationPattern(
            id = "double_pulse",
            name = "Double Pulse",
            description = "Two strong distinct pulses",
            pattern = longArrayOf(0, 200, 150, 200),
            amplitudes = intArrayOf(0, 255, 0, 255),
            recommended = true
        ),
        VibrationPattern(
            id = "morphing",
            name = "Morphing",
            description = "Shape shifting pattern",
            pattern = longArrayOf(0, 50, 100, 50, 200, 50, 100, 50, 50, 100, 50, 200),
            amplitudes = intArrayOf(0, 150, 200, 150, 255, 150, 200, 150, 100, 180, 100, 220),
            recommended = false
        ),
        VibrationPattern(
            id = "thunder",
            name = "Thunder",
            description = "Strong impact with rumble",
            pattern = longArrayOf(0, 300, 50, 100, 50, 100, 50, 80),
            amplitudes = intArrayOf(0, 255, 100, 200, 100, 180, 100, 255),
            recommended = true
        ),
        VibrationPattern(
            id = "stealth",
            name = "Stealth",
            description = "Subtle but noticeable",
            pattern = longArrayOf(0, 30, 50, 30, 50, 30),
            amplitudes = intArrayOf(0, 180, 0, 200, 0, 220),
            recommended = false
        ),
        VibrationPattern(
            id = "panic",
            name = "Panic",
            description = "Rapid urgent vibration",
            pattern = longArrayOf(0, 50, 25, 50, 25, 50, 25, 50, 25, 50, 25, 50, 25, 50, 25),
            amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0),
            recommended = false
        ),
        VibrationPattern(
            id = "comet",
            name = "Comet Trail",
            description = "Fast with trailing effect",
            pattern = longArrayOf(0, 150, 20, 150, 20, 150, 20, 50),
            amplitudes = intArrayOf(0, 255, 180, 255, 140, 255, 100, 255),
            recommended = true
        )
    )
    
    fun getPatternById(id: String): VibrationPattern? = patterns.find { it.id == id }
    
    fun vibrate(patternId: String, repeat: Int = -1) {
        val pattern = getPatternById(patternId) ?: patterns.first()
        vibratePattern(pattern, repeat)
    }
    
    fun vibratePattern(pattern: VibrationPattern, repeat: Int = -1) {
        if (!vibrator.hasVibrator()) return
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Use amplitude control for precise vibration
                val effect = VibrationEffect.createWaveform(
                    pattern.pattern,
                    pattern.amplitudes,
                    repeat
                )
                vibrator.vibrate(effect)
            } else {
                // Fallback for older devices
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern.pattern, repeat)
            }
        } catch (e: Exception) {
            // Fallback to simple vibration
            try {
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            } catch (e2: Exception) {
                // Device doesn't support vibration
            }
        }
    }
    
    fun cancel() {
        vibrator.cancel()
    }
}

data class VibrationPattern(
    val id: String,
    val name: String,
    val description: String,
    val pattern: LongArray, // timing: pause, vibrate, pause, vibrate...
    val amplitudes: IntArray, // intensity for each vibrate segment (0-255)
    val recommended: Boolean // show as recommended option
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as VibrationPattern
        
        if (id != other.id) return false
        if (!pattern.contentEquals(other.pattern)) return false
        if (!amplitudes.contentEquals(other.amplitudes)) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + pattern.contentHashCode()
        result = 31 * result + amplitudes.contentHashCode()
        return result
    }
}