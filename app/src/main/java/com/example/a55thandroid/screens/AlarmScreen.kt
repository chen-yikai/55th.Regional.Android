package com.example.a55thandroid.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a55thandroid.TitleText
import com.example.a55thandroid.api.fetchAlarm
import com.example.a55thandroid.api.schema.Alarm
import com.example.a55thandroid.api.toggleAlarm
import kotlinx.coroutines.launch

data class Nav(
    val name: String,
    val route: String
)


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AlarmScreen() {
    var alarmList = remember { mutableStateListOf<Alarm>() }
    var alarmLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    suspend fun getAlarmsList() {
        alarmList.clear()
        alarmList.addAll(fetchAlarm())
    }

    LaunchedEffect(Unit) {
        try {
            getAlarmsList()
        } catch (e: Exception) {
            Log.e("AlarmScreen", "Error fetching alarm list", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 15.dp)
            .padding(horizontal = 15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TitleText("提醒")
        }
        Spacer(Modifier.height(20.dp))
        if (alarmList.isEmpty() && !alarmLoading) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text("No Alarm", modifier = Modifier.align(Alignment.Center))
            }
        }
        if (alarmList.isEmpty() && alarmLoading) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
        LazyColumn {
            items(alarmList) { items ->
                Card(
                    modifier = Modifier
                        .padding(vertical = 5.dp)
                        .height(70.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(items.time(), fontSize = 25.sp)
                            Text(items.soundName, color = Color.Gray, fontSize = 15.sp)
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