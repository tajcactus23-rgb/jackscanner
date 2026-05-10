package com.axonbleeder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.axonbleeder.service.BleScanService

/**
 * Boot receiver - auto-starts scanning after device reboot
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Get preferences
            val prefs = context.getSharedPreferences("scanner_prefs", Context.MODE_PRIVATE)
            val autoStart = prefs.getBoolean("auto_start", false)
            
            if (autoStart) {
                val serviceIntent = Intent(context, BleScanService::class.java).apply {
                    action = BleScanService.ACTION_START_SCANNING
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}