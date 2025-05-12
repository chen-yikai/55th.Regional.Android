package com.example.a55thandroid.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.a55thandroid.R
import com.example.a55thandroid.services.PlaybackService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class Glance : GlanceAppWidget() {
    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("RestrictedApi")
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val player by PlaybackService.playerState.collectAsState()
            var bitmap by remember { mutableStateOf<Bitmap?>(null) }

            LaunchedEffect(Unit, player.isStarted, player.currentIndex) {
                bitmap = null
                if (player.currentIndex != -1) withContext(Dispatchers.IO) {
                    try {
                        bitmap = URL(player.metadata.artworkUri.toString()).openStream().use {
                            BitmapFactory.decodeStream(it)
                        }
                    } catch (e: Exception) {
                        Log.i("Glance Image", "error: $e")
                    }
                }
            }
            Column(
                modifier = GlanceModifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondaryContainer),
            ) {
                if (!player.isStarted) {
                    Row(
                        GlanceModifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("No Sound Currently is Playing")
                    }
                } else {
                    Row(
                        modifier = GlanceModifier.fillMaxHeight().padding(end = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (bitmap == null) {
                            Row(
                                GlanceModifier.size(120.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(GlanceModifier)
                            }
                        }
                        bitmap?.let {
                            Image(
                                provider = ImageProvider(bitmap = it),
                                contentDescription = "",
                                modifier = GlanceModifier.size(120.dp).padding(end = 10.dp)
                            )
                        }
                        Column {
                            Column() {
                                Text(
                                    player.metadata.title.toString(),
                                    style = TextStyle(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    player.metadata.artist.toString(),
                                    style = TextStyle(color = ColorProvider(Color.Gray))
                                )
                            }
                            Spacer(GlanceModifier.height(5.dp))
                            Row(
                                GlanceModifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalAlignment = Alignment.End
                            ) {
                                CircleIconButton(
                                    imageProvider = ImageProvider(resId = R.drawable.prev),
                                    backgroundColor = ColorProvider(MaterialTheme.colorScheme.secondaryContainer),
                                    contentDescription = "",
                                    onClick = { PlaybackService.prev() },
                                )
                                Spacer(GlanceModifier.defaultWeight())
                                CircleIconButton(
                                    imageProvider = ImageProvider(
                                        if (player.isPlaying) R.drawable.pause else R.drawable.play
                                    ),
                                    contentDescription = "",
                                    onClick = { PlaybackService.toggle() },
                                )
                                Spacer(GlanceModifier.defaultWeight())
                                CircleIconButton(
                                    imageProvider = ImageProvider(resId = R.drawable.next),
                                    backgroundColor = ColorProvider(MaterialTheme.colorScheme.secondaryContainer),
                                    contentDescription = "",
                                    onClick = { PlaybackService.next() },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}