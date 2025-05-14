package com.example.a55thandroid.api.schema

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class Alarm(
    val id: Int,
    val soundId: Int,
    val soundName: String,
    val alarmTime: String,
    val isActive: Int,
) {
    fun isActive() = isActive == 1

    @SuppressLint("DefaultLocale")
    @RequiresApi(Build.VERSION_CODES.O)
    fun time(): String {
        fun isoToHourAndSecond(isoTime: String): Pair<Int, Int> {
            val zonedDateTime = ZonedDateTime.parse(isoTime, DateTimeFormatter.ISO_ZONED_DATE_TIME)
            return Pair(zonedDateTime.hour, zonedDateTime.minute)
        }

        val (hour, minutes) = isoToHourAndSecond(alarmTime)
        val amPm = if (hour < 12) "AM" else "PM"
        val hour12 = if (hour > 12) hour - 12 else hour
        return String.format("%02d:%02d %s", hour12, minutes, amPm)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun hourAndMinuteToIso(hour: Int, minute: Int, date: LocalDateTime = LocalDateTime.now()): String {
    require(hour in 0..23) { "Hour must be between 0 and 23" }
    require(minute in 0..59) { "Minute must be between 0 and 59" }

    val dateTime = date.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
    val zonedDateTime = dateTime.atZone(ZoneId.of("UTC"))
    Log.i(
        "Alarm Change Time",
        "hourAndMinuteToIso: ${zonedDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)}"
    )
    return zonedDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
}
