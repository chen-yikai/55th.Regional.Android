package com.example.a55thandroid

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.a55thandroid.api.fetchMusicList
import com.example.a55thandroid.api.host
import com.example.a55thandroid.services.PlaybackService
import com.example.a55thandroid.ui.theme._55thAndroidTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _55thAndroidTheme {
                EntryComposable()
            }
        }
    }
}