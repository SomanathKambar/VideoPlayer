package com.somanath.videoplayer.ui.screens

import android.Manifest
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.somanath.videoplayer.media.VideoFile
import com.somanath.videoplayer.util.getAllVideos

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocalVideoPlayer(
    videoUri: String,
    modifier: Modifier = Modifier
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

    Box(modifier.fillMaxSize().then(gestureModifier)) {
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
            onSeekBackward = { player.seekBack() }, onDownloadClick = {

            }, onSpeedChange = {
                player.setPlaybackSpeed(it)
            }, onPipClick = {

            }
        )
    }
}

private fun createExoPlayer(context: Context, videoUri: String): ExoPlayer {
    val mediaItem = MediaItem.fromUri(Uri.parse(videoUri))

    return ExoPlayer.Builder(context).build().apply {
        setMediaItem(mediaItem)
        prepare()
        playWhenReady = true
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequestUI(permissionState: PermissionState) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Storage permission is required to access videos.")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { permissionState.launchPermissionRequest() }) {
            Text("Grant Permission")
        }
    }
}
