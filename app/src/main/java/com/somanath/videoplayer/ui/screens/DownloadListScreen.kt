package com.somanath.videoplayer.ui.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import com.somanath.videoplayer.media.VideoDownloadManager

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadListScreen(downloadManager: VideoDownloadManager, onVideoClick: (String) -> Unit) {
    val context = LocalContext.current
    val downloads = remember { mutableStateListOf<Download>() }

    LaunchedEffect(Unit) {
        downloads.clear()
        downloads.addAll(downloadManager.getCurrentDownloads())
    }

    Scaffold (
        topBar = { TopAppBar(title = { Text("Downloaded Videos") }) }
    ) { innerPAdding ->
        LazyColumn(modifier = Modifier.padding(innerPAdding)) {
            items(downloads) { download ->
                DownloadItem(download, context, downloadManager, onVideoClick)
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun DownloadItem(download: Download, context: Context, downloadManager: VideoDownloadManager, onVideoClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onVideoClick(download.request.id) },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Video: ${download.request.id}")
            Text(text = "State: ${download.state}")
            Row {
                Button(onClick = { downloadManager.removeDownload(download.request.id) }) {
                    Text("Delete")
                }
            }
        }
    }
}
