package com.somanath.videoplayer.media

import android.content.Context
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DefaultDownloadIndex
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import java.io.File
import java.util.concurrent.Executors

@UnstableApi
class VideoDownloadManager(private val context: Context) {
    private val downloadDirectory: File =
        File(context.getExternalFilesDir(null), "downloads").apply { mkdirs() }

    private val databaseProvider = StandaloneDatabaseProvider(context)
    private val cache = SimpleCache(downloadDirectory, NoOpCacheEvictor(), databaseProvider)

    private val dataSourceFactory = DefaultHttpDataSource.Factory()
    private val cacheDataSourceFactory = CacheDataSource.Factory()
        .setCache(cache)
        .setUpstreamDataSourceFactory(dataSourceFactory)

    private val executorService = Executors.newFixedThreadPool(2) // ✅ Background thread for downloads
    private val downloadIndex = DefaultDownloadIndex(databaseProvider)

    private val downloadManager = DownloadManager(
        context,
        databaseProvider,
        cache,
        dataSourceFactory,
        executorService
    )

    fun downloadVideo(videoUrl: String, licenseUrl: String? = null) {
        val mediaItemBuilder = MediaItem.Builder().setUri(videoUrl)

        // ✅ Add DRM configuration if a license URL is provided
        if (!licenseUrl.isNullOrEmpty()) {
            mediaItemBuilder.setDrmConfiguration(
                MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                    .setLicenseUri(licenseUrl)
                    .build()
            )
        }

        val mediaItem = mediaItemBuilder.build()
        val downloadRequest = DownloadRequest.Builder("1", Uri.parse(videoUrl)).build()

        downloadManager.addDownload(downloadRequest)
    }

    fun getCurrentDownloads() = downloadManager.currentDownloads
    fun removeDownload(id: String) {
        downloadManager.removeDownload(id)
    }
}
