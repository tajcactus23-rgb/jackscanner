package com.jackscanner.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests for [ScanController] — covers the PR changes:
 * - startScanning() now returns Boolean and always uses startForegroundService
 * - stopScanning() now returns Boolean and uses startService
 * - Both methods wrap service calls in try-catch and return false on exception
 */
class ScanControllerTest {

    private lateinit var context: Context
    private lateinit var scanController: ScanController

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        scanController = ScanController(context)
    }

    // ── startScanning ──────────────────────────────────────────────────────────

    @Test
    fun `startScanning returns true when startForegroundService succeeds`() {
        every { context.startForegroundService(any()) } returns mockk()

        val result = scanController.startScanning()

        assertTrue(result)
    }

    @Test
    fun `startScanning calls startForegroundService not startService`() {
        every { context.startForegroundService(any()) } returns mockk()

        scanController.startScanning()

        verify(exactly = 1) { context.startForegroundService(any()) }
        verify(exactly = 0) { context.startService(any()) }
    }

    @Test
    fun `startScanning sends intent with ACTION_START_SCANNING`() {
        val intentSlot = slot<Intent>()
        every { context.startForegroundService(capture(intentSlot)) } returns mockk()

        scanController.startScanning()

        assertEquals(BleScanService.ACTION_START_SCANNING, intentSlot.captured.action)
    }

    @Test
    fun `startScanning targets BleScanService`() {
        val intentSlot = slot<Intent>()
        every { context.startForegroundService(capture(intentSlot)) } returns mockk()

        scanController.startScanning()

        assertEquals(
            BleScanService::class.java.name,
            intentSlot.captured.component?.className
        )
    }

    @Test
    fun `startScanning returns false when startForegroundService throws IllegalStateException`() {
        every { context.startForegroundService(any()) } throws IllegalStateException("Not allowed")

        val result = scanController.startScanning()

        assertFalse(result)
    }

    @Test
    fun `startScanning returns false when startForegroundService throws SecurityException`() {
        every { context.startForegroundService(any()) } throws SecurityException("Permission denied")

        val result = scanController.startScanning()

        assertFalse(result)
    }

    @Test
    fun `startScanning returns false when startForegroundService throws generic RuntimeException`() {
        every { context.startForegroundService(any()) } throws RuntimeException("Unexpected")

        val result = scanController.startScanning()

        assertFalse(result)
    }

    // ── stopScanning ───────────────────────────────────────────────────────────

    @Test
    fun `stopScanning returns true when startService succeeds`() {
        every { context.startService(any()) } returns mockk()

        val result = scanController.stopScanning()

        assertTrue(result)
    }

    @Test
    fun `stopScanning calls startService not startForegroundService`() {
        every { context.startService(any()) } returns mockk()

        scanController.stopScanning()

        verify(exactly = 1) { context.startService(any()) }
        verify(exactly = 0) { context.startForegroundService(any()) }
    }

    @Test
    fun `stopScanning sends intent with ACTION_STOP_SCANNING`() {
        val intentSlot = slot<Intent>()
        every { context.startService(capture(intentSlot)) } returns mockk()

        scanController.stopScanning()

        assertEquals(BleScanService.ACTION_STOP_SCANNING, intentSlot.captured.action)
    }

    @Test
    fun `stopScanning targets BleScanService`() {
        val intentSlot = slot<Intent>()
        every { context.startService(capture(intentSlot)) } returns mockk()

        scanController.stopScanning()

        assertEquals(
            BleScanService::class.java.name,
            intentSlot.captured.component?.className
        )
    }

    @Test
    fun `stopScanning returns false when startService throws IllegalStateException`() {
        every { context.startService(any()) } throws IllegalStateException("Not allowed")

        val result = scanController.stopScanning()

        assertFalse(result)
    }

    @Test
    fun `stopScanning returns false when startService throws SecurityException`() {
        every { context.startService(any()) } throws SecurityException("Permission denied")

        val result = scanController.stopScanning()

        assertFalse(result)
    }

    @Test
    fun `stopScanning returns false when startService throws generic RuntimeException`() {
        every { context.startService(any()) } throws RuntimeException("Unexpected")

        val result = scanController.stopScanning()

        assertFalse(result)
    }

    // ── ACTION constants ───────────────────────────────────────────────────────

    @Test
    fun `ACTION_START_SCANNING constant has expected value`() {
        assertEquals("com.jackscanner.START_SCANNING", BleScanService.ACTION_START_SCANNING)
    }

    @Test
    fun `ACTION_STOP_SCANNING constant has expected value`() {
        assertEquals("com.jackscanner.STOP_SCANNING", BleScanService.ACTION_STOP_SCANNING)
    }

    @Test
    fun `startScanning and stopScanning use different actions`() {
        assertFalse(
            BleScanService.ACTION_START_SCANNING == BleScanService.ACTION_STOP_SCANNING
        )
    }

    // ── Regression: no checkBluetooth parameter ────────────────────────────────

    @Test
    fun `startScanning accepts no arguments — signature change regression`() {
        // Verifies the old checkBluetooth Boolean parameter was removed
        every { context.startForegroundService(any()) } returns mockk()

        // This should compile and call the zero-arg variant introduced in this PR
        val result = scanController.startScanning()
        assertTrue(result)
    }
}