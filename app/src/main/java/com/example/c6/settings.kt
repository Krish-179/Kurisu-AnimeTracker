package com.example.c6

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun SettingsTopBar(){
    Column {
        Spacer(modifier = Modifier.height(40.dp))
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ){
            Spacer(modifier = Modifier.width(30.dp))
            Text(
                text = "Settings",
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.5f))
    }
}

@Composable
fun SettingsBottomBar(navController: NavController){
    HorizontalDivider(color = Color.White.copy(alpha = 0.5f))
    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp, horizontal = 20.dp)
    ){
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
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
            modifier = Modifier.weight(1f)
        ){
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = Color(0xFF0BA6DF).copy(alpha = 0.8f),
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Settings",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF0BA6DF).copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun SettingsBody(innerPadding : PaddingValues,navController: NavController){
    Box (
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ){
        Column (
            modifier = Modifier.padding(top = 20.dp)
        ){
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 15.dp)
                    .clip(shape = RoundedCornerShape(16.dp))
                    .background(color = Color.White.copy(alpha = 0.1f))
                    .clickable(onClick = {
                        navController.navigate("Tracker")
                    })
                    .height(IntrinsicSize.Min),
            ){
                Spacer(modifier = Modifier.width(15.dp))
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(15.dp))
                Column (
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ){
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Trackers",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Myanimelist, Anilist, syncing",
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(25.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 15.dp)
                    .clip(shape = RoundedCornerShape(16.dp))
                    .background(color = Color.White.copy(alpha = 0.1f))
                    .clickable(onClick = {
                        navController.navigate("Preferences")
                    })
                    .height(IntrinsicSize.Min)
            ){
                Spacer(modifier = Modifier.width(15.dp))
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(15.dp))
                Column (
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ){
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Preferences",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Default tab, Notifications",
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(25.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 15.dp)
                    .clip(shape = RoundedCornerShape(16.dp))
                    .background(color = Color.White.copy(alpha = 0.1f))
                    .clickable(onClick = {})
                    .height(IntrinsicSize.Min)
            ){
                Spacer(modifier = Modifier.width(15.dp))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(15.dp))
                Column (
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ){
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "About",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "App version, Privacy & terms",
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(25.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
        Text(
            text = "New Updates, Coming Soon.",
            color = Color.White.copy(alpha = 0.5f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.align(alignment = Alignment.Center)
        )
    }
}