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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.a55thandroid.LocalNavController
import com.example.a55thandroid.NetworkImage
import com.example.a55thandroid.services.PlaybackService
import com.example.a55thandroid.R
import com.example.a55thandroid.Screens
import com.example.a55thandroid.TitleText
import com.example.a55thandroid.api.createAlarm
import com.example.a55thandroid.api.deleteAlarm
import com.example.a55thandroid.api.fetchAlarm
import com.example.a55thandroid.api.schema.Alarm
import com.example.a55thandroid.api.schema.hourAndMinuteToIso
import com.example.a55thandroid.api.toggleAlarm
import com.example.a55thandroid.api.updateAlarm
import com.example.a55thandroid.services.durationFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun PlayerScreen() {
    val nav = LocalNavController.current
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
                key(player.currentIndex) {
                    AlarmController()
                }
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
    // NOTE: current system music volume dividing by system max volume to get a float between 0f to 1f
    var volume by remember {
        mutableFloatStateOf(
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVolume
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

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlarmDialog(dismiss: () -> Unit, update: () -> Unit) {
    var timePickerState = rememberTimePickerState()
    val player by PlaybackService.playerState.collectAsState()
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = dismiss) {
        Column(
            Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp)
        ) {
            TimePicker(timePickerState)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = dismiss, modifier = Modifier.weight(1f)) {
                    Text("取消")
                }
                Spacer(Modifier.width(10.dp))
                Button(onClick = {
                    scope.launch {
                        createAlarm(
                            player.currentIndex + 1,
                            player.metadata.title.toString(),
                            hourAndMinuteToIso(timePickerState.hour, timePickerState.minute)
                        )
                    }
                    update()
                    dismiss()
                }, modifier = Modifier.weight(1f)) {
                    Text("確定")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun AlarmController() {
    var alarmList = remember { mutableStateListOf<Alarm>() }
    val scope = rememberCoroutineScope()
    var showPicker by remember { mutableStateOf(false) }
    var timePickerState = rememberTimePickerState()
    val player by PlaybackService.playerState.collectAsState()
    val context = LocalContext.current

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    suspend fun getAlarmsList() {
        delay(500L)
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


    Column {
        Card {
            var showCreateAlarmDialog by remember { mutableStateOf(false) }

            if (showCreateAlarmDialog) CreateAlarmDialog(
                { showCreateAlarmDialog = false },
                {
                    scope.launch {
                        getAlarmsList()
                    }
                })
            Column(
                Modifier
                    .padding(horizontal = 10.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("提醒通知")
                    IconButton(onClick = {
                        showCreateAlarmDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
            }

            AnimatedVisibility(alarmList.isNotEmpty()) {
                LazyColumn(Modifier.heightIn(max = 100.dp)) {
                    items(alarmList) { items ->
                        var checked by remember { mutableStateOf(items.isActive()) }
                        var updateId by remember { mutableIntStateOf(0) }

                        LaunchedEffect(checked) {
                            if (items.isActive() != checked) toggleAlarm(items.id)
                        }

                        if (showPicker && items.id == updateId)
                            Dialog(onDismissRequest = { showPicker = false }) {
                                Column(
                                    Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(20.dp)
                                ) {
                                    TimePicker(state = timePickerState)
                                    Spacer(Modifier.height(10.dp))
                                    Row(Modifier.fillMaxWidth()) {
                                        Button(onClick = {
                                            scope.launch {
                                                val date = LocalDateTime.of(2025, 5, 11, 0, 0)
                                                val formatedDate = hourAndMinuteToIso(
                                                    timePickerState.hour,
                                                    timePickerState.minute,
                                                    date
                                                )

                                                updateAlarm(
                                                    items.id,
                                                    items.soundId,
                                                    items.soundName,
                                                    formatedDate,
                                                    items.isActive()
                                                )
                                                getAlarmsList()
                                            }
                                            showPicker = false
                                        }, modifier = Modifier.weight(3f)) { Text("確定") }
                                        Spacer(Modifier.width(10.dp))
                                        FilledTonalButton(
                                            onClick = {
                                                scope.launch {
                                                    deleteAlarm(items.id)
                                                    getAlarmsList()
                                                }
                                                showPicker = false
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                painter = painterResource(
                                                    R.drawable.delete
                                                ), contentDescription = ""
                                            )
                                        }
                                    }
                                }
                            }
                        if (items.soundId == player.currentIndex + 1) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                modifier = Modifier
                                    .padding(bottom = 10.dp)
                                    .padding(horizontal = 10.dp)
                                    .clickable {
                                        showPicker = true
                                        updateId = items.id
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
                                    Switch(checked = checked, onCheckedChange = {
                                        checked = !checked
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
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
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .size(60.dp)
        ) {
            Icon(
                painter = painterResource(if (!player.isPlaying) R.drawable.play else R.drawable.pause),
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.secondary,
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

@Preview(showBackground = true)
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