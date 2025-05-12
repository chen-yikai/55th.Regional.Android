package com.example.a55thandroid.services

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.glance.appwidget.updateAll
import com.example.a55thandroid.widget.Glance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

const val player_channel = "player_service_channel"

class App : Application() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        createChannel()
        updateWidget()
        startForegroundService(Intent(this, PlaybackService::class.java))
    }

    private fun updateWidget() {
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        appScope.launch {
            Glance().updateAll(this@App)
        }
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