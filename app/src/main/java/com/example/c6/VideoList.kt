package com.example.c6

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFramePercent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue

@Composable
fun videoListTopBar(context: ComponentActivity,flags: AppFlags,folderUri: FolderUri,navController: NavController){
    val folder = DocumentFile.fromTreeUri(context, Uri.parse(folderUri.uri))
    Column {
        Spacer(modifier = Modifier.height(40.dp))
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ){
            Spacer(modifier = Modifier.width(10.dp))
            IconButton(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "${folder?.name}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    flags.selectAnimeFlag.value = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.5f))
    }
    BackHandler {
        flags.selectAnimeFlag.value = false
        navController.popBackStack()
    }
}
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun videoListBody(context: ComponentActivity, playerLauncher: ActivityResultLauncher<Intent>, innerPadding: PaddingValues, malUsing: MalUsing, aniListApi: AniListApi, room: room, folderUri: FolderUri, updateProgress: UpdateProgress, flag: Boolean){
    val flag1 = SettingsManager.getUpdateProgress(context).collectAsState(initial = false).value
    var isLoading by remember { mutableStateOf(true) }
    val isOnline = rememberNetworkState(context)
    var videoList by remember { mutableStateOf<List<DocumentFile>>(emptyList()) }
    if (flag && flag1){
        if (isOnline) {
            Log.d("VideoListEp","${updateProgress.epNumber} ${updateProgress.alAnimeId}")
            if (updateProgress.alAnimeId.value != 0) {
                aniListApi.updateAnimeList(updateProgress.alAnimeId.value,updateProgress.epNumber.value)
            }
            if (updateProgress.malAnimeId.value != 0) {
                malUsing.updateUserList(updateProgress.malAnimeId.value,updateProgress.epNumber.value)
            }
        }
        else{
            room.addEp(updateProgress.malAnimeId.value,updateProgress.alAnimeId.value,updateProgress.epNumber.value)
            Toast.makeText(context,"Update when internet available", Toast.LENGTH_SHORT).show()
        }
        SettingsManager.flag.value = !flag
    }
    LaunchedEffect(folderUri) {
        isLoading = true
        withContext(Dispatchers.IO) {
            val folder = DocumentFile.fromTreeUri(context, Uri.parse(folderUri.uri))
            val files = folder?.listFiles()
                ?.filter { file -> file.type?.startsWith("video/") == true }
                ?.sortedBy { it.name }
                ?: emptyList()
            isLoading = false
            videoList = files
        }
    }
    val scope = rememberCoroutineScope()
    if (isLoading){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ){
            CircularProgressIndicator()
        }
    }
    else if (videoList.isEmpty()){
        Box (
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ){
            Column (
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Image(
                    painter = painterResource(R.drawable.qiqi),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    colorFilter = null
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "No videos found",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }

    }
    else{
        Box (
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ){
            LazyColumn (
                modifier = Modifier.padding(top = 5.dp)
            ){
                items(videoList){ file ->
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick = {
                                    val number = Regex("""\d+""").findAll(
                                        file?.name?.substringBeforeLast('.') ?: ""
                                    ).map { it.value.toInt() }.toList().last()
                                    val intent =
                                        Intent(context, VideoPlayerActivity::class.java).apply {
                                            putExtra("videoUri", file?.uri)
                                            putExtra("epNumber", number)
                                            putExtra("fileName",file?.name)
                                        }
                                    playerLauncher.launch(intent)
                                    scope.launch {
                                        val folder =
                                            room.folderDao.getAnimeIds(folderUri.uri.toString())
                                        updateProgress.malAnimeId.value = folder.malId
                                        updateProgress.alAnimeId.value = folder.aniId
                                        updateProgress.epNumber.value = number
                                    }
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ){
                        Spacer(modifier = Modifier.width(5.dp))
                        Box (
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center
                        ){
                            if(file?.type=="video/mp4"){
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(file?.uri)
                                        .size(150,400)
                                        .decoderFactory(VideoFrameDecoder.Factory())
                                        .videoFramePercent(0.4)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .align(alignment = Alignment.Center)
                                        .size(80.dp)
                                        .clip(shape = RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else{
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .align(alignment = Alignment.TopCenter)
                                        .clip(shape = RoundedCornerShape(10.dp))
                                        .size(55.dp)
                                        .background(color = Color.Blue.copy(alpha = 0.7f))
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Column {
                            Text(
                                text = "${file?.name}",
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                            val size = ((file.length()/1024)/1024)
                            if (size > 1000){
                                val sizeInGB: Float = (size/1024f).absoluteValue
                                val formated = String.format("%.2f",sizeInGB)
                                Text(
                                    text = "$formated GB",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                            else{
                                Text(
                                    text = "${size} MB",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                            Spacer(modifier = Modifier.height(7.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.5f))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                }
            }
        }
    }
}