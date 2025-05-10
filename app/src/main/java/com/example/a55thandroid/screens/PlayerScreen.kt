package com.example.a55thandroid.screens

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.media3.common.audio.AudioManagerCompat.getStreamMaxVolume
import com.example.a55thandroid.LocaleNavController
import com.example.a55thandroid.NetworkImage
import com.example.a55thandroid.services.PlaybackService
import com.example.a55thandroid.R
import com.example.a55thandroid.Screens
import com.example.a55thandroid.TitleText
import com.example.a55thandroid.services.durationFormatter
import kotlin.math.roundToInt

@Composable
fun PlayerScreen() {
    val nav = LocaleNavController.current
    val player by PlaybackService.Companion.playerState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp)
            .padding(horizontal = 5.dp), verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    nav.navigate(Screens.Home.name)
                }) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "")
                }
                TitleText(player.metadata.title.toString())
            }
            Column(
                Modifier
                    .padding(horizontal = 25.dp, vertical = 20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                key(player.currentIndex) {
                    NetworkImage(player.metadata.artworkUri.toString())
                }
                Spacer(Modifier.height(20.dp))
                TitleText(player.metadata.title.toString())
                Text(player.metadata.artist.toString(), color = Color.Gray)
                Spacer(Modifier.height(30.dp))
                PlayerSeekController()
                Spacer(Modifier.height(30.dp))
                PlayerController()
                Spacer(Modifier.height(20.dp))
                AlarmController()
            }
        }
        SystemVolumeController()
    }
}


@Composable
fun SystemVolumeController() {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    var volume by remember {
        mutableFloatStateOf(
            maxVolume.toFloat() / audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
        )
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = painterResource(R.drawable.volumn), contentDescription = "")
        Spacer(Modifier.width(10.dp))
        Slider(
            value = volume,
            onValueChange = {
                volume = it
                val volumeLevel = (it * maxVolume).roundToInt()
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    volumeLevel,
                    AudioManager.FLAG_SHOW_UI
                )
            },
            valueRange = 0f..1f
        )
    }
}

@Composable
fun AlarmController() {
    Column {
        Row(Modifier.fillMaxWidth()) {
            Text("提醒通知")
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
fun PlayerController() {
    val player by PlaybackService.Companion.playerState.collectAsState()
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            PlaybackService.Companion.prev()
        }) {
            Icon(painter = painterResource(R.drawable.prev), contentDescription = null)
        }
        IconButton(
            onClick = {
                PlaybackService.Companion.toggle()
            },
            modifier = Modifier
                .border(2.dp, Color.Gray, CircleShape)
                .shadow(5.dp, CircleShape)
                .background(Color.White)
                .size(60.dp)
        ) {
            Icon(
                painter = painterResource(if (!player.isPlaying) R.drawable.play else R.drawable.pause),
                modifier = Modifier.size(40.dp),
                contentDescription = ""
            )
        }
        IconButton(onClick = {
            PlaybackService.Companion.next()
        }) {
            Icon(painter = painterResource(R.drawable.next), contentDescription = "")
        }
    }
}

@Composable
fun PlayerSeekController() {
    val player by PlaybackService.Companion.playerState.collectAsState()
    var position by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(position) {
        PlaybackService.Companion.seekTo(position)
    }

    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(durationFormatter(player.currentPosition))
            Text(durationFormatter(player.duration))
        }
        Spacer(Modifier.height(10.dp))
        Slider(player.currentPosition.toFloat(), onValueChange = {
            position = it
        }, valueRange = 0f..player.getDuration())
    }
}