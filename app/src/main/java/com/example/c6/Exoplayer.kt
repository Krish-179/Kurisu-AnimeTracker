package com.example.c6

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.io.File

class VideoPlayerActivity : ComponentActivity() {
    private var lastProgress: Long = 0L
    private var duration: Long = 0L
    private var epNumber = 0
    private lateinit var libVlc: LibVLC
    private lateinit var vlcPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideSystemUI()
        libVlc = LibVLC(this, arrayListOf("--no-drop-late-frames", "--no-skip-frames"))
        vlcPlayer = MediaPlayer(libVlc)

        setContent {
            val uri = intent.getParcelableExtra<Uri>("videoUri")
            val number = intent.getIntExtra("epNumber", 0)
            epNumber = number
            if (uri != null) {
                FullVlcPlayer(uri)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val watched = duration > 0 && (lastProgress.toDouble() / duration.toDouble()) * 100 >= 90
        returnResult(watched)
    }

    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        val watched = duration > 0 && (lastProgress.toDouble() / duration.toDouble()) * 100 >= 90
        setResult(RESULT_OK, Intent().apply { putExtra("Update", watched) })
        super.onBackPressed()
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).hide(
            WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()
        )
        WindowInsetsControllerCompat(window, window.decorView).systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).show(
            WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()
        )
    }

    private fun returnResult(watched: Boolean) {
        val resultIntent = Intent().apply {
            putExtra("Update", watched)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private suspend fun copyUriToCache(context: Context, uri: Uri): File =
        withContext(Dispatchers.IO){
            val input = context.contentResolver.openInputStream(uri)!!
            val tempFile = File(context.cacheDir, "temp_video.mp4")
            input.use { inputStream ->
                tempFile.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
            }
            tempFile
        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun FullVlcPlayer(uri: Uri) {
        val context = LocalActivity.current as ComponentActivity
        var isLoading by remember { mutableStateOf(true) }
        var showControls by remember { mutableStateOf(false) }
        var isPlaying by remember { mutableStateOf(false) }
        var showTracks by remember { mutableStateOf(false) }
        var sliderDuration by remember { mutableStateOf(0f) }
        var isSeeking by remember { mutableStateOf(false) }

        // Attach media
        LaunchedEffect(uri) {
            val file = copyUriToCache(context, uri)
            val media = Media(libVlc, file.absolutePath)
            vlcPlayer.media = media
            vlcPlayer.play()
            media.release()
            isLoading = false
            isPlaying = true
        }

        if (isLoading){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ){
                CircularProgressIndicator(color = Color.White)
            }
        }
        else{
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            showControls = !showControls
                            showTracks = false
                        }
                    ),
            ) {
                // Video Surface
                AndroidView(
                    factory = { ctx ->
                        VLCVideoLayout(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            vlcPlayer.attachViews(this, null, false, false)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Progress updater
                LaunchedEffect(vlcPlayer) {
                    while (true) {
                        delay(1500)
                        if (showControls && !isSeeking) showControls = false
                        duration = vlcPlayer.length
                        lastProgress = vlcPlayer.time
                        if (duration > 0){
                            sliderDuration = lastProgress.toFloat() / duration.toFloat()
                        }
                    }
                }

                DisposableEffect(Unit) {
                    onDispose {
                        vlcPlayer.stop()
                        vlcPlayer.detachViews()
                        vlcPlayer.release()
                        libVlc.release()
                        showSystemUI()
                    }
                }
            }
            if (showControls){
                Box (
                    modifier = Modifier.fillMaxSize()
                ){
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Transparent),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Slider(
                            value = sliderDuration,
                            onValueChange = { newValue ->
                                sliderDuration = newValue
                                val seekTo = (sliderDuration * duration).toLong()
                                vlcPlayer.time = seekTo
                                isSeeking = true
                            },
                            onValueChangeFinished = {
                                isSeeking = false
                            },
                            colors = SliderColors(
                                activeTrackColor = Color.Red.copy(alpha = 0.6f),
                                inactiveTrackColor = Color.White.copy(alpha = 0.6f),
                                activeTickColor = Color.Transparent,
                                inactiveTickColor = Color.Transparent,
                                thumbColor = Color.Red.copy(alpha = 0.6f),
                                disabledThumbColor = Color.Transparent,
                                disabledActiveTrackColor = Color.Transparent,
                                disabledActiveTickColor = Color.Transparent,
                                disabledInactiveTrackColor = Color.Transparent,
                                disabledInactiveTickColor = Color.Transparent,
                            ),
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            thumb = {
                                Box (
                                    modifier = Modifier.size(18.dp)
                                        .background(color = Color.Red.copy(alpha = 0.6f), shape = CircleShape)
                                )
                            },

                        )
                        Row (
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Start
                        ){
                            IconButton(
                                onClick = {
                                    showTracks = !showTracks
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Subtitles,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(340.dp))
                            if (isPlaying){
                                IconButton(
                                   onClick = {
                                       vlcPlayer.pause()
                                       isPlaying = false
                                   }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PauseCircleOutline,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            }
                            else{
                                IconButton(
                                    onClick = {
                                        vlcPlayer.play()
                                        isPlaying = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircleOutline,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (showTracks){
                showControls = false
                displayTracks(vlcPlayer)
            }
        }
    }
}

@Composable
fun displayTracks(vlcPlayer: MediaPlayer){
    val audioTracks = vlcPlayer.audioTracks
    val subtitleTracks = vlcPlayer.spuTracks
    var currentAudioTrack by remember { mutableStateOf(vlcPlayer.audioTrack) }
    var currentSubtitleTrack by remember { mutableStateOf(vlcPlayer.spuTrack) }
    Box (
        modifier = Modifier.fillMaxSize()
    ){
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 175.dp)
                .background(color = Color(0xFF222222).copy(alpha = 0.83f))
                .height(225.dp)
        ){
            Spacer(modifier = Modifier.width(5.dp))
            Column (
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp)
            ){
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Audio",
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(5.dp))
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (audioTracks.isNullOrEmpty()){
                    Text(
                        text = "No Audio",
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
                else{
                    LazyColumn {
                        items(audioTracks){ track ->
                            Row (
                                modifier = Modifier.clickable(
                                    onClick = {
                                        currentAudioTrack = track.id
                                        vlcPlayer.setAudioTrack(currentAudioTrack)
                                    }
                                )
                            ){
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(26.dp),
                                    tint = if (currentAudioTrack == track.id) Color.White.copy(alpha = 0.9f) else Color.Transparent
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "${track.name ?: track.id}",
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (currentAudioTrack == track.id) Color.White else Color.White.copy(alpha = 0.5f)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
            Column (
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp)
            ){
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Subtitles",
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(5.dp))
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (subtitleTracks.isNullOrEmpty()){
                    Text(
                        text = "No Subtitles",
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
                else{
                    LazyColumn {
                        items(subtitleTracks){ track ->
                            Row (
                                modifier = Modifier.clickable(
                                    onClick = {
                                        currentSubtitleTrack = track.id
                                        vlcPlayer.setSpuTrack(currentSubtitleTrack)
                                    }
                                )
                            ){
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(26.dp),
                                    tint = if (currentSubtitleTrack == track.id) Color.White.copy(alpha = 0.9f) else Color.Transparent
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "${track.name ?: track.id}",
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (currentSubtitleTrack == track.id) Color.White else Color.White.copy(alpha = 0.5f)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }

}