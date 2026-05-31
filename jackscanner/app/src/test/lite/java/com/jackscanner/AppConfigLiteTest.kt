package com.jackscanner

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [AppConfig] in the **lite** product flavor.
 *
 * This test file lives in src/test/lite/ so it is only compiled and executed
 * when running tests against the liteDebug / liteRelease build variants.
 */
class AppConfigLiteTest {

    @Test
    fun `IS_LITE is true for lite flavor`() {
        assertTrue(
            "Lite flavor AppConfig.IS_LITE must be true",
            AppConfig.IS_LITE
        )
    }

    @Test
    fun `SHOW_BOTTOM_NAV is false for lite flavor`() {
        assertFalse(
            "Lite flavor AppConfig.SHOW_BOTTOM_NAV must be false",
            AppConfig.SHOW_BOTTOM_NAV
        )
    }

    @Test
    fun `IS_LITE compile-time constant equals true`() {
        val expected: Boolean = true
        val actual: Boolean = AppConfig.IS_LITE
        assert(expected == actual) {
            "Expected IS_LITE=true but was $actual"
        }
    }

    @Test
    fun `SHOW_BOTTOM_NAV compile-time constant equals false`() {
        val expected: Boolean = false
        val actual: Boolean = AppConfig.SHOW_BOTTOM_NAV
        assert(expected == actual) {
            "Expected SHOW_BOTTOM_NAV=false but was $actual"
        }
    }

    @Test
    fun `IS_LITE and SHOW_BOTTOM_NAV are not the same value`() {
        // IS_LITE is true, SHOW_BOTTOM_NAV is false — they must differ
        assertTrue(AppConfig.IS_LITE != AppConfig.SHOW_BOTTOM_NAV)
    }

    @Test
    fun `lite flavor IS_LITE does not equal full flavor value`() {
        // Regression guard: lite IS_LITE must be true (not false like full flavor)
        val fullFlagValue = false
        assertFalse(AppConfig.IS_LITE == fullFlagValue)
    }
}
