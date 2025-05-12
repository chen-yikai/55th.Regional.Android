package com.example.a55thandroid.services

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
const val alarm_channel = "alarm_service_channel"

class App : Application() {
    companion object {
        private lateinit var instance: App

        fun getAppContext(): Context {
            return instance.applicationContext
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        instance = this
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
        val mediaPlaybackChannel =
            NotificationChannel(
                player_channel,
                "背景播放服務",
                NotificationManager.IMPORTANCE_LOW
            )
        val alarmChannel =
            NotificationChannel(alarm_channel, "聲景播放提醒", NotificationManager.IMPORTANCE_HIGH)

        manager.createNotificationChannels(
            listOf<NotificationChannel>(
                mediaPlaybackChannel,
                alarmChannel
            )
        )
    }
}