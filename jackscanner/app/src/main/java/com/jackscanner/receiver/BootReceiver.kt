package com.jackscanner.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.jackscanner.data.preferences.PreferencesManager
import com.jackscanner.service.BleScanService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Boot receiver - auto-starts scanning after device reboot
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val scannerSettings = preferencesManager.scannerSettings.first()
                    if (scannerSettings.autoStartOnBoot) {
                        val serviceIntent = Intent(context, BleScanService::class.java).apply {
                            action = BleScanService.ACTION_START_SCANNING
                        }
                        
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(serviceIntent)
                        } else {
                            context.startService(serviceIntent)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}