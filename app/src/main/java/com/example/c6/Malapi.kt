package com.example.c6

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


class Malapi(val context: ComponentActivity,val malUsing: MalUsing) {
    var room: room? = null
    var access_token: String? = null
    var refresh_token: String? = null
    private val clientId = "6826b7227389e1ab2fcfae4625efe8c2"
    private val redirectUrl ="myanimetracker://callback"
    val verifier = generateCodeVerifier()
    fun updateRoom(room: room,list: Token?){
        this.room = room
        access_token = list?.malApiAccessToken
        refresh_token = list?.malApiRefreshToken
        buildApi()
    }
    fun setToken(aT: String,rT: String){
        room?.setMalApiAccessToken(aT)
        room?.setMalApiRefreshToken(rT)
        context.lifecycleScope.launch(Dispatchers.IO) {
            access_token = room?.getMalApiAccessToken()
            refresh_token = room?.getMalApiRefreshToken()
        }
    }

    fun buildApi(){
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${access_token}")
                    .build()
                chain.proceed(request)
            }
            .authenticator { _,response ->
                val newToken = refreshToken()
                if (newToken != null) {
                    room?.setMalApiAccessToken(newToken)
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                } else null
            }
            .build()
        val retofit = Retrofit.Builder()
            .client(client)
            .baseUrl("https://api.myanimelist.net/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retofit.create(Mal::class.java)
        malUsing.apiSetter(api)
        malUsing.contextSetter(context)
    }

    fun loginMal(){
        val authUrl = "https://myanimelist.net/v1/oauth2/authorize" +
                "?response_type=code" +
                "&client_id=$clientId" +
                "&redirect_uri=$redirectUrl" +
                "&code_challenge=$verifier"

        val intent = CustomTabsIntent.Builder().build()
        intent.launchUrl(context, authUrl.toUri())
    }

    fun exchangeCodeForToken(code: String) {
        val url = "https://myanimelist.net/v1/oauth2/token"

        val client = OkHttpClient()
        val body = FormBody.Builder()
            .add("client_id", clientId)
            .add("code", code)
            .add("code_verifier", verifier) // prove the handshake
            .add("grant_type", "authorization_code")
            .add("redirect_uri", redirectUrl)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Anime", "Token request failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val json = JSONObject(responseBody)
                    setToken(json.getString("access_token"),json.getString("refresh_token"))
                    buildApi()
                }
            }
        })
    }
    fun refreshToken(): String?{
        val url = "https://myanimelist.net/v1/oauth2/token"

        val client = OkHttpClient()
        val body = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("refresh_token", refresh_token?:"")
            .add("client_id",clientId)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()

        if (response.isSuccessful && responseBody != null) {
            val json = JSONObject(responseBody)
            val newAccess = json.getString("access_token")
            val newRefresh = json.getString("refresh_token")

            room?.setMalApiRefreshToken(newRefresh)
            room?.setMalApiAccessToken(newAccess)

            return newAccess
        }

        return null
    }
}
fun generateCodeVerifier(): String {
    val charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-._~"
    return (1..64).map { charset.random() }.joinToString("")
}
