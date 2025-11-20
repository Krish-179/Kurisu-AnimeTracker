package com.example.c6

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun TrackerTopBar(navController: NavController){
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
                text = "Trackers",
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {}
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
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
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun TrackerBody(innerPadding : PaddingValues,context: ComponentActivity,malapi: Malapi,aniListApi: AniListApi,room: room){
    val flag = SettingsManager.getUpdateProgress(context).collectAsState(initial = false).value
    var showAlertDialog by remember { mutableStateOf(false) }
    var logOutTrackerName by remember { mutableStateOf("") }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ){
        Column (
            modifier = Modifier.padding(16.dp)
        ){
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            ){
                Text(
                    text = "Update progress after watching",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Normal,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = flag,
                    onCheckedChange = {
                        context.lifecycleScope.launch {
                            SettingsManager.setUpdateProgress(context,!flag)
                        }
                        SettingsManager.updateProgressFlag =!flag
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF0BA6DF),
                        checkedTrackColor = Color.Transparent,
                        checkedBorderColor = Color.White.copy(alpha = 0.45f),
                        uncheckedTrackColor = Color.Transparent,
                        uncheckedBorderColor = Color.White.copy(alpha = 0.45f)
                    )
                )
            }
            Text(
                text = "Trackers",
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
            )
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 4.dp)
                    .clickable(
                        onClick = {
                            if (malapi.access_token.isNullOrEmpty()){
                                malapi.loginMal()
                            }
                            else {
                                showAlertDialog = true
                                logOutTrackerName = "MyAnimeList"
                            }
                        }
                    )
            ){
                Image(
                    painter = painterResource(R.drawable.mal),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp)
                        .clip(shape = RoundedCornerShape(16.dp)),
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "MyAnimeList",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                if (!malapi.access_token.isNullOrEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.Green,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 4.dp)
                    .clickable(
                        onClick = {
                            if (aniListApi.access_token.isNullOrEmpty()){
                                aniListApi.loginAnilist()
                            }
                            else{
                                showAlertDialog = true
                                logOutTrackerName = "AniList"
                            }
                        }
                    )
            ){
                Image(
                    painter = painterResource(R.drawable.al),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp)
                        .clip(shape = RoundedCornerShape(16.dp)),
                    alignment = Alignment.Center,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "AniList",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                if (!aniListApi.access_token.isNullOrEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.Green,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
        if (showAlertDialog){
            AlertDialog(
                onDismissRequest = { showAlertDialog = false},
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (logOutTrackerName.contentEquals("MyAnimeList")){
                                room.setMalApiAccessToken(null)
                                room.setMalApiRefreshToken(null)
                            }
                            if (logOutTrackerName.contentEquals("AniList")){
                                room.setAniListToken(null)
                            }
                            showAlertDialog = false
                        }
                    ) {
                        Text(
                            text = "Log out",
                            color = Color.Red,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showAlertDialog = false}
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                title = {
                    Text(
                        text = "Log out from $logOutTrackerName",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                titleContentColor = Color.White,
                tonalElevation = 12.dp,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}