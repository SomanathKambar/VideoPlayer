package com.somanath.videoplayer.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.somanath.videoplayer.media.VideoFile
import com.somanath.videoplayer.util.getAllVideos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun VideoListScreen(navController: NavController) {
    val context = LocalContext.current
    val videoList = remember { mutableStateListOf<VideoFile>() }
    val permissionState = rememberPermissionState (
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_VIDEO
        else
            Manifest.permission.READ_EXTERNAL_STORAGE
    )

    LaunchedEffect(Unit) {
        if (permissionState.status.isGranted) {
            videoList.clear()
            videoList.addAll(context.getAllVideos())
        } else {
            permissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(Unit) {
        videoList.clear()
        videoList.addAll(context.getAllVideos())
    }
    if (permissionState.status.isGranted) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("All Videos") }) }
        ) { inner ->
            LazyColumn(modifier = Modifier.padding(inner).padding(16.dp)) {
                items(videoList) { video ->
                    VideoItem(video, navController)
                }
            }
        }
    }else {
        PermissionRequestUI(permissionState)
    }
}

@Composable
fun VideoItem(video: VideoFile, navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    var thumbnail by remember { mutableStateOf<Bitmap?>(null) }

    // Load thumbnail asynchronously
    LaunchedEffect(video.path) {
        coroutineScope.launch {
            thumbnail = getVideoThumbnail(video.path)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { navController.navigate("play/${Uri.encode(video.path)}") },
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            // Show thumbnail if available
            if (thumbnail != null) {
                Image(
                    bitmap = thumbnail!!.asImageBitmap(),
                    contentDescription = "Video Thumbnail",
                    modifier = Modifier.size(100.dp)
                )
            } else {
                // Show placeholder while loading
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(text = video.title, style = MaterialTheme.typography.headlineMedium)
                Text(text = video.path, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}


fun getThumbnailUri(videoId: String): Uri {
    ThumbnailUtils.createVideoThumbnail(videoId, MediaStore.Images.Thumbnails.MICRO_KIND)
    return Uri.withAppendedPath(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, videoId)
}


suspend fun getVideoThumbnail(videoPath: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MINI_KIND)
    }
}