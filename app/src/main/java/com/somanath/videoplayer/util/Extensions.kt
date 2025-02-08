package com.somanath.videoplayer.util

import android.content.Context
import android.provider.MediaStore
import com.somanath.videoplayer.media.VideoFile

fun Context.getAllVideos(): List<VideoFile> {
    val videoList = mutableListOf<VideoFile>()
    val projection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DATA
    )

    val cursor = contentResolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        projection, null, null, "${MediaStore.Video.Media.DATE_ADDED} DESC"
    )

    cursor?.use {
        val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val titleColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
        val pathColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

        while (it.moveToNext()) {
            val id = it.getLong(idColumn)
            val title = it.getString(titleColumn)
            val path = it.getString(pathColumn)

            videoList.add(VideoFile(id, title, path))
        }
    }

    return videoList
}
