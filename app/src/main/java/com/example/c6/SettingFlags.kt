package com.example.c6

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.app.NotificationCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settingsFlags")

object SettingsManager{
    private val UPDATE_PROGRESS_AFTER_WATCHING = booleanPreferencesKey("update_progress")
    private val NOTIFICATIONS = booleanPreferencesKey("notifications")
    var updateProgressFlag: Boolean? = true
    var notification: Boolean? = true
    var flag = mutableStateOf(false)
    fun showNotification(context: ComponentActivity?, title: String, message: String) {
        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "my_channel_id")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(1001, builder.build())
    }

    suspend fun setUpdateProgress(context: Context,value: Boolean){
        context.settingsDataStore.edit {
            it[UPDATE_PROGRESS_AFTER_WATCHING] = value
        }
    }
    suspend fun setNotification(context: Context,value: Boolean){
        context.settingsDataStore.edit {
            it[NOTIFICATIONS] = value
        }
    }
    fun getUpdateProgress(context: Context): Flow<Boolean> {
        return context.settingsDataStore.data.map {
            it[UPDATE_PROGRESS_AFTER_WATCHING] ?: true
        }
    }
    fun getNotifications(context: Context): Flow<Boolean>{
        return context.settingsDataStore.data.map {
            it[NOTIFICATIONS] ?: true
        }
    }
}
class NetworkMonitor(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isConnected.value = true
        }

        override fun onLost(network: Network) {
            _isConnected.value = false
        }
    }

    init {
        // Start listening to network changes
        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        // Initial value
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        _isConnected.value = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    fun unregister() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}
@Composable
fun rememberNetworkState(context: Context): Boolean {
    val monitor = remember { NetworkMonitor(context) }
    val isConnected by monitor.isConnected.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            monitor.unregister()
        }
    }

    return isConnected
}

