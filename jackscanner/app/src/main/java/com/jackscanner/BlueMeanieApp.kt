package com.jackscanner

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BlueMeanieApp : Application() {

    companion object {
        const val CHANNEL_ID_SCANNING = "bluemeanie_scanning"
        const val CHANNEL_ID_ALERTS = "bluemeanie_alerts"
        const val CHANNEL_ID_COMMUNITY = "bluemeanie_community"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Scanning Channel - Persistent foreground notification
        val scanningChannel = NotificationChannel(
            CHANNEL_ID_SCANNING,
            getString(R.string.channel_scanning_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.channel_scanning_description)
            setShowBadge(false)
            enableVibration(false)
            setSound(null, null)
        }

        // Alerts Channel - Detection alerts
        val alertsChannel = NotificationChannel(
            CHANNEL_ID_ALERTS,
            getString(R.string.channel_alerts_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.channel_alerts_description)
            enableVibration(true)
            enableLights(true)
            setShowBadge(true)
        }

        // Community Channel - Community activity
        val communityChannel = NotificationChannel(
            CHANNEL_ID_COMMUNITY,
            getString(R.string.channel_community_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.channel_community_description)
            setShowBadge(true)
        }

        notificationManager.createNotificationChannels(
            listOf(scanningChannel, alertsChannel, communityChannel)
        )
    }
}