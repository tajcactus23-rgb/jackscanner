package com.jackscanner.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for AppTheme enum — specifically covers the addition of SIREN in this PR.
 */
class AppThemeTest {

    @Test
    fun `AppTheme SIREN exists as an enum entry`() {
        val siren = AppTheme.values().find { it == AppTheme.SIREN }
        assertNotNull("AppTheme.SIREN should exist", siren)
    }

    @Test
    fun `AppTheme SIREN has correct displayName`() {
        assertEquals("Siren", AppTheme.SIREN.displayName)
    }

    @Test
    fun `AppTheme valueOf SIREN resolves correctly`() {
        val siren = AppTheme.valueOf("SIREN")
        assertEquals(AppTheme.SIREN, siren)
    }

    @Test
    fun `AppTheme contains exactly 11 values after SIREN addition`() {
        assertEquals(11, AppTheme.values().size)
    }

    @Test
    fun `AppTheme SIREN is the last entry`() {
        val values = AppTheme.values()
        assertEquals(AppTheme.SIREN, values.last())
    }

    @Test
    fun `AppTheme all pre-existing entries are still present`() {
        val expected = listOf(
            "BLUE_MEANIE_CLASSIC",
            "CARBON",
            "TITANIUM",
            "AURORA",
            "MONOLITH",
            "ARCTIC",
            "MIDNIGHT",
            "QUANTUM",
            "NOVA",
            "GLASS",
            "SIREN"
        )
        val actual = AppTheme.values().map { it.name }
        assertEquals(expected, actual)
    }

    @Test
    fun `AppTheme SIREN displayName is non-empty`() {
        assertTrue(AppTheme.SIREN.displayName.isNotEmpty())
    }

    @Test
    fun `AppTheme GLASS displayName is still Glass after SIREN was appended`() {
        // Regression: adding SIREN should not affect the GLASS entry
        assertEquals("Glass", AppTheme.GLASS.displayName)
    }

    @Test
    fun `AppTheme ordinal of SIREN is 10`() {
        assertEquals(10, AppTheme.SIREN.ordinal)
    }

    @Test
    fun `AppTheme entries list contains SIREN`() {
        assertTrue(AppTheme.entries.contains(AppTheme.SIREN))
    }
}