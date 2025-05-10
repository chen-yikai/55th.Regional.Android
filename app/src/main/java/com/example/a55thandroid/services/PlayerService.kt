package com.example.a55thandroid.services

import android.R
import android.app.Notification
import android.content.ComponentName
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionToken
import com.example.a55thandroid.api.fetchMusicList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.example.a55thandroid.api.host
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

data class PlayerState(
    var currentPosition: Long = 0L,
    var duration: Long = 1000L,
    var isPlaying: Boolean = false,
    var volume: Float = 1.0f,
    var currentIndex: Int = 0,
    var repeatMode: Int = Player.REPEAT_MODE_OFF,
    var metadata: MediaMetadata = MediaMetadata.Builder().build(),
    var isStarted: Boolean = false,
) {
    fun getDuration(): Float = if (duration > 0) duration.toFloat() else 0f
}

fun durationFormatter(pos: Long): String {
    return "${(pos / 1000 / 60).toString().padStart(2, '0')}:${
        (pos / 1000 % 60).toString().padStart(2, '0')
    }"
}

class PlaybackService : MediaSessionService() {
    private var isForegroundStarted = false
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var controllerJob: Job? = null
    private var pos: Job? = null

    companion object {
        private val _playerState = MutableStateFlow(PlayerState())
        val playerState: StateFlow<PlayerState> = _playerState
        var playerInstance: ExoPlayer? = null

        fun setIndex(index: Int) {
            playerInstance?.apply {
                if (playerInstance?.currentMediaItemIndex != index) {
                    seekTo(index, 0L)
                }
                prepare()
                play()
            }
        }

        fun playMediaById(mediaId: String) {
            playerInstance?.let { player ->
                val index = player.mediaItemCount
                    .takeIf { it > 0 }
                    ?.let { count ->
                        (0 until count).find { player.getMediaItemAt(it).mediaId == mediaId }
                    }
                if (index != null) {
                    player.seekTo(index, 0)
                    player.prepare()
                    player.play()
                } else {
                    Log.i("Playback Service", "No Media id found")
                }
            }
        }

        fun seekTo(position: Float) {
            playerInstance?.seekTo(position.toLong())
        }

        fun toggle() =
            if (_playerState.value.isPlaying) playerInstance?.pause() else playerInstance?.play()

        fun next() = playerInstance?.seekToNextMediaItem()

        fun prev() = playerInstance?.seekToPreviousMediaItem()
    }

    private fun initSounds() {
        serviceScope.launch {
            fetchMusicList().forEach {
                player.addMediaItem(
                    MediaItem.Builder().setUri(host + it.audio.url)
                        .setMediaId(it.id.toString()).setMediaMetadata(
                            MediaMetadata.Builder().setTitle(it.name)
                                .setArtist(it.metadata.author)
                                .setArtworkUri((host + it.cover.url).toUri())
                                .build()
                        ).build()
                )
            }
        }
        player.prepare()
        player.play()
    }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        player.repeatMode = Player.REPEAT_MODE_ALL
        initSounds()
        playerInstance = player
        player.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                _playerState.value = _playerState.value.copy(
                    currentIndex = player.currentMediaItemIndex,
                    isPlaying = player.isPlaying,
                    duration = player.duration,
                    volume = player.volume,
                    repeatMode = player.repeatMode,
                    metadata = player.mediaMetadata
                )
                updateCurrentPosition()
                super.onEvents(player, events)
            }
        })
        mediaSession = MediaSession.Builder(this, player).build()
        startControllerConnection()
    }

    private fun updateCurrentPosition() {
        pos?.cancel()
        pos = CoroutineScope(Dispatchers.Main).launch {
            while (player.isPlaying) {
                _playerState.value = _playerState.value.copy(
                    currentPosition = player.currentPosition
                )
                delay(500)
            }
        }
    }

    private fun startControllerConnection() {
        controllerJob?.cancel()
        controllerJob = CoroutineScope(Dispatchers.Main).launch {
            val sessionToken = SessionToken(
                this@PlaybackService,
                ComponentName(this@PlaybackService, PlaybackService::class.java)
            )
            MediaController.Builder(this@PlaybackService, sessionToken).buildAsync()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (!isForegroundStarted) {
            val notification = createNotification()
            startForeground(1, notification)
            isForegroundStarted = true
        }
        return START_STICKY
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        playerInstance = null
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "media_channel")
            .setSmallIcon(R.drawable.ic_media_play)
            .setContentTitle("title")
            .setContentText("text")
            .setOngoing(true)
            .build()
    }
}