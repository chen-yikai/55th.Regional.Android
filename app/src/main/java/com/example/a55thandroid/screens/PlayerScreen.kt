package com.example.a55thandroid.screens

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontVariation.Settings
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.media3.common.audio.AudioManagerCompat.getStreamMaxVolume
import com.example.a55thandroid.LocaleNavController
import com.example.a55thandroid.NetworkImage
import com.example.a55thandroid.services.PlaybackService
import com.example.a55thandroid.R
import com.example.a55thandroid.Screens
import com.example.a55thandroid.TitleText
import com.example.a55thandroid.api.fetchAlarm
import com.example.a55thandroid.api.schema.Alarm
import com.example.a55thandroid.api.toggleAlarm
import com.example.a55thandroid.services.durationFormatter
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlayerScreen() {
    val nav = LocaleNavController.current
    val player by PlaybackService.playerState.collectAsState()

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
                    .padding(horizontal = 25.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))
                key(player.currentIndex) {
                    NetworkImage(player.metadata.artworkUri.toString())
                }
                Spacer(Modifier.height(20.dp))
                TitleText(player.metadata.title.toString())
                Text(player.metadata.artist.toString(), color = Color.Gray)
                Spacer(Modifier.height(11.dp))
                PlayerSeekController()
                Spacer(Modifier.height(20.dp))
                PlayerController()
                Spacer(Modifier.height(30.dp))
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
            maxVolume / audioManager.getStreamVolume(
                AudioManager.STREAM_MUSIC
            ).toFloat()
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
        Slider(modifier = Modifier.fillMaxWidth(), value = volume, onValueChange = {
            volume = it
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (it * maxVolume).toInt(), 0)
        }, valueRange = 0f..1f)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AlarmController() {
    var alarmList = remember { mutableStateListOf<Alarm>() }
    val scope = rememberCoroutineScope()
    var showPicker by remember { mutableStateOf(false) }
    var timePickerState = rememberTimePickerState()
    val player by PlaybackService.playerState.collectAsState()

    suspend fun getAlarmsList() {
        alarmList.clear()
        alarmList.addAll(fetchAlarm())
    }

    LaunchedEffect(Unit) {
        try {
            getAlarmsList()
        } catch (e: Exception) {
            Log.e("PlayerAlarmScreen", "Error fetching alarm list", e)
        }
    }

    if (showPicker)
        Dialog(onDismissRequest = { showPicker = false }) {
            Column(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(20.dp)
            ) {
                TimePicker(state = timePickerState)
                Spacer(Modifier.height(10.dp))

            }
        }

    Column {
        AnimatedVisibility(alarmList.isNotEmpty()) {
            Card {
                Column(
                    Modifier
                        .heightIn(max = 80.dp)
                        .padding(horizontal = 10.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("提醒通知")
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                }
                LazyColumn(Modifier) {
                    items(alarmList) { items ->
                        if (items.soundId == player.currentIndex + 1)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                modifier = Modifier
                                    .padding(bottom = 10.dp)
                                    .padding(horizontal = 10.dp)
                                    .clickable {
                                        showPicker = true
                                    }
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxHeight(),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(items.time(), fontSize = 20.sp)
                                    }
                                    Spacer(Modifier.weight(1f))
                                    Switch(checked = items.isActive(), onCheckedChange = {
                                        scope.launch {
                                            toggleAlarm(items.id)
                                            getAlarmsList()
                                        }
                                    })
                                }
                            }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerController() {
    val player by PlaybackService.playerState.collectAsState()

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
    val player by PlaybackService.playerState.collectAsState()

    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(durationFormatter(player.currentPosition))
            Text(durationFormatter(player.duration))
        }
        Spacer(Modifier.height(10.dp))
        Slider(player.currentPosition.toFloat(), onValueChange = {
            PlaybackService.seekTo(it)
        }, valueRange = 0f..player.getDuration())
    }
}