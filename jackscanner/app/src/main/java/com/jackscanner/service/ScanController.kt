package com.jackscanner.service

import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun startScanning(checkBluetooth: Boolean = true) {
        val intent = Intent(context, BleScanService::class.java).apply {
            action = BleScanService.ACTION_START_SCANNING
            putExtra("checkBluetooth", checkBluetooth)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopScanning() {
        val intent = Intent(context, BleScanService::class.java).apply {
            action = BleScanService.ACTION_STOP_SCANNING
        }
        context.startService(intent)
    }
}