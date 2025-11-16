package com.example.c6

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        vlcPlayer = MediaPlayer(libVlc).apply {
            setEventListener { event ->
                when (event.type){
                    MediaPlayer.Event.EndReached -> {
                        runOnUiThread {
                            returnResult(true)
                        }
                    }
                }
            }
        }

        setContent {
            val uri = intent.getParcelableExtra<Uri>("videoUri")
            val number = intent.getIntExtra("epNumber", 0)
            val fileName = intent.getStringExtra("fileName")
            epNumber = number
            if (uri != null) {
                FullVlcPlayer(uri,fileName)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val watchedNow = duration > 0 && (lastProgress.toDouble() / duration) * 100 >= 90
        setResult(RESULT_OK, Intent().putExtra("Update", watchedNow))
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
    fun formatTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    private fun skipForward(){
        vlcPlayer.time = (vlcPlayer.time + 10000).coerceAtMost(vlcPlayer.length)
    }
    private fun skipBackward(){
        vlcPlayer.time = (vlcPlayer.time - 10000).coerceAtLeast(0)
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
    fun FullVlcPlayer(uri: Uri,fileName: String?) {
        val context = LocalActivity.current as ComponentActivity
        var isLoading by remember { mutableStateOf(true) }
        var showControls by remember { mutableStateOf(false) }
        var isPlaying by remember { mutableStateOf(false) }
        var showTracks by remember { mutableStateOf(false) }
        var sliderDuration by remember { mutableStateOf(0f) }
        var isSeeking by remember { mutableStateOf(false) }
        var watched by remember { mutableStateOf("") }

        BackHandler(true) {
            val watchedNow =
                vlcPlayer.length > 0 &&
                        (vlcPlayer.time.toDouble() / vlcPlayer.length.toDouble()) * 100.0 >= 90.0
            returnResult(watchedNow) // setResult + finish()
        }
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
                        onClick = {}
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                showControls = !showControls
                                showTracks = false
                            },
                            onDoubleTap = { offset ->
                                val boxWidth = size.width
                                if (offset.x < boxWidth / 3) {
                                    skipBackward()
                                } else if (offset.x > boxWidth * 2 / 3) {
                                    skipForward()
                                } else {
                                    if (isPlaying) {
                                        isPlaying = false
                                        vlcPlayer.pause()
                                    } else {
                                        isPlaying = true
                                        vlcPlayer.play()
                                    }
                                }
                            }
                        )
                    },
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
                        delay(4000)
                        if (showControls && !isSeeking) showControls = false
                    }
                }
                LaunchedEffect(vlcPlayer) {
                    while (true) {
                        delay(1000)
                        duration = vlcPlayer.length
                        lastProgress = vlcPlayer.time
                        watched = formatTime(vlcPlayer.time)
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
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = { offset ->
                                    val boxWidth = size.width
                                    if (offset.x < boxWidth / 3) {
                                        skipBackward()
                                    } else if (offset.x > boxWidth * 2 / 3) {
                                        skipForward()
                                    } else {
                                        if (isPlaying) {
                                            isPlaying = false
                                            vlcPlayer.pause()
                                        } else {
                                            isPlaying = true
                                            vlcPlayer.play()
                                        }
                                    }
                                }
                            )
                        }
                ){
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 50.dp, vertical = 30.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Top
                    ){
                        Text(
                            text = fileName?:"",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Normal,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Transparent),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 45.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Text(
                                text = watched,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                            )
                            Text(
                                text = formatTime(vlcPlayer.length),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
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
                                activeTrackColor = Color(0xFF0BA6DF).copy(alpha = 0.7f),
                                inactiveTrackColor = Color.White.copy(alpha = 0.6f),
                                activeTickColor = Color.Transparent,
                                inactiveTickColor = Color.Transparent,
                                thumbColor = Color(0xFF0BA6DF),
                                disabledThumbColor = Color.Transparent,
                                disabledActiveTrackColor = Color.Transparent,
                                disabledActiveTickColor = Color.Transparent,
                                disabledInactiveTrackColor = Color.Transparent,
                                disabledInactiveTickColor = Color.Transparent,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 30.dp)
                                .height(20.dp),
                            thumb = {
                                Box (
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(color = Color(0xFF0BA6DF), shape = CircleShape)
                                        .align(alignment = Alignment.CenterHorizontally)
                                )
                            },
                            track = { sliderState ->
                                SliderDefaults.Track(
                                    sliderState = sliderState,
                                    modifier = Modifier.height(3.dp)
                                )
                            }
                        )
                        Row (
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 40.dp)
                                .padding(bottom = 20.dp),
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
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Box (
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ){
                                if (isPlaying){
                                    IconButton(
                                        onClick = {
                                            vlcPlayer.pause()
                                            isPlaying = false
                                        },
                                        modifier = Modifier.align(alignment = Alignment.Center)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PauseCircleOutline,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                                else {
                                    IconButton(
                                        onClick = {
                                            vlcPlayer.play()
                                            isPlaying = true
                                        },
                                        modifier = Modifier.align(alignment = Alignment.Center)
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
                            IconButton(
                                onClick = {}
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Subtitles,
                                    contentDescription = null,
                                    tint = Color.Transparent,
                                    modifier = Modifier.size(30.dp)
                                )
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
                .padding(horizontal = 165.dp)
                .background(color = Color(0xFF222222).copy(alpha = 0.83f))
                .height(250.dp)
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
                    LazyColumn (
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ){
                        items(audioTracks){ track ->
                            Row (
                                modifier = Modifier.clickable(
                                    onClick = {
                                        currentAudioTrack = track.id
                                        vlcPlayer.setAudioTrack(currentAudioTrack)
                                    }
                                ),
                                horizontalArrangement = Arrangement.Start
                            ){
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(26.dp),
                                    tint = if (currentAudioTrack == track.id) Color.White.copy(alpha = 0.9f) else Color.Transparent
                                )
                                Spacer(modifier = Modifier.width(8.dp))
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
            VerticalDivider(color = Color.White.copy(alpha = 0.5f))
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
                    LazyColumn (
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ){
                        items(subtitleTracks){ track ->
                            Row (
                                modifier = Modifier.clickable(
                                    onClick = {
                                        currentSubtitleTrack = track.id
                                        vlcPlayer.setSpuTrack(currentSubtitleTrack)
                                    }
                                ),
                                horizontalArrangement = Arrangement.Start
                            ){
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(26.dp),
                                    tint = if (currentSubtitleTrack == track.id) Color.White.copy(alpha = 0.9f) else Color.Transparent
                                )
                                Spacer(modifier = Modifier.width(8.dp))
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