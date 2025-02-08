package com.somanath.videoplayer.ui.screens


import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PiPButton(context: Context, onClick: () -> Unit) {
    IconButton(onClick = {
        onClick()
    }) {
        Icon(Icons.Default.ExitToApp, contentDescription = "PiP Mode")
    }
}
