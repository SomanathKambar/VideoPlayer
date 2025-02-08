package com.somanath.videoplayer.ui.screens

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun VideoPlayer(
    videoUri: String,
    modifier: Modifier = Modifier,
    onPipClick: () -> Unit,
    onDownloadClick: (String) -> Unit
) {
    val context = LocalContext.current
    val player = remember { createExoPlayer(context, videoUri) }
    var isPlaying by remember { mutableStateOf(player.isPlaying) }
    var volumeLevel by remember { mutableStateOf(0f) }
    var brightnessLevel by remember { mutableStateOf(0.5f) }
    var showVolumeOverlay by remember { mutableStateOf(false) }
    var showBrightnessOverlay by remember { mutableStateOf(false) }

    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // Detect gestures
    val gestureModifier = Modifier.pointerInput(Unit) {
        detectVerticalDragGestures(
            onVerticalDrag = { change, dragAmount ->
                change.consume()
                if (change.position.x < 300) {
                    // Adjust brightness (left side of the screen)
                    brightnessLevel = (brightnessLevel - dragAmount / 2000f).coerceIn(0f, 1f)
                    setScreenBrightness(context, brightnessLevel)
                    showBrightnessOverlay = true
                } else {
                    // Adjust volume (right side of the screen)
                    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    volumeLevel = (volumeLevel - dragAmount / 1000f).coerceIn(0f, maxVolume.toFloat())
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeLevel.toInt(), 0)
                    showVolumeOverlay = true
                }
            },
            onDragEnd = {
                showVolumeOverlay = false
                showBrightnessOverlay = false
            }
        )
    }

    Box(
        modifier
            .fillMaxSize()
            .then(gestureModifier)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    useController = false // Hide default controls
                }
            }
        )

        // Custom Volume & Brightness Overlay
        if (showVolumeOverlay) {
            VolumeOverlay(volumeLevel)
        }
        if (showBrightnessOverlay) {
            BrightnessOverlay(brightnessLevel)
        }

        VideoControls(
            isPlaying = isPlaying,
            onPlayPauseToggle = {
                if (isPlaying) player.pause() else player.play()
                isPlaying = !isPlaying
            },
            onSeekForward = { player.seekForward() },
            onSeekBackward = { player.seekBack() },
            onSpeedChange = {player.setPlaybackSpeed(it)},
            onPipClick = onPipClick,
            onDownloadClick = {
                onDownloadClick(videoUri)
            }
        )
    }
}

@Composable
fun VolumeOverlay(volume: Float) {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Volume: ${(volume * 100).toInt()}%",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    RoundedCornerShape(10.dp)
                )
                .padding(16.dp)
        )
    }
}

@Composable
fun BrightnessOverlay(brightness: Float) {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Brightness: ${(brightness * 100).toInt()}%",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    RoundedCornerShape(10.dp)
                )
                .padding(16.dp)
        )
    }
}

fun setScreenBrightness(context: Context, brightness: Float) {
    Settings.System.putInt(
        context.contentResolver,
        Settings.System.SCREEN_BRIGHTNESS,
        (brightness * 255).toInt()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoControls(
    isPlaying: Boolean,
    onPlayPauseToggle: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onPipClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    var showSpeedDialog by remember { mutableStateOf(false) }
    val speeds = listOf(0.5f, 1.0f, 1.5f, 2.0f)

    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            Modifier
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    RoundedCornerShape(10.dp)
                )
                .padding(8.dp)
        ) {
            IconButton(onClick = onSeekBackward) {
                Icon(Icons.Default.Refresh, contentDescription = "Rewind")
            }
            IconButton(onClick = onPlayPauseToggle) {
                Icon(
                    if (isPlaying) Icons.Default.Face else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause"
                )
            }
            IconButton(onClick = onSeekForward) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Forward")
            }
            IconButton(onClick = { showSpeedDialog = true }) {
                Icon(Icons.Default.ShoppingCart, contentDescription = "Speed")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PiPButton(context = LocalContext.current, onPipClick)
            }

            IconButton(onClick = {onDownloadClick()}) {
                Icon(Icons.Default.Favorite, contentDescription = "Download")
            }
        }

        if (showSpeedDialog) {
            AlertDialog(
                onDismissRequest = { showSpeedDialog = false }, content = {
                    Column {
                        speeds.forEach { speed ->
                            Text(
                                text = "${speed}x",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSpeedChange(speed)
                                        showSpeedDialog = false
                                    }
                                    .padding(16.dp)
                            )
                        }
                    }
                },
            )
        }
    }
}


private fun createExoPlayer(context: Context, videoUri: String): ExoPlayer {
    return ExoPlayer.Builder(context).build().apply {
        setMediaItem(MediaItem.fromUri(Uri.parse(videoUri)))
        prepare()
        playWhenReady = true
    }
}
