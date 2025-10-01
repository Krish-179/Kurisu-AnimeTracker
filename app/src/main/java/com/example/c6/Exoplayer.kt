package com.example.c6

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

class VideoPlayerActivity : ComponentActivity() {
    private var lastProgress: Long = 0L
    private var duration: Long = 0L
    private var epNumber = 0
    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContent {
            val uri = intent.getParcelableExtra<Uri>("videoUri")
            val number = intent.getIntExtra("epNumber",0)
            epNumber = number
            val scope = rememberCoroutineScope()
            if (uri != null) {
                ExoPlayerView(context = this, uri = uri,scope) { progress ->
                    lastProgress = progress
                }
            }
        }
    }

    @UnstableApi
    override fun onPause() {
        super.onPause()
        if ((lastProgress.toDouble()/duration.toDouble())*100 >= 90 ){
            returnResult(true)
        }
        else{
            returnResult(false)
        }
    }

    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        val watched = duration > 0 && (lastProgress.toDouble() / duration.toDouble()) * 100 >= 90
        setResult(Activity.RESULT_OK, Intent().apply { putExtra("Update", watched) })
        super.onBackPressed()  // finish activity
    }
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).hide(
            WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()
        )
        WindowInsetsControllerCompat(window,window.decorView).systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).show(
            WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()
        )
    }

    @UnstableApi
    private fun returnResult(watched: Boolean) {
        val resultIntent = Intent().apply {
            putExtra("Update", watched)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
    @UnstableApi
    @Composable
    fun ExoPlayerView(context: ComponentActivity,uri: Uri,scope: CoroutineScope, onProgressSaved: (Long) -> Unit){
        Box (
            Modifier.background(color = Color.Black)
        ){
            if (uri != null){
                val trackSelector = remember {
                    DefaultTrackSelector(context).apply {
                        parameters = buildUponParameters()
                            .setPreferredTextLanguage("en")
                            .setPreferredAudioLanguage("en")
                            .build()
                    }
                }
                val exoplayer = remember {
                    ExoPlayer.Builder(context)
                        .setTrackSelector(trackSelector)
                        .build().apply {
                            setMediaItem(MediaItem.fromUri(uri!!))
                            prepare()
                            playWhenReady = true
                        }
                }
                LaunchedEffect(exoplayer) {
                    while(true) {
                        delay(1000)
                        duration = exoplayer.duration
                        onProgressSaved(exoplayer.currentPosition)
                    }
                }
                AndroidView(
                    factory = {
                        PlayerView(context).apply {
                            if (exoplayer.isPlaying){
                                hideSystemUI()
                            }
                            this.player = exoplayer
                            useController = true
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                DisposableEffect(Unit) {
                    onDispose {
                        exoplayer.release()
                        showSystemUI()
                    }
                }
            }
        }
    }
}
