package com.jackscanner

import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Tests for [AppConfig] in the **full** product flavor.
 *
 * This test file lives in src/test/full/ so it is only compiled and executed
 * when running tests against the fullDebug / fullRelease build variants.
 */
class AppConfigFullTest {

    @Test
    fun `IS_LITE is false for full flavor`() {
        assertFalse(
            "Full flavor AppConfig.IS_LITE must be false",
            AppConfig.IS_LITE
        )
    }

    @Test
    fun `IS_LITE compile-time constant has value false`() {
        // Confirm the const val semantics — value must equal the boolean literal false
        val expected: Boolean = false
        val actual: Boolean = AppConfig.IS_LITE
        assert(expected == actual) {
            "Expected IS_LITE=false but was $actual"
        }
    }

    @Test
    fun `full flavor navItems is not null`() {
        // navItems is a val property backed by bottomNavItems; verify it is non-null
        val items = AppConfig.navItems
        assert(items != null) { "navItems should not be null in the full flavor" }
    }

    @Test
    fun `full flavor navItems contains at least one item`() {
        val items = AppConfig.navItems
        assert(items.isNotEmpty()) {
            "navItems should not be empty in the full flavor"
        }
    }

    @Test
    fun `full flavor IS_LITE does not equal lite flag`() {
        // Regression guard: IS_LITE should never accidentally be set to true
        val liteFlag = true
        assertFalse(AppConfig.IS_LITE == liteFlag)
    }
}