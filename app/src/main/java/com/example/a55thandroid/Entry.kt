package com.example.a55thandroid

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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

val LocaleNavController = compositionLocalOf<NavController> { error("error") }

data class NavScreen(val route: String, val label: String, val icon: Int)

@Composable
fun EntryComposable() {
    val navController = rememberNavController()
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry.value?.destination?.route

    val routes = listOf(
        NavScreen(Screens.Home.name, "環境音效", R.drawable.music),
        NavScreen(Screens.Alarm.name, "提醒", R.drawable.alarm)
    )

    CompositionLocalProvider(LocaleNavController provides navController) {
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
