package com.example.a55thandroid.api.schema

import android.os.Build
import androidx.annotation.RequiresApi
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun time(): String {
        val zonedDateTime = ZonedDateTime.parse(alarmTime)
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")

        return zonedDateTime.format(formatter)
    }
}