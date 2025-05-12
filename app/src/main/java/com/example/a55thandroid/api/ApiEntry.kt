package com.example.a55thandroid.api

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.a55thandroid.api.schema.Alarm
import com.example.a55thandroid.api.schema.Sounds
import com.example.a55thandroid.services.App
import com.example.a55thandroid.services.updateLocalAlarm
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.jvm.java

const val host = "http://10.0.2.2:3000"
//const val host = "https://skills-music-api-v3.eliaschen.dev"
const val apiKey = "kitty-secret-key"

val client = OkHttpClient()
val gson = Gson()

suspend fun fetchMusicList(search: String = "", sort: String = ""): List<Sounds> {
    return withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("$host/sounds")
            .addHeader("X-API-Key", apiKey)
            .addHeader("filter", "date")
            .addHeader("search", search)
            .addHeader("sort", sort)
            .build()

        client.newCall(req).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("API call failed: ${response.code}")
            }

            val responseBody = response.body?.string()

            try {
                gson.fromJson(responseBody, object : TypeToken<List<Sounds>>() {}.type)
                    ?: emptyList()
            } catch (e: Exception) {
                Log.e("fetchMusicList", "JSON parsing error: ${e.message}")
                throw e
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
suspend fun fetchAlarm(alarmUpdate: Boolean = true): List<Alarm> {
    val context = App.getAppContext()

    return withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("$host/alarms")
            .addHeader("X-API-Key", apiKey)
            .build()

        try {
            client.newCall(req).execute().use { response ->
                val res: List<Alarm> = gson.fromJson(
                    response.body?.string(),
                    object : TypeToken<List<Alarm>>() {}.type
                )
                res
            }
        } catch (e: Exception) {
            Log.e("fetchAlarm", "Error fetching alarm list", e)
            return@withContext emptyList()
        } finally {
            if (alarmUpdate) updateLocalAlarm(context)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
suspend fun toggleAlarm(id: Int) {
    val context = App.getAppContext()
    withContext(Dispatchers.IO) {
        val req = Request.Builder().patch("".toRequestBody())
            .addHeader("X-API-Key", apiKey)
            .url("$host/alarms/${id}/toggle").build()
        try {
            client.newCall(req).execute()
        } catch (e: Exception) {
            Log.e("toggleAlarm", "Error fetching alarm list", e)
        } finally {
            updateLocalAlarm(context)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
suspend fun updateAlarm(
    id: Int,
    soundId: Int,
    soundName: String,
    alarmTime: String,
    isActive: Boolean
) {
    withContext(Dispatchers.IO) {
        val context = App.getAppContext()
        val now = alarmTime
        val json = gson.toJson(
            mapOf(
                "id" to id,
                "apiKey" to apiKey,
                "soundId" to soundId,
                "soundName" to soundName,
                "alarmTime" to now,
                "isActive" to isActive,
                "createdAt" to now,
                "updatedAt" to now
            )
        )
//        Log.i("updateAlarm", "json: $json")
        val req = Request.Builder()
            .url("$host/alarms/$id")
            .put(json.toRequestBody())
            .addHeader("X-API-Key", apiKey)
            .build()

        try {
            client.newCall(req).execute()
        } catch (e: Exception) {
            Log.e("updateAlarm", "Error fetching alarm list", e)
        } finally {
            updateLocalAlarm(context)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
suspend fun deleteAlarm(id: Int) {
    val context = App.getAppContext()
    withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .delete()
            .url("$host/alarms/$id")
            .addHeader("X-API-Key", apiKey)
            .build()
        try {
            client.newCall(req).execute()
        } catch (e: Exception) {
            Log.e("deleteAlarm", "Error fetching alarm list", e)
        } finally {
            updateLocalAlarm(context)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
suspend fun createAlarm(soundId: Int, soundName: String, alarmTime: String) {
    val context = App.getAppContext()
    withContext(Dispatchers.IO) {
        val json = gson.toJson(
            mapOf(
                "soundId" to soundId,
                "soundName" to soundName,
                "alarmTime" to alarmTime,
            )
        )

        val req = Request.Builder()
            .post(json.toRequestBody())
            .addHeader("X-API-Key", apiKey)
            .url("$host/alarms")
            .build()

        try {
            client.newCall(req).execute()
        } catch (e: Exception) {
            Log.e("createAlarm", "Error fetching alarm list", e)
        } finally {
            updateLocalAlarm(context)
        }
    }
}