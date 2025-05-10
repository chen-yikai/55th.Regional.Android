package com.example.a55thandroid.services

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi

const val player_channel = "player_service_channel"

class App : Application() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForegroundService(Intent(this, PlaybackService::class.java))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val manager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            player_channel,
            "Media Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)
    }
}