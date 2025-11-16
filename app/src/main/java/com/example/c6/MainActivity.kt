package com.example.c6

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.c6.ui.theme.C6Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    val malUsing = MalUsing()
    val malApi = Malapi(this,malUsing)
    val aniListApi = AniListApi(this)
    val playerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val watchedCompletely = result.data?.getBooleanExtra("Update", false) ?: false
                if (watchedCompletely) {
                    SettingsManager.flag.value = true
                } else {
                    SettingsManager.flag.value = false
                }
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            C6Theme {
                val room = room(this)
                val updateProgress: UpdateProgress = viewModel()
                val searchKeyword: SearchKeyword = viewModel()
                AskNotificationPermission()
                val folderUri: FolderUri = viewModel()
                createNotificationChannel()
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        if (room.tokenDao.count() == 0) {
                            room.homeFilter.addRow(HomeFilter())
                            room.tokenDao.insertRow(
                                Token(
                                    malApiAccessToken = null,
                                    malApiRefreshToken = null,
                                    aniListToken = null
                                )
                            )
                        }
                    }
                }
                val list = room.token.collectAsState(initial = Token()).value
                malApi.updateRoom(room,list)
                aniListApi.updateRoom(room,list)
                val navController = rememberNavController()
                val flags: AppFlags = viewModel()
                var destination by remember { mutableStateOf("Home") }
                val tracking = Tracking()
                val watchedList = room.getOfflineWatchedList().collectAsState(initial = emptyList()).value
                val isOnline = rememberNetworkState(this)
                LaunchedEffect(Unit,watchedList,isOnline) {
                    if (isOnline && !watchedList.isEmpty()){
                        for (ep in watchedList){
                            Log.d("MainEP","$watchedList")
                            if (ep.malId != 0)
                                malUsing.updateUserList(ep.malId,ep.epNumber)
                            if (ep.aniId != 0)
                                aniListApi.updateAnimeList(ep.aniId,ep.epNumber)
                            room.deleteEp(ep)
                        }
                        SettingsManager.showNotification(this@MainActivity,"Pending Episodes Updated","Mal and AniList lists Updated")
                    }
                }
                NavHost(navController = navController, startDestination = "Home"){
                    composable("Home") {
                        destination = "Home"
                    }
                    composable("Tracking") {
                        destination = "Tracking"
                    }
                    composable("Settings") {
                        destination = "Settings"
                    }
                    composable("Tracker"){
                        destination = "Tracker"
                    }
                    composable("Preferences"){
                        destination = "Preferences"
                    }
                    composable("VideoList"){
                        destination = "VideoList"
                    }
                    composable("Exoplayer"){
                        destination = "Exoplayer"
                    }
                    composable("SelectAnime"){
                        destination = "SelectAnime"
                    }
                }
                Scaffold (
                    containerColor = Color.Black,
                    topBar = {
                        when(destination){
                            "Home" -> {
                                HomeTopBar(this,room,flags,folderUri)
                            }
                            "Tracking" -> {
                                tracking.TrackingTopBar()
                            }
                            "Settings" -> {
                                SettingsTopBar()
                            }
                            "Tracker" -> {
                                TrackerTopBar(navController)
                            }
                            "Preferences" -> {
                                PreferencesTopBar(navController)
                            }
                            "VideoList" -> {
                                videoListTopBar(this,flags,folderUri,navController)
                            }
                            "SelectAnime" -> {
                                selectAnimeTopBar(navController,flags,searchKeyword)
                            }
                        }
                    },
                    bottomBar = {
                        when(destination){
                            "Home" -> {
                                HomeBottomBar(navController,malUsing)
                            }
                            "Tracking" -> {
                                tracking.TrackingBottomBar(navController)
                            }
                            "Settings" -> {
                                SettingsBottomBar(navController)
                            }
                        }
                    }
                ){ innerPadding ->
                    when(destination){
                        "Home" -> {
                            HomeBody(innerPadding,room,this,navController,folderUri,flags)
                        }
                        "Tracking" -> {
                            tracking.TrackingBody(innerPadding,malApi,malUsing,aniListApi,room)
                        }
                        "Settings" -> {
                            SettingsBody(innerPadding,navController)
                        }
                        "Tracker" -> {
                            TrackerBody(innerPadding,this,malApi,aniListApi,room)
                        }
                        "Preferences" -> {
                            PreferencesBody(innerPadding,this)
                        }
                        "VideoList" -> {
                            videoListBody(this,playerLauncher,innerPadding,malUsing,aniListApi,room,folderUri,updateProgress,
                                SettingsManager.flag.value)
                        }
                        "SelectAnime" -> {
                            selectAnimeBody(innerPadding,room,malUsing,aniListApi,folderUri,flags,navController,searchKeyword)
                        }
                    }
                }
                if (flags.selectAnimeFlag.value)
                selectAnime(flags,navController)
            }
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val data = intent.data
        if (data != null) {
            when (data.host) {

                // MAL callback
                "callback" -> {
                    val code = data.getQueryParameter("code")
                    if (code != null) {
                        malApi.exchangeCodeForToken(code)
                    }
                }

                // AniList callback
                "ani-callback" -> {
                   val fragment = data.fragment?: return
                    val params = fragment.split("&").associate {
                        val (k, v) = it.split("=")
                        k to v
                    }
                    val accessToken = params["access_token"]
                    aniListApi.setAccessToken(accessToken?:"")
                }

                else -> {
                    return
                }
            }
        }
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "my_channel_id",
                "My Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel description"
            }
            val manager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    @Composable
    fun AskNotificationPermission() {
        val context = LocalContext.current

        // Launcher to show system permission dialog
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(context, "Permission Granted ✅", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Permission Denied ❌", Toast.LENGTH_SHORT).show()
            }
        }

        // Only for Android 13+
        LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permission = android.Manifest.permission.POST_NOTIFICATIONS
                if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    launcher.launch(permission) // 🔥 shows system dialog
                }
            }
        }
    }

}
class FolderUri: ViewModel(){
    var uri: String? = null
}
class AppFlags: ViewModel(){
    val homeFilterSelected = mutableStateOf(false)
    var malSelected = mutableStateOf(false)
    var aniSelected = mutableStateOf(false)
    var selectAnimeFlag= mutableStateOf(false)
}
class UpdateProgress: ViewModel() {
    val malAnimeId = mutableIntStateOf(0)
    val alAnimeId = mutableIntStateOf(0)
    val epNumber = mutableIntStateOf(0)
}
class SearchKeyword: ViewModel() {
    val keyword = mutableStateOf("")
}