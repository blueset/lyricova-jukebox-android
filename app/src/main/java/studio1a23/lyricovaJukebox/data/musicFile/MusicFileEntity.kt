package studio1a23.lyricovaJukebox.data.musicFile

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity
data class MusicFileEntity(
    @PrimaryKey val id: Int,
    val trackName: String?,
    val trackSortOrder: String?,
    val artistName: String?,
    val artistSortOrder: String?,
    val albumName: String?,
    val albumSortOrder: String?,
    /** Duration of the track in seconds. */
    val duration: Double,
    /** MD5 of the file. */
    val hash: String,
    val path: String,
)

@Dao
interface MusicFileDao {
    @Query("SELECT * FROM MusicFileEntity ORDER BY trackSortOrder ASC, artistSortOrder ASC, albumSortOrder ASC")
    fun getAllFlow(): Flow<List<MusicFileEntity>>

    @Query("SELECT * FROM MusicFileEntity ORDER BY trackSortOrder ASC, artistSortOrder ASC, albumSortOrder ASC")
    fun getAll(): List<MusicFileEntity>

    @Query("SELECT * FROM MusicFileEntity WHERE " +
            "trackName LIKE '%' || :keyword || '%' OR " +
            "artistName LIKE '%' || :keyword || '%' OR " +
            "albumName LIKE '%' || :keyword || '%' OR " +
            "trackSortOrder LIKE '%' || :keyword || '%' OR " +
            "artistSortOrder LIKE '%' || :keyword || '%' OR " +
            "albumSortOrder LIKE '%' || :keyword || '%'")
    fun searchByKeywordFlow(keyword: String): Flow<List<MusicFileEntity>>

    @Query("SELECT * FROM MusicFileEntity WHERE " +
            "trackName LIKE '%' || :keyword || '%' OR " +
            "artistName LIKE '%' || :keyword || '%' OR " +
            "albumName LIKE '%' || :keyword || '%' OR " +
            "trackSortOrder LIKE '%' || :keyword || '%' OR " +
            "artistSortOrder LIKE '%' || :keyword || '%' OR " +
            "albumSortOrder LIKE '%' || :keyword || '%'")
    fun searchByKeyword(keyword: String): List<MusicFileEntity>

    @Query("SELECT * FROM MusicFileEntity WHERE id = :id")
    fun getByIdFlow(id: Int): Flow<MusicFileEntity>

    @Query("SELECT * FROM MusicFileEntity WHERE id = :id")
    fun getById(id: Int): MusicFileEntity

    @Insert
    fun insert(vararg musicFiles: MusicFileEntity)

    @Update
    fun update(vararg musicFile: MusicFileEntity)

    @Delete
    fun delete(vararg musicFile: MusicFileEntity)
}