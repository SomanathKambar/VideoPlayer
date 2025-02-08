package com.somanath.videoplayer


import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.somanath.videoplayer.media.VideoDownloadManager
import com.somanath.videoplayer.ui.screens.DownloadListScreen
import com.somanath.videoplayer.ui.screens.LocalVideoPlayer
import com.somanath.videoplayer.ui.screens.VideoListScreen
import com.somanath.videoplayer.ui.screens.VideoPlayer
import java.net.URLDecoder

class MainActivity : ComponentActivity() {
    @UnstableApi
    var videoDownloadManager :  VideoDownloadManager?= null

    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        videoDownloadManager =  VideoDownloadManager(this)
        setContent {
            val navController = rememberNavController()
            val context = LocalContext.current
            val videoUrl = "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd"
            val licenseUrl = "https://proxy.uat.widevine.com/proxy?provider=widevine_test"


            NavHost(navController, startDestination = "video_list") {
//                val context = LocalContext.current
//                val databaseProvider = StandaloneDatabaseProvider(context)
//                val cache = SimpleCache(context.cacheDir, NoOpCacheEvictor(), databaseProvider)
//                val downloadManager = DownloadManager(
//                    context, databaseProvider, cache, DefaultDataSource.Factory(context),
//                )
                composable ("video") {
                    Column {
                        Button(onClick = { navController.navigate("downloads") }) {
                            Text("View Downloads", color = Color.Black)
                        }
                        VideoScreen(Modifier, onPipClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                handlePip()
                            }
                        }, onDownloadClick = {
                            handleVideoDownload(it)
                        })
                    }
                }
                composable("downloads") {
                    DownloadListScreen(videoDownloadManager!!) { videoId ->
                        navController.navigate("play/$videoId")
                    }
                }
                composable("play/{videoId}") { backStackEntry ->
                    val videoId = backStackEntry.arguments?.getString("videoId") ?: ""
//                    VideoScreen(Modifier, onPipClick = {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            handlePip()
//                        }
//                    }, onDownloadClick = {
//                        handleVideoDownload(it)
//                    })
                    LocalVideoPlayer(videoUri = "file://${context.filesDir}/downloads/$videoId")
                }

                composable("video_list") { VideoListScreen(navController) }
                composable("play/{videoPath}") { backStackEntry ->
                    val videoPath = URLDecoder.decode(backStackEntry.arguments?.getString("videoPath"), "UTF-8")

                    LocalVideoPlayer(videoPath)
                }
            }
        }

//        setContent {
//            VideoPlayerTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    VideoScreen(Modifier.padding(innerPadding), onPipClick = {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            handlePip()
//                        }
//                    }, onDownloadClick = {
//                        handleVideoDownload(it)
//                    })
//                }
//            }
//        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun handlePip() {
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .build()
        enterPictureInPictureMode(params)
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    fun handleVideoDownload(videoUrl:String, licenceUrl:String = "") {
        videoDownloadManager?.downloadVideo(videoUrl, licenceUrl)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(modifier: Modifier,
                onPipClick:()->Unit,
                onDownloadClick:(String)->Unit) {
    val videoUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8" // HLS Test Stream

    Scaffold( modifier = modifier,
        topBar = { TopAppBar(title = { Text("Video Player") }) }, bottomBar = {

        }
    ) { innerPAdding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPAdding)) {
            VideoPlayer(videoUri = videoUrl, modifier = Modifier.weight(1f),
                onPipClick = onPipClick, onDownloadClick)
        }
    }
}