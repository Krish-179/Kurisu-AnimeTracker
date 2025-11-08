package com.example.c6

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun HomeTopBar(context: ComponentActivity,room: room,flags: AppFlags,folderUri: FolderUri){
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) {
        uri ->
        uri?.let {
            folderUri.uri = it.toString()
            scope.launch {
                val exists = room.folderDao.containsUri(folderUri.uri.toString())
                if (exists == true){
                    Toast.makeText(context,"Already added", Toast.LENGTH_SHORT).show()
                }
                else{
                    context.contentResolver.takePersistableUriPermission(it,Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    room.addFolder(it.toString())
                    flags.malSelected.value = false
                    flags.aniSelected.value = false
                    flags.selectAnimeFlag.value = true
                }
            }
        }
    }
    Column {
        Spacer(modifier = Modifier.height(40.dp))
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ){
            Spacer(modifier = Modifier.width(30.dp))
            Text(
                text = "Library",
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            IconButton(
                onClick = {
                    launcher.launch(null)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            IconButton(
                onClick = {
                    flags.homeFilterSelected.value = !flags.homeFilterSelected.value
                }
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
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
}

@OptIn(UnstableApi::class)
@Composable
fun HomeBottomBar(navController: NavController,malUsing: MalUsing){
    HorizontalDivider(color = Color.White.copy(alpha = 0.5f))
    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp, horizontal = 20.dp)
    ){
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
        ){
            Icon(
                imageVector = Icons.Default.VideoLibrary,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Library",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .clickable(
                    onClick = {
                        navController.navigate("Tracking")
                    }
                )
        ){
            Icon(
                imageVector = Icons.Default.Repeat,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Tracking",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .clickable(
                    onClick = {
                        navController.navigate("Settings")
                    }
                )
        ){
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Settings",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@OptIn(UnstableApi::class)
@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeBody(innerPadding : PaddingValues,room: room,context: ComponentActivity,navController: NavController,folderUri1: FolderUri,flags: AppFlags){
    val sort = room.getSortBy().collectAsState(initial = HomeFilter()).value.sortBy
    var folderList: List<Folder>
    var isLoading by remember { mutableStateOf(true) }
    val folderCount = room.folderDao.getCount().collectAsState(initial = -1).value
    when(sort){
        "Name" -> folderList = room.getSortedByNameList().collectAsState(initial = emptyList()).value
        "Time" -> folderList = room.getSortedByTimeList().collectAsState(initial = emptyList()).value
        "Size" -> folderList = room.getSortedByVideoList().collectAsState(initial = emptyList()).value
        else -> folderList = room.getFolderList().collectAsState(initial = emptyList()).value
    }
    LaunchedEffect(folderList) {
        isLoading = folderList.isEmpty()
    }
    folderList = folderList.filter { folder ->
        val file = DocumentFile.fromTreeUri(context, Uri.parse(folder.folderUri))
        if ( file != null && file.exists())
            true
        else{
            room.deleteFolder(folder.folderUri)
            false
        }
    }
    Log.d("Home","$folderCount")
    if (isLoading && folderCount != 0){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ){
            CircularProgressIndicator()
        }
    }
    else if (folderList.isNullOrEmpty() && folderCount == 0){
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
                    text = "Add Folder",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )
            }
            if (flags.homeFilterSelected.value){
                Column (
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(top = 3.dp, end = 3.dp)
                        .align(alignment = Alignment.TopEnd)
                        .clip(shape = RoundedCornerShape(22.dp))
                        .background(color = Color(0xFF222326))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ){
                    Spacer(modifier = Modifier.height(10.dp))
                    if (sort.contentEquals("Name")){
                        Text(
                            text = "Name",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 24.sp
                            ),
                            color = Color(0xFF0BA6DF),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .fillMaxWidth(0.3f)
                                .clickable(
                                    onClick = {
                                        flags.homeFilterSelected.value = false
                                    }
                                )
                        )
                    }
                    else{
                        Text(
                            text = "Name",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 24.sp
                            ),
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .fillMaxWidth(0.3f)
                                .clickable(
                                    onClick = {
                                        room.updateSortBy("Name")
                                        flags.homeFilterSelected.value = false
                                    }
                                )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (sort.contentEquals("Time")) {
                        Text(
                            text = "Time",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 24.sp
                            ),
                            color = Color(0xFF0BA6DF),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .fillMaxWidth(0.3f)
                                .clickable(
                                    onClick = {
                                        flags.homeFilterSelected.value = false
                                    }
                                )
                        )
                    }
                    else{
                        Text(
                            text = "Time",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 24.sp
                            ),
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .fillMaxWidth(0.3f)
                                .clickable(
                                    onClick = {
                                        room.updateSortBy("Time")
                                        flags.homeFilterSelected.value = false
                                    }
                                )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (sort.contentEquals("Size")) {
                        Text(
                            text = "Episodes",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 24.sp
                            ),
                            color = Color(0xFF0BA6DF),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .fillMaxWidth(0.3f)
                                .clickable(
                                    onClick = {
                                        flags.homeFilterSelected.value = false
                                    }
                                )
                        )
                    }
                    else{
                        Text(
                            text = "Episodes",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 24.sp
                            ),
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .fillMaxWidth(0.3f)
                                .clickable(
                                    onClick = {
                                        room.updateSortBy("Size")
                                        flags.homeFilterSelected.value = false
                                    }
                                )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
    else{
        var showAlertDialog by remember { mutableStateOf(false) }
        var deletingFolderUri by remember { mutableStateOf<String?>("") }
        Box (
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ){
            LazyColumn (
                modifier = Modifier
                    .padding(top = 20.dp)
                    .clickable(
                        onClick = {
                            flags.homeFilterSelected.value = false
                        }
                    )
                    .fillMaxHeight()
            ){
                items(folderList){ folder ->
                    val file = DocumentFile.fromTreeUri(context,Uri.parse(folder.folderUri))
                    LaunchedEffect(Unit) {
                        room.updateNameVideo(file?.listFiles()?.filter { file->
                            val type = file.type
                            type!=null && type.startsWith("video/")
                        }?.size,file?.name,folder.folderUri)
                    }
                    Card (
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 15.dp)
                            .pointerInput(Unit){
                                detectTapGestures(
                                    onLongPress = {
                                        showAlertDialog = true
                                        deletingFolderUri = folder.folderUri
                                    },
                                    onTap = {
                                        room.updateLastUsed(System.currentTimeMillis(),folder.folderUri)
                                        folderUri1.uri = folder.folderUri
                                        navController.navigate("VideoList")
                                    }
                                )
                            }
                    ){
                        Row (
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                        ){
                            Spacer(modifier = Modifier.width(15.dp))
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.width(15.dp))
                            Column {
                                Spacer(modifier = Modifier.height(10.dp))
                                if (file?.name?.length?: 0 >= 50){
                                    Text(
                                        text = "${file?.name?.substring(0,50)} ...",
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                else{
                                    Text(
                                        text = "${file?.name}",
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "${file?.listFiles()?.filter { file ->
                                        val type = file.type
                                        type != null &&type.startsWith("video/")
                                    }?.size} videos",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 12.sp,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Spacer(modifier = Modifier.width(15.dp))
                        }
                    }
                }
            }
            if (flags.homeFilterSelected.value){
                Column (
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(top = 3.dp, end = 3.dp)
                        .align(alignment = Alignment.TopEnd)
                        .clip(shape = RoundedCornerShape(22.dp))
                        .background(color = Color(0xFF222326))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ){
                    Spacer(modifier = Modifier.height(10.dp))
                    if (sort.contentEquals("Name")){
                        Text(
                            text = "Name",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 24.sp
                            ),
                            color = Color(0xFF0BA6DF),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .fillMaxWidth(0.3f)
                                .clickable(
                                    onClick = {
                                        flags.homeFilterSelected.value = false
                                    }
                                )
                        )
                    }
                    else{
                        Text(
                            text = "Name",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 24.sp
                            ),
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .fillMaxWidth(0.3f)
                                .clickable(
                                    onClick = {
                                        room.updateSortBy("Name")
                                        flags.homeFilterSelected.value = false
                                    }
                                )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (sort.contentEquals("Time")) {
                        Text(
                            text = "Time",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 24.sp
                            ),
                            color = Color(0xFF0BA6DF),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .fillMaxWidth(0.3f)
                                .clickable(
                                    onClick = {
                                        flags.homeFilterSelected.value = false
                                    }
                                )
                        )
                    }
                    else{
                        Text(
                            text = "Time",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 24.sp
                            ),
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .fillMaxWidth(0.3f)
                                .clickable(
                                    onClick = {
                                        room.updateSortBy("Time")
                                        flags.homeFilterSelected.value = false
                                    }
                                )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (sort.contentEquals("Size")) {
                        Text(
                            text = "Episodes",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 24.sp
                            ),
                            color = Color(0xFF0BA6DF),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .fillMaxWidth(0.3f)
                                .clickable(
                                    onClick = {
                                        flags.homeFilterSelected.value = false
                                    }
                                )
                        )
                    }
                    else{
                        Text(
                            text = "Episodes",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 24.sp
                            ),
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .fillMaxWidth(0.3f)
                                .clickable(
                                    onClick = {
                                        room.updateSortBy("Size")
                                        flags.homeFilterSelected.value = false
                                    }
                                )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            if (showAlertDialog){
                AlertDialog(
                    onDismissRequest = { showAlertDialog = false},
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (!deletingFolderUri.isNullOrEmpty()){
                                    room.deleteFolder(deletingFolderUri)
                                }
                                showAlertDialog = false
                            }
                        ) {
                            Text(
                                text = "Remove",
                                color = Color.Red
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showAlertDialog = false
                            }
                        ) {
                            Text(
                                text = "Cancel",
                                color = Color.White
                            )
                        }
                    },
                    title = {
                        Text("Confirm Remove")
                    },
                    text = {
                        Text("Are you sure you want to remove this folder?")
                    },
                    shape = RoundedCornerShape(16.dp),
                    textContentColor = Color.White.copy(alpha = 0.8f),
                    titleContentColor = Color.White,
                    tonalElevation = 12.dp
                )
            }
        }
    }
}
@Composable
fun selectAnime(flags: AppFlags,navController: NavController) {
    Box(
        modifier = Modifier
            .background(color = Color.Transparent.copy(alpha = 0.8f))
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 30.dp)
                .clip(shape = RoundedCornerShape(12.dp))
                .background(color = Color.Black)
                .fillMaxWidth()
        ){
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Select Tracker",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 18.dp)
                    .clickable(
                        onClick = {
                            navController.navigate("SelectAnime")
                            flags.selectAnimeFlag.value = false
                            flags.malSelected.value = true
                        }
                    )
            ){
                Image(
                    painter = painterResource(R.drawable.mal),
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(shape = RoundedCornerShape(16.dp)),
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "MyAnimeList",
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 18.dp)
                    .clickable(
                        onClick = {
                            flags.selectAnimeFlag.value = false
                            flags.aniSelected.value = true
                            navController.navigate("SelectAnime")
                        }
                    )
            ){
                Image(
                    painter = painterResource(R.drawable.al),
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(shape = RoundedCornerShape(16.dp)),
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "AniList",
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}