package com.example.a55thandroid.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a55thandroid.LocalNavController
import com.example.a55thandroid.services.PlaybackService
import com.example.a55thandroid.R
import com.example.a55thandroid.Screens
import com.example.a55thandroid.TitleText
import com.example.a55thandroid.api.fetchMusicList
import com.example.a55thandroid.api.schema.Sounds
import kotlinx.coroutines.launch

@Preview(showBackground = true)
@Composable
fun HomeScreen() {
    var showSortMenu by remember { mutableStateOf(false) }
    val player by PlaybackService.Companion.playerState.collectAsState()
    val nav = LocalNavController.current
    val scope = rememberCoroutineScope()
    var searchTextField by remember { mutableStateOf("") }
    val soundList = remember { mutableStateListOf<Sounds>() }
    var soundLoading by remember { mutableStateOf(false) }

    fun updateSound(sort: String = "", search: String = "") {
        soundLoading = true
        soundList.clear()
        scope.launch {
            try {
                val response = fetchMusicList(sort = sort)
                soundList.addAll(response)
            } catch (e: Exception) {
                Log.e("HomeScreen", "Error fetching music list", e)
            } finally {
                soundLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        updateSound()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp)
            .padding(horizontal = 15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TitleText("環境音效")
            Box {
                IconButton(onClick = {
                    showSortMenu = !showSortMenu
                }) {
                    Icon(painter = painterResource(R.drawable.filter), contentDescription = "")
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("升序") },
                        onClick = { updateSound(sort = "asc") })
                    DropdownMenuItem(
                        text = { Text("降序") },
                        onClick = { updateSound(sort = "desc") })
                }
            }
        }
        OutlinedTextField(
            value = searchTextField,
            onValueChange = { searchTextField = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            placeholder = { Text("搜尋") },
            trailingIcon = {
                IconButton(onClick = {
                    soundList.clear()
                    scope.launch {
                        try {
                            val response = fetchMusicList(searchTextField)
                            soundList.addAll(response)
                        } catch (e: Exception) {
                        }
                    }
                }) {
                    Icon(painter = painterResource(R.drawable.search), contentDescription = "")
                }
            })

        if (soundList.isEmpty() && soundLoading) {
            Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
        if (soundList.isEmpty() && !soundLoading) {
            Box(Modifier.fillMaxSize()) {
                Text("No Sound", modifier = Modifier.align(Alignment.Center))
            }
        }
        LazyColumn {
            itemsIndexed(soundList) { index, item ->
                val id = item.id - 1
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .height(70.dp)
                        .clickable {
                            PlaybackService.setIndex(id)
                            nav.navigate(Screens.Player.name)
                        },
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    ) {
                        Row(Modifier.align(Alignment.CenterStart)) {
                            Icon(
                                painter = painterResource(if (player.currentIndex == id && player.isPlaying) R.drawable.pause else R.drawable.play),
                                contentDescription = ""
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(item.name, fontSize = 20.sp)
                        }
                        Row(Modifier.align(Alignment.BottomEnd)) {
                            Icon(
                                painter = painterResource(R.drawable.date),
                                contentDescription = ""
                            )
                            Spacer(Modifier.width(5.dp))
                            item.metadata.publishDate.let { text ->
                                Text(text.replace("-", "."))
                            }
                        }
                    }
                }
            }
        }
    }
}