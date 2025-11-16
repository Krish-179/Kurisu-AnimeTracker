package com.example.c6

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class AniListApi(val context: ComponentActivity){
    private var api : Alapi? = null
    var room: room? = null
    var access_token: String? = null
    fun apiSetter(API : Alapi){
        api=API
    }
    fun updateRoom(room: room,list: Token){
        this.room = room
        access_token = list.aniListToken
        buildApi()
    }
    var variable: Map<String, Any?>? =null
    fun updateVariable(variables: Map<String, Any?>?){
        variable = variables
    }
    var userName: String = ""
    val clientId = "30229"
    val getUserName = """
        query {
            Viewer {
                id
                name
             }
        }
    """.trimIndent()

    val searchQueryById = """
        query (${"$"}userName: String!) {
            MediaListCollection(userName: ${"$"}userName, status: CURRENT, type: ANIME) {
                lists {
                    entries {
                        progress
                        media {
                            title {
                                english
                                romaji
                            }
                            episodes
                        }
                    }
                }
            }
        }
    """.trimIndent()

    val query = """
    query (${"$"}username: String) {
  MediaListCollection(userName: ${"$"}username, type: ANIME) {
    lists {
      entries {
        progress  
        media {
          id
          title {
            romaji
            english
          }
          episodes  
          coverImage {
            large
            medium
          }
        }
      }
    }
  }
}""".trimIndent()

    val updateQuery = """mutation (${"$"}mediaId: Int, ${"$"}progress: Int) {
  SaveMediaListEntry(mediaId: ${"$"}mediaId, status: CURRENT, progress: ${"$"}progress) {
    id
    status
    progress
  }
}
"""

    val searchQuery = """query (${"$"}search: String) {
  Page(perPage: 10) {
    media(search: ${"$"}search, type: ANIME) {
      id
      title {
        romaji
        english
      }
      coverImage {
        large
      }
      status
      format
      description
    }
  }
}
"""

    fun buildApi(){
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${access_token}")
                    .build()
                chain.proceed(request)
            }
            .build()
        val retofit = Retrofit.Builder()
            .client(client)
            .baseUrl("https://graphql.anilist.co/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiSetter(retofit.create(Alapi::class.java))
        getUser()
    }
    fun getUser(){
        context.lifecycleScope.launch {
            try {
                val user = api?.getUserData(GraphQLRequest(getUserName))
                userName = user?.data?.Viewer?.name?:""
                updateVariable(mapOf(
                    "username" to "$userName"
                ))
            } catch (e: Exception) {
               Log.d("Error","AniList getUser()",e)
            }
        }
    }
    fun searchAnimeList(searchKeyword: String?): Flow<List<AnimeDetails>> = flow{
        val list = api?.getSearchAnimeList(GraphQLRequest(searchQuery,mapOf("search" to "$searchKeyword")))?.data?.Page?.media?.filter { anime->
            anime.status != "not_yet_released"
        }
        if (list != null){
            emit(list)
        }
    }.catch { e->
        Log.d("Error","AniList searchAnimeList()",e)
        emit(emptyList())
    }

    fun getAnimeById(): Flow<MediaEntries> = flow{
        var animeList = api?.getAnimeById(GraphQLRequest(searchQueryById,mapOf("userName" to userName)))?.data?.MediaListCollection?.lists?.first()
        if (animeList != null){
            emit(animeList)
        }
        else{
            Log.d("Tracker AniList","getAnimeById()")
        }
    }.catch { e->
        Log.d("Tracker AniList","in catch of getAnimeById()")
    }
    fun updateAnimeList(Id: Int,episodes: Int){
        context.lifecycleScope.launch {
            api?.updateUserList(GraphQLRequest(updateQuery,mapOf("mediaId" to Id,"progress" to episodes)))
            if (SettingsManager.notification == true){
                SettingsManager.showNotification(context,"Anime List Updated","Updated the AniList for Anime you watched")
            }
        }
    }
    fun getAniListList(): Flow<MediaList> = flow {
        getUser()
        val list =api?.query(GraphQLRequest(query,variable))?.data?.MediaListCollection?.lists?.first()
        if (list != null){
            emit(list)
        }
    }.catch { e->
        Log.d("Error", "AniList UserList",e)
        emit(MediaList(listOf()))
    }
    fun loginAnilist(){
        val authUrl = "https://anilist.co/api/v2/oauth/authorize" +
                "?client_id=$clientId" +
                "&response_type=token"

        val intent = CustomTabsIntent.Builder().build()
        intent.launchUrl(context, authUrl.toUri())
    }
    fun setAccessToken(accessToken : String){
        context.lifecycleScope.launch(Dispatchers.IO) {
            room?.setAniListToken(accessToken)
        }
        buildApi()
    }
}
interface Alapi{
    @POST("/")
    suspend fun updateUserList(
        @Body body: GraphQLRequest
    )
    @POST("/")
    suspend fun query(
        @Body body: GraphQLRequest
    ): GraphQLResponse
    @POST("/")
    suspend fun getUserData(
        @Body body: GraphQLRequest
    ): UserResponse
    @POST("/")
    suspend fun getSearchAnimeList(
        @Body body: GraphQLRequest
    ): SearchAnimeList
    @POST("/")
    suspend fun getAnimeById(
        @Body body: GraphQLRequest
    ): SearchAnimeById
}
data class GraphQLRequest(
    val query: String,
    val variables: Map<String, Any?>? = null
)
data class GraphQLResponse(
    val data: MediaListCollectionWrapper?
)
data class SearchAnimeById(
    val data: MediaListCollectionX?
)
data class MediaListCollectionX(
    val MediaListCollection: MediaListX
)
data class MediaListX(
    val lists: List<MediaEntries>
)
data class MediaEntries(
    val entries: List<MediaX>
)
data class MediaX(
    val progress: Int,
    val media: MediaY
)
data class MediaY(
    val title: MediaTitle,
    val episodes: Int
)
data class MediaListCollectionWrapper(
    val MediaListCollection: MediaListCollection
)

data class MediaListCollection(
    val lists: List<MediaList>
)

data class MediaList(
    val entries: List<MediaEntry>
)

data class MediaEntry(
    val progress: Int,
    val media: Media
)

data class Media(
    val id: Int,
    val title: MediaTitle,
    val episodes: Int,
    val coverImage: AlPicture
)
data class AlPicture(
    val large: String,
    val medium: String
)

data class MediaTitle(
    val romaji: String,
    val english: String?
)
data class UserResponse(
    val data : ViewerData
)
data class ViewerData(
    val Viewer : UserData
)
data class UserData(
    val id: Int,
    val name: String
)
data class SearchAnimeList(
    val data: AnimePage
)
data class AnimePage(
    val Page : AnimeMedia
)
data class AnimeMedia(
    val media: List<AnimeDetails>
)
data class AnimeDetails(
    val id: Int,
    val title: MediaTitle,
    val coverImage: AlPicture,
    val status: String,
    val format: String,
    val description: String
)