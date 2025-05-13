package com.example.a55thandroid.services

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.a55thandroid.MainActivity
import com.example.a55thandroid.R
import com.example.a55thandroid.api.fetchAlarm
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

enum class AlarmExtra {
    AlarmId, SoundName, SoundId
}

enum class AlarmNotificationExtra {
    SoundId
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationId = intent?.getIntExtra(AlarmExtra.AlarmId.name, 0) ?: 0
        val soundId = intent?.getIntExtra(AlarmExtra.SoundId.name, 0) ?: 0
        val name = intent?.getStringExtra(AlarmExtra.SoundName.name) ?: ""

        postNotification(context!!, notificationId, soundId, name)
    }

    private fun postNotification(
        context: Context,
        notificationId: Int,
        soundId: Int,
        name: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(AlarmNotificationExtra.SoundId.name, soundId)
        }
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        val notification =
            NotificationCompat.Builder(context, alarm_channel)
                .setSmallIcon(R.drawable.alarm)
                .setContentTitle("播放聲景提醒")
                .setContentText("收聽 $name 聲景")
                .setContentIntent(pendingIntent)
                .build()

        notificationManager.notify(notificationId, notification)

        // repeat alarm next day
        setSoundAlarm(
            context,
            notificationId,
            soundId,
            name,
            System.currentTimeMillis() + AlarmManager.INTERVAL_DAY
        )
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
suspend fun updateLocalAlarm(context: Context) {
    cancelAllSoundAlarm(context)
    val alarmList = fetchAlarm(false)
    alarmList.forEach {
        val date = ZonedDateTime.parse(it.alarmTime, DateTimeFormatter.ISO_DATE_TIME).toLocalTime()
        val alarmTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, date.hour)
            set(Calendar.MINUTE, date.minute)
            set(Calendar.SECOND, 0)
        }.timeInMillis
        val nowMs = Calendar.getInstance().timeInMillis

        if (it.isActive())
            setSoundAlarm(
                context,
                it.id,
                it.soundId,
                it.soundName,
                if (alarmTime > nowMs) alarmTime else alarmTime + AlarmManager.INTERVAL_DAY
            )
    }
}

@SuppressLint("ScheduleExactAlarm")
fun setSoundAlarm(
    context: Context,
    notificationId: Int,
    soundId: Int,
    name: String,
    triggerTime: Long
) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra(AlarmExtra.AlarmId.name, notificationId)
        putExtra(AlarmExtra.SoundName.name, name)
        putExtra(AlarmExtra.SoundId.name, soundId)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        notificationId,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun cancelAllSoundAlarm(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    alarmManager.cancelAll()
}