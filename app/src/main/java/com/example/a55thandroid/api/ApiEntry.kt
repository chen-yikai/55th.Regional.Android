package com.example.a55thandroid.api

import android.util.Log
import com.example.a55thandroid.api.schema.Alarm
import com.example.a55thandroid.api.schema.Sounds
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

suspend fun fetchMusicList(search: String = "", sort: String = ""): List<Sounds> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val gson = Gson()
        val req = Request.Builder()
            .url("$host/sounds")
            .addHeader("X-API-Key", apiKey)
            .addHeader("filter","date")
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

suspend fun fetchAlarm(): List<Alarm> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val gson = Gson()
        val req = Request.Builder()
            .url("$host/alarms")
            .addHeader("X-API-Key", apiKey)
            .build()

        client.newCall(req).execute().use { response ->
            gson.fromJson(response.body?.string(), object : TypeToken<List<Alarm>>() {}.type)
                ?: emptyList()
        }
    }
}

suspend fun toggleAlarm(id: Int) {
    withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val req = Request.Builder().patch("".toRequestBody())
            .addHeader("X-API-Key", apiKey)
            .url("$host/alarms/${id}/toggle").build()
        client.newCall(req).execute()
    }
}