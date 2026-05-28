package com.jackscanner.service

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun startScanning() {
        val intent = Intent(context, BleScanService::class.java).apply {
            action = BleScanService.ACTION_START_SCANNING
        }
        context.startForegroundService(intent)
    }

    fun stopScanning() {
        val intent = Intent(context, BleScanService::class.java).apply {
            action = BleScanService.ACTION_STOP_SCANNING
        }
        context.startService(intent)
    }
}