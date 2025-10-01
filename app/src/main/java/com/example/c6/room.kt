package com.example.c6

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class room(private val context: ComponentActivity){
    val db = Room.databaseBuilder(context, Db::class.java,"db").fallbackToDestructiveMigration().build()
    val folderDao = db.folderDao()
    val tokenDao = db.getToken()
    val homeFilter = db.homeFilter()
    fun addFolder(uri: String){
        context.lifecycleScope.launch {
            folderDao.addFolder(Folder(folderUri = uri, malId = 0, aniId = 0, name = null))
        }
    }
    fun updateSortBy(sort: String){
        context.lifecycleScope.launch {
            homeFilter.updateSortBy(sort)
        }
    }
    fun getSortBy(): Flow<HomeFilter>{
        return homeFilter.getSortBy()
    }
    fun getFolderList(): Flow<List<Folder>> {
        return folderDao.display()
    }
    fun getSortedByNameList(): Flow<List<Folder>> {
        return folderDao.getSortedByName()
    }
    fun getSortedByVideoList(): Flow<List<Folder>> {
        return folderDao.getSortedByVideo()
    }
    fun getSortedByTimeList(): Flow<List<Folder>> {
        return folderDao.getSortedByTime()
    }
    @SuppressLint("SuspiciousIndentation")
    fun updateNameVideo(size: Int?, name: String?, uri: String?){
        if (size!=null)
        context.lifecycleScope.launch {
            folderDao.updateNameandVideo(name,size,uri)
        }
    }
    fun updateLastUsed(time: Long,uri: String?){
        context.lifecycleScope.launch {
            folderDao.updateLastUsed(time,uri)
        }
    }
    fun updateMalAnimeId(animeId: Int, uri: String){
        context.lifecycleScope.launch {
            folderDao.updateMalId(animeId,uri)
        }
    }
    fun updateAniAnimeId(animeId: Int,uri: String){
        context.lifecycleScope.launch {
            folderDao.updateAniId(animeId,uri)
        }
    }
    fun setMalApiAccessToken(token: String){
        context.lifecycleScope.launch(Dispatchers.IO) {
            tokenDao.updateMalApiAccessToken(token)
        }
    }
    fun setMalApiRefreshToken(token: String){
        context.lifecycleScope.launch(Dispatchers.IO) {
            tokenDao.updateMalApiRefreshToken(token)
        }
    }
    fun setAniListToken(token: String){
        context.lifecycleScope.launch(Dispatchers.IO) {
            tokenDao.updateAniListToken(token)
        }
    }
    suspend fun getMalApiAccessToken(): String?{
        return token.first().malApiAccessToken
    }
    suspend fun getMalApiRefreshToken(): String?{
        return token.first().malApiRefreshToken
    }
    suspend fun getAniListToken(): String?{
        return token.first().aniListToken
    }
    val token = tokenDao.display()
}
@Entity
data class Folder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val folderUri: String? = null,
    val malId: Int,
    val aniId: Int,
    val name: String?,
    val videos: Int = -1,
    val lastUsed: Long = System.currentTimeMillis()
)
@Entity(tableName = "tokens")
data class Token(
    @PrimaryKey val id: Int = 0,
    val malApiAccessToken : String? = null,
    val malApiRefreshToken: String? = null,
    val aniListToken: String? = null
)
@Entity(tableName = "HomeFilter")
data class HomeFilter(
    @PrimaryKey val id: Int = 0,
    val sortBy: String = "Name"
)

@Dao
interface Home{
    @Insert
    suspend fun addRow(homeFilter: HomeFilter)
    @Query("update homefilter set sortBy = :sort where id = 0")
    suspend fun updateSortBy(sort: String)
    @Query("select * from homefilter where id = 0")
    fun getSortBy(): Flow<HomeFilter>
}
@Dao
interface FolderDao{
    @Insert
    suspend fun addFolder(folder: Folder)
    @Query("select * from Folder")
    fun display(): Flow<List<Folder>>
    @Query("SELECT EXISTS(SELECT 1 FROM folder WHERE folderUri = :uri)")
    suspend fun containsUri(uri: String): Boolean
    @Query("update folder set malId = :Id where folderUri = :uri")
    suspend fun updateMalId(Id: Int, uri: String)
    @Query("update folder set aniId = :Id where folderUri = :uri")
    suspend fun updateAniId(Id: Int, uri: String)
    @Query("select * from folder where folderUri = :uri")
    suspend fun getAnimeIds(uri: String): Folder
    @Query("update folder set name = :folderName,videos = :size  where folderUri = :uri")
    suspend fun updateNameandVideo(folderName: String?,size: Int,uri: String?)
    @Query("update folder set lastUsed = :time where folderUri = :uri")
    suspend fun updateLastUsed(time: Long,uri: String?)
    @Query("select * from folder order by name")
    fun getSortedByName(): Flow<List<Folder>>
    @Query("select * from folder order by videos")
    fun getSortedByVideo(): Flow<List<Folder>>
    @Query("select * from folder order by lastUsed desc")
    fun getSortedByTime(): Flow<List<Folder>>
}

@Dao
interface GetToken{
    @Insert(onConflict = IGNORE)
    suspend fun insertRow(token: Token)
    @Query("update tokens set malApiAccessToken = :token where id = 0")
    suspend fun updateMalApiAccessToken(token: String)

    @Query("update tokens set malApiRefreshToken = :token where id = 0")
    suspend fun updateMalApiRefreshToken(token: String)

    @Query("update tokens set aniListToken = :token where id = 0")
    suspend fun updateAniListToken(token: String)

    @Query("select * from tokens where id = 0")
    fun display(): Flow<Token>

    @Query("select count(*) from tokens")
    suspend fun count(): Int
}
@Database(entities = [Folder::class,Token::class, HomeFilter::class], version = 6)
abstract class Db: RoomDatabase(){
    abstract fun getToken(): GetToken
    abstract fun folderDao(): FolderDao
    abstract fun homeFilter(): Home
}