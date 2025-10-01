package com.example.c6

import android.net.Uri
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.NavController
import coil.compose.AsyncImage

@Composable
fun selectAnimeTopBar(navController: NavController,flags: AppFlags,searchKeyword: SearchKeyword){
    val focusRequester = remember { FocusRequester() }
    var extended by remember { mutableStateOf(false) }
    var textFieldValue by remember { mutableStateOf("") }
    LaunchedEffect(extended) {
        if (extended) {
            focusRequester.requestFocus()
        }
    }
    Column {
        Spacer(modifier = Modifier.height(40.dp))
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ){
            Spacer(modifier = Modifier.width(10.dp))
            IconButton(
                onClick = {
                    flags.malSelected.value = false
                    flags.aniSelected.value = false
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
            if (extended){
                TextField(
                    value = textFieldValue,
                    onValueChange = {
                        textFieldValue = it
                        searchKeyword.keyword.value = it
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    ),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = "Search Anime to Track",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                        )
                    },
                    modifier = Modifier.focusRequester(focusRequester)
                        .width(IntrinsicSize.Min)
                )
            }
            else{
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Select Anime to Track",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
            }
            if (!extended){
                IconButton(
                    onClick = {
                        extended = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            else{
                IconButton(
                    onClick = {
                        extended = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(25.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.5f))
    }
}

@Composable
fun selectAnimeBody(innerPadding: PaddingValues,room: room,malUsing: MalUsing,aniListApi: AniListApi,folderUri: FolderUri,flags: AppFlags,navController: NavController,searchKeyword: SearchKeyword){
    var isLoading by remember { mutableStateOf(true) }
    DisposableEffect(Unit){
        onDispose {
            searchKeyword.keyword.value = ""
            flags.aniSelected.value = false
            flags.malSelected.value = false
        }
    }
    val animeFolderRegex = Regex("""(?:\[[^\]]+\]\s*)?(.+?)(?:\s*[-_]?\s*\d+.*)?$""")
    val folderName = DocumentFile.fromTreeUri(LocalContext.current, Uri.parse(folderUri.uri))?.name
    val isOnline = rememberNetworkState(LocalContext.current)
    var keyword = animeFolderRegex.find(folderName ?: "")?.groups?.get(1)?.value
        ?.replace("""\[[^\]]*]""".toRegex(), "") // remove [tags]
        ?.replace("""\((?:Season|OVA|Movie|BD|Ep).*?\)""".toRegex(), "") // remove season/extra junk in ()
        ?.trim()
    if (searchKeyword.keyword.value.isNotEmpty()){
        keyword = searchKeyword.keyword.value
    }
    if (!isOnline){
        Box(
            modifier = Modifier.fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ){
            Text(
                text = "⚠ No internet connection",
                color = Color.Red,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Black
            )
        }
    }
    else{
        if (flags.malSelected.value){
            val malList = malUsing.searchAnimeList(keyword?:"").collectAsState(initial = emptyList()).value
            LaunchedEffect(malList) {
                isLoading = malList.isEmpty()
            }
            if (isLoading){
                Box(
                    modifier = Modifier.fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ){
                    CircularProgressIndicator()
                }
            }
            else{
                Box(
                    modifier = Modifier.padding(innerPadding)
                        .fillMaxSize()
                ){
                    LazyColumn (
                        modifier = Modifier.padding(top = 15.dp)
                    ){
                        items(malList){ anime->
                            Row (
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                                    .fillMaxWidth()
                                    .clickable(
                                        onClick = {
                                            room.updateMalAnimeId(anime.node.id,folderUri.uri?:"")
                                            navController.popBackStack()
                                        }
                                    )
                            ){
                                Spacer(modifier = Modifier.width(5.dp))
                                AsyncImage(
                                    model = anime.node.main_picture.medium,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(width = 125.dp,200.dp)
                                        .clip(shape = RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column (
                                    horizontalAlignment = Alignment.Start,
                                    verticalArrangement = Arrangement.Top,
                                    modifier = Modifier.fillMaxWidth()
                                        .fillMaxHeight()
                                        .align(alignment = Alignment.Top)
                                        .padding(horizontal = 10.dp)
                                ){
                                    Text(
                                        text = "${anime.node.title}",
                                        color = Color.White,
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Text(
                                        text = "Status: ${anime.node.status.replace("_"," ").capitalize()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Text(
                                        text = "Type: ${anime.node.media_type.replace("_"," ").capitalize()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.height(5.dp))
                                    if ((anime.node.synopsis?.length ?: 0) > 75){
                                        Text(
                                            text = "${anime.node.synopsis?.substring(0,75)} ...",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                    else{
                                        Text(
                                            text = "${anime.node.synopsis}",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 10.dp))
                        }
                    }
                }
            }
        }
        if (flags.aniSelected.value){
            val aniList = aniListApi.searchAnimeList(keyword).collectAsState(initial = emptyList<AnimeDetails>()).value
            LaunchedEffect(aniList) {
                isLoading = aniList.isEmpty()
            }
            if (isLoading){
                Box(
                    modifier = Modifier.fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ){
                    CircularProgressIndicator()
                }
            }
            else{
                Box(
                    modifier = Modifier.padding(innerPadding)
                        .fillMaxSize()
                ){
                    LazyColumn (
                        modifier = Modifier.padding(top = 15.dp)
                    ){
                        items(aniList){ anime->
                            Row (
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                                    .fillMaxWidth()
                                    .clickable(
                                        onClick = {
                                            room.updateAniAnimeId(anime.id,folderUri.uri?:"")
                                            navController.popBackStack()
                                        }
                                    )
                            ){
                                Spacer(modifier = Modifier.width(5.dp))
                                AsyncImage(
                                    model = anime.coverImage.large,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(width = 125.dp,200.dp)
                                        .clip(shape = RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column (
                                    horizontalAlignment = Alignment.Start,
                                    verticalArrangement = Arrangement.Top,
                                    modifier = Modifier.fillMaxWidth()
                                        .fillMaxHeight()
                                        .align(alignment = Alignment.Top)
                                        .padding(horizontal = 10.dp)
                                ){
                                    if (anime.title.english!=null) {
                                        Text(
                                            text = "${anime.title.english}",
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                    else{
                                        Text(
                                            text = "${anime.title.romaji}",
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Text(
                                        text = "Status: ${anime.status.replace("_"," ").capitalize()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Text(
                                        text = "Type: ${anime.format.replace("_"," ").capitalize()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.height(5.dp))
                                    if ((anime.description?.length ?: 0) > 75){
                                        Text(
                                            text = "${anime.description?.substring(0,75)} ...",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                    else{
                                        Text(
                                            text = "${anime.description}",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 10.dp))
                        }
                    }
                }
            }
        }
    }
}