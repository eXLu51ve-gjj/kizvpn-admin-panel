package com.kizvpn.admin.ui.screens

import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.kizvpn.admin.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    val context = LocalContext.current
    var isCompleted by remember { mutableStateOf(false) }
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            try {
                val videoResId = R.raw.valve
                val videoUri = Uri.parse("android.resource://${context.packageName}/$videoResId")
                val mediaItem = MediaItem.fromUri(videoUri)
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_OFF
            } catch (e: android.content.res.Resources.NotFoundException) {
                android.util.Log.w("SplashScreen", "Видео файл valve.mp4 не найден в res/raw/", e)
                // Продолжаем без видео
            } catch (e: Exception) {
                android.util.Log.e("SplashScreen", "Ошибка загрузки видео", e)
                // Не падаем, просто логируем
            }
        }
    }
    
    // MediaPlayer для воспроизведения звука
    val mediaPlayer = remember {
        try {
            val soundResId = R.raw.valve_intro_sound
            MediaPlayer.create(context, soundResId)?.apply {
                isLooping = false
            }
        } catch (e: android.content.res.Resources.NotFoundException) {
            android.util.Log.w("SplashScreen", "Звук файл valve_intro_sound.mp3 не найден в res/raw/", e)
            null
        } catch (e: Exception) {
            android.util.Log.e("SplashScreen", "Ошибка загрузки звука", e)
            null
        }
    }

    // Функция для завершения сплэша
    val completeSplash = {
        if (!isCompleted) {
            isCompleted = true
            exoPlayer.stop()
            try {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        it.stop()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("SplashScreen", "Ошибка остановки звука", e)
            }
            onSplashComplete()
        }
    }

    // Запускаем звук одновременно с видео
    LaunchedEffect(Unit) {
        try {
            mediaPlayer?.let {
                it.start()
                android.util.Log.d("SplashScreen", "Звук запущен")
            } ?: run {
                android.util.Log.w("SplashScreen", "MediaPlayer не инициализирован")
            }
        } catch (e: Exception) {
            android.util.Log.e("SplashScreen", "Ошибка запуска звука", e)
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    completeSplash()
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            if (!isCompleted) {
                exoPlayer.release()
            }
            try {
                if (mediaPlayer != null) {
                    mediaPlayer.release()
                }
            } catch (e: Exception) {
                android.util.Log.e("SplashScreen", "Ошибка освобождения MediaPlayer", e)
            }
        }
    }

    // Таймер на 13 секунд максимум
    LaunchedEffect(Unit) {
        try {
            delay(13000)
            if (!isCompleted && exoPlayer.playbackState != Player.STATE_ENDED) {
                completeSplash()
            }
        } catch (e: Exception) {
            android.util.Log.e("SplashScreen", "Ошибка в таймере", e)
            // В случае ошибки завершаем сплэш
            if (!isCompleted) {
                completeSplash()
            }
        }
    }

    // Черный фон на весь экран
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(onClick = { completeSplash() }), // Тап для пропуска
        contentAlignment = Alignment.Center
    ) {
        // Видео плеер с сохранением соотношения сторон и центрированием
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // Скрыть элементы управления
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT // Сохранить соотношение сторон
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )
    }
}

