package com.example.c6

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

class MalUsing(){
    private var api :Mal? = null
    private var context: ComponentActivity? = null

    fun apiSetter(API : Mal){
        api=API
    }
    fun contextSetter(Context: ComponentActivity){
        context=Context
    }
    fun getAnimeList(folderlist: List<Folder>): Flow<List<WatchingAnime?>> = flow{
        val animeList = mutableListOf<WatchingAnime?>()
        for (f in folderlist){
            animeList.add(api?.getAnime(anime_id = f.malId))
        }
        emit(animeList)
    }.catch { e->
        Log.d("Error","Tracker List in MalUsing",e)
    }
    fun updateUserList(id: Int,watchedEps: Int){
        if (SettingsManager.updateProgressFlag == true) {
            context?.lifecycleScope?.launch {
                api?.updateUserList(id,watchedEps)
                if (SettingsManager.notification == true){
                    SettingsManager.showNotification(context,"Anime List Updated","Updated the MyAnimeList for Anime you watched")
                }
            }

        }
    }
    fun searchAnimeList(title: String): Flow<List<AnimeData>> = flow {
        val list = api?.getAnimeList(query = title)?.data
        emit(list?:listOf())
    }.catch { e->
        Log.e("Error","Getting Anime List from mal",e)
        emit(listOf())
    }
    fun getUserList(): Flow<AnimeList> = flow {
        val list = api?.userList("@me","list_status,num_episodes")
        emit(list?: AnimeList())
    }.catch { e->
        Log.d("Error","Getting User List in MAl",e)
        emit(AnimeList())
    }
}

data class Anime(
    val id : Int,
    val title : String,
    val main_picture: MainPicture,
    val num_episodes: Int
)
data class WatchingAnime(
    val title : String,
    val num_episodes: Int,
    val my_list_status: ListStatus
)
data class AnimeNode(
    val node: Anime,
    val list_status: ListStatus
)
data class ListStatus(
    val num_episodes_watched: Int
)
data class MainPicture(
    val large : String?,
    val medium : String
)
data class AnimeList(
    val data: List<AnimeNode> = emptyList()
)
interface Mal{
    @GET("users/{userName}/animelist")
    suspend fun userList(
        @Path("userName")  userName: String,
        @Query("fields") fields: String = "list_status,num_episodes",
        @Query("limit")  limit: Int = 100
    ): AnimeList

    @GET("anime/{anime_id}")
    suspend fun getAnime(
        @Path("anime_id") anime_id: Int,
        @Query("fields") fields: String = "title,num_episodes,my_list_status"
    ): WatchingAnime

    @GET("anime")
    suspend fun getAnimeList(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("fields") fields: String = "title,media_type,synopsis,status,rating,main_picture"
    ): AnimeListForSelecting

    @FormUrlEncoded
    @PATCH("anime/{anime_id}/my_list_status")
    suspend fun updateUserList(
        @Path("anime_id") anime_id: Int,
        @Field("num_watched_episodes") num_watched_episodes: Int
    )
}
data class AnimeListForSelecting(
    val data: List<AnimeData>
)
data class AnimeData(
    val node: AnimeInfoForSelecting
)
data class AnimeInfoForSelecting(
    val id: Int,
    val media_type: String,
    val synopsis: String?,
    val status: String,
    val rating: String?,
    val title: String,
    val main_picture: MainPicture
)
