package com.example.a55thandroid

import android.content.Intent
import android.os.Build
import androidx.compose.runtime.getValue
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.a55thandroid.screens.AlarmScreen
import com.example.a55thandroid.screens.HomeScreen
import com.example.a55thandroid.screens.PlayerScreen
import com.example.a55thandroid.services.AlarmNotificationExtra
import com.example.a55thandroid.services.PlaybackService

val LocalNavController = compositionLocalOf<NavController> { error("LocalNavController error") }
val LocalIntent = compositionLocalOf<Intent> { error("LocalIntentSoundId error") }

data class NavScreen(val route: String, val label: String, val icon: Int)

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun EntryComposable(intent: Intent) {
    val navController = rememberNavController()
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry.value?.destination?.route

    val routes = listOf(
        NavScreen(Screens.Home.name, "環境音效", R.drawable.music),
        NavScreen(Screens.Alarm.name, "提醒", R.drawable.alarm)
    )

    CompositionLocalProvider(
        LocalNavController provides navController,
        LocalIntent provides intent
    ) {
        val player by PlaybackService.playerState.collectAsState()

        LaunchedEffect(player.ready) {
            if (player.ready) {
                val id = intent.getIntExtra(AlarmNotificationExtra.SoundId.name, -1)
                Log.i("EntryComposable", "intent: $id")
                if (id != -1) {
                    PlaybackService.setIndex(id - 1)
                    navController.navigate(Screens.Player.name)
                }
            }
        }
        Scaffold(bottomBar = {
            if (currentRoute != Screens.Player.name)
                NavigationBar {
                    routes.forEach {
                        NavigationBarItem(
                            selected = it.route == currentRoute,
                            onClick = { navController.navigate(it.route) },
                            icon = {
                                Icon(
                                    painter = painterResource(it.icon),
                                    contentDescription = it.label
                                )
                            },
                            label = { Text(it.label) })
                    }
                }
        }) { scaffoldPadding ->
            Column(modifier = Modifier.padding(scaffoldPadding)) {
                NavHost(navController, startDestination = Screens.Home.name) {
                    composable(Screens.Home.name) {
                        HomeScreen()
                    }
                    composable(Screens.Player.name) {
                        PlayerScreen()
                    }
                    composable(Screens.Alarm.name) {
                        AlarmScreen()
                    }
                }
            }
        }
    }
}

enum class Screens {
    Home, Player, Alarm
}
