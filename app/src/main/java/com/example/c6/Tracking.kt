package com.example.c6

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalMaterial3Api::class)
class Tracking{
    var primarySelectedTab = mutableIntStateOf(0)
    var secondarySelectedTab = mutableIntStateOf(0)
    @Composable
    fun TrackingTopBar(){
        Column {
            val tabs = listOf("Watching","Tracker")
            Spacer(modifier = Modifier.height(50.dp))
            PrimaryTabRow(
                selectedTabIndex = primarySelectedTab.value,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Black,
                indicator = {
                    TabRowDefaults.Indicator(
                        color = Color.Blue.copy(alpha = 0.5f),
                        modifier = Modifier
                            .tabIndicatorOffset(
                                selectedTabIndex = primarySelectedTab.value,
                                matchContentSize = true
                            )
                            .height(4.5.dp)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = primarySelectedTab.value == index,
                        onClick = {
                            primarySelectedTab.value = index
                        },
                        text = {
                            Text(
                                text = title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
            if (primarySelectedTab.value == 1){
                val secondaryTabs = listOf("MyAnimeList","Anilist","Others")
                SecondaryTabRow(
                    selectedTabIndex = secondarySelectedTab.value,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color.Black,
                    indicator = {
                        TabRowDefaults.Indicator(
                            color = Color.Blue.copy(alpha = 0.5f),
                            modifier = Modifier
                                .tabIndicatorOffset(
                                    selectedTabIndex = secondarySelectedTab.value,
                                    matchContentSize = true
                                )
                                .height(4.5.dp)
                        )
                    }
                ) {
                    secondaryTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = secondarySelectedTab.value == index,
                            onClick = {
                                secondarySelectedTab.value = index
                            },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            selectedContentColor = Color.White,
                            unselectedContentColor = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
    @Composable
    fun TrackingBottomBar(navController: NavController){
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
                    .clickable(
                        onClick = {
                            navController.navigate("Home")
                        }
                    )
            ){
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Library",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ){
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Tracking",
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

    @androidx.annotation.OptIn(UnstableApi::class)
    @Composable
    fun TrackingBody(innerPadding : PaddingValues, malapi: Malapi, malUsing: MalUsing, aniListApi: AniListApi, room: room){
        val folderList = room.getFolderList().collectAsState(initial = emptyList()).value
        val animeList = malUsing.getAnimeList(folderList.filter { file -> file.malId != 0 }).collectAsState(initial = emptyList()).value
        val animeListAL = aniListApi.getAnimeById(folderList.filter { file -> file.aniId != 0  }).collectAsState(initial = emptyList()).value
        val isOnline = rememberNetworkState(LocalContext.current)
        if (!isOnline){
            Box(
                modifier = Modifier.fillMaxSize()
                    .padding(innerPadding)
                    .background(color = Color.Black),
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
            var isLoading by remember { mutableStateOf(true) }
            LaunchedEffect(animeList,animeListAL) {
                isLoading = !folderList.isEmpty() && (animeList.isEmpty() || animeListAL.isEmpty())
            }
            if (primarySelectedTab.value == 0){
                if (isLoading){
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ){
                        CircularProgressIndicator()
                    }
                }
                else if (folderList.isEmpty()){
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
                                text = "No folder Added",
                                color = Color.White.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                else{
                    Column (
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    ){
                        Text(
                            text = "MyAnimeList",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                                .padding(top = 10.dp),
                            fontFamily = FontFamily.SansSerif
                        )
                        LazyColumn {
                            items(animeList) { anime ->
                                Card (
                                    onClick = {},
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(0.1f)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 15.dp)
                                ){
                                    Row (
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(IntrinsicSize.Min)
                                    ){
                                        Spacer(modifier = Modifier.width(15.dp))
                                        Icon(
                                            imageVector = Icons.Default.Movie,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(30.dp)
                                        )
                                        Spacer(modifier = Modifier.width(15.dp))
                                        Column {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "${anime?.title}",
                                                color = Color.White,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Row (
                                                verticalAlignment = Alignment.CenterVertically
                                            ){
                                                var watched = anime?.my_list_status?.num_episodes_watched?.toFloat()?: 0f
                                                if (watched==null)
                                                    watched = 0f
                                                val total = anime?.num_episodes?.toFloat()?: 1f
                                                LinearProgressIndicator(
                                                    progress = { watched/total },
                                                    color = Color(color = 0xFF0BA6DF),
                                                    trackColor = Color.Black,
                                                    gapSize = 0.dp,
                                                    strokeCap = StrokeCap.Butt,
                                                    modifier = Modifier
                                                        .clip(shape = RoundedCornerShape(8.dp))
                                                        .border(
                                                            width = 0.2.dp,
                                                            color = Color.Black,
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .weight(1f)
                                                ){}
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "${watched.toInt()} / ${total.toInt()}",
                                                    color = Color.White.copy(alpha = 0.8f),
                                                    fontSize = 14.sp,
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }
                                    }
                                }
                            }
                        }
                        Text(
                            text = "AniList",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            fontFamily = FontFamily.SansSerif
                        )
                        LazyColumn {
                            items(animeListAL){ anime ->
                                Card (
                                    onClick = {},
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(0.1f)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 15.dp)
                                ){
                                    Row (
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(IntrinsicSize.Min)
                                    ){
                                        Spacer(modifier = Modifier.width(15.dp))
                                        Icon(
                                            imageVector = Icons.Default.Movie,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(30.dp)
                                        )
                                        Spacer(modifier = Modifier.width(15.dp))
                                        Column {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "${anime?.Media?.title?.english}",
                                                color = Color.White,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Row (
                                                verticalAlignment = Alignment.CenterVertically
                                            ){

                                                var watched = anime?.MediaList?.progress?.toFloat()
                                                if (watched==null)
                                                    watched = 0f
                                                val total = anime?.Media?.episodes?.toFloat()?: 1f
                                                LinearProgressIndicator(
                                                    progress = { watched/total },
                                                    color = Color(color = 0xFF0BA6DF),
                                                    trackColor = Color.Black,
                                                    gapSize = 0.dp,
                                                    strokeCap = StrokeCap.Butt,
                                                    modifier = Modifier
                                                        .clip(shape = RoundedCornerShape(8.dp))
                                                        .border(
                                                            width = 0.2.dp,
                                                            color = Color.Black,
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .weight(1f)
                                                ){}
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "${watched.toInt()} / ${total.toInt()}",
                                                    color = Color.White.copy(alpha = 0.8f),
                                                    fontSize = 14.sp,
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
            else if (primarySelectedTab.value == 1){
                if (secondarySelectedTab.value == 0){
                    if(!malapi.access_token.isNullOrEmpty()){
                        val list = malUsing.getUserList().collectAsState(initial = AnimeList()).value
                        var isMalLoading by remember { mutableStateOf(true) }
                        LaunchedEffect(list) {
                            isMalLoading = list.data.isEmpty()
                        }
                        if (isMalLoading){
                            Box(
                                modifier = Modifier.fillMaxSize()
                                    .padding(innerPadding),
                                contentAlignment = Alignment.Center
                            ){
                                CircularProgressIndicator()
                            }
                        }
                        else{
                            Box (
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ){
                                LazyColumn {
                                    items(list.data.chunked(2)){ rowItem ->
                                        Row (
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 6.dp, vertical = 8.dp)
                                        ) {
                                            rowItem.forEach { AnimeNode ->
                                                Card(
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = Color.Black,
                                                        contentColor = Color.White
                                                    ),
                                                    elevation = CardDefaults.cardElevation(7.dp),
                                                    modifier = Modifier
                                                        .weight(1f),
                                                    border = BorderStroke(
                                                        width = 1.5.dp,
                                                        color = Color.DarkGray,
                                                    )
                                                ) {
                                                    AsyncImage(
                                                        model = AnimeNode.node.main_picture.medium,
                                                        contentDescription = null,
                                                        alignment = Alignment.Center,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .fillMaxHeight()
                                                            .aspectRatio(0.7f),
                                                        contentScale = ContentScale.FillWidth
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = AnimeNode.node.title,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Black,
                                                        modifier = Modifier.padding(horizontal = 10.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 12.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        if (AnimeNode.node.num_episodes == AnimeNode.list_status.num_episodes_watched){
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                                modifier = Modifier
                                                                    .clip(shape = RoundedCornerShape(4.dp))
                                                                    .background(color = Color.Green.copy(alpha = 0.8f))
                                                                    .size(16.dp)
                                                            )
                                                        }
                                                        else{
                                                            Icon(
                                                                imageVector = Icons.Default.Refresh,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                                modifier = Modifier
                                                                    .clip(shape = RoundedCornerShape(4.dp))
                                                                    .background(color = Color.Red.copy(alpha = 0.8f))
                                                                    .size(16.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(5.dp))
                                                        Text(
                                                            text = " ${AnimeNode.list_status.num_episodes_watched} / ${AnimeNode.node.num_episodes}",
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                            }
                                            if (rowItem.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else{
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
                                    text = "MAL is not connected",
                                    color = Color.White.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                else if (secondarySelectedTab.value == 1){
                    if (!aniListApi.access_token.isNullOrEmpty()){
                        val list = aniListApi.getAniListList().collectAsState(initial = MediaList(listOf())).value
                        var isAniListLoading by remember { mutableStateOf(true) }
                        LaunchedEffect(list) {
                            isAniListLoading = list.entries.isEmpty()
                        }
                        if (isAniListLoading){
                            Box(
                                modifier = Modifier.fillMaxSize()
                                    .padding(innerPadding),
                                contentAlignment = Alignment.Center
                            ){
                                CircularProgressIndicator()
                            }
                        }
                        else{
                            Box (
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ){
                                LazyColumn {
                                    items(list.entries.chunked(2)){ rowItem ->
                                        Row (
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 6.dp, vertical = 8.dp)
                                        ) {
                                            rowItem.forEach { entry ->
                                                Card(
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = Color.Black,
                                                        contentColor = Color.White
                                                    ),
                                                    elevation = CardDefaults.cardElevation(5.dp),
                                                    modifier = Modifier
                                                        .border(
                                                            width = 2.5.dp,
                                                            color = Color.White.copy(alpha = 0.5f),
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .weight(1f),
                                                    border = BorderStroke(
                                                        width = 1.5.dp,
                                                        color = Color.DarkGray,
                                                    )
                                                ) {
                                                    AsyncImage(
                                                        model = entry.media.coverImage.large,
                                                        contentDescription = null,
                                                        alignment = Alignment.Center,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .fillMaxHeight()
                                                            .aspectRatio(0.7f),
                                                        contentScale = ContentScale.FillWidth
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    if (entry.media.title.english.isNullOrEmpty()){
                                                        Text(
                                                            text = entry.media.title.romaji,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            fontSize = 18.sp,
                                                            fontWeight = FontWeight.Black,
                                                            modifier = Modifier.padding(horizontal = 10.dp)
                                                        )
                                                    }
                                                    else{
                                                        Text(
                                                            text = entry.media.title.english,
                                                            fontSize = 18.sp,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            fontWeight = FontWeight.Black,
                                                            modifier = Modifier.padding(horizontal = 10.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 12.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        if (entry.progress == entry.media.episodes){
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                                modifier = Modifier
                                                                    .clip(shape = RoundedCornerShape(4.dp))
                                                                    .background(color = Color.Green.copy(alpha = 0.8f))
                                                                    .size(16.dp)
                                                            )
                                                        }
                                                        else{
                                                            Icon(
                                                                imageVector = Icons.Default.Refresh,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                                modifier = Modifier
                                                                    .clip(shape = RoundedCornerShape(4.dp))
                                                                    .background(color = Color.Red.copy(alpha = 0.8f))
                                                                    .size(16.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(5.dp))
                                                        Text(
                                                            text = " ${entry.progress} / ${entry.media.episodes}",
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                            }
                                            if (rowItem.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else{
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
                                    text = "AniList is not connected",
                                    color = Color.White.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                else{
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
                                text = "List is Empty",
                                color = Color.White.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(R.drawable.qiqi),
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth,
                            colorFilter = null
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Folder added",
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}