package studio1a23.lyricovaJukebox.data

import androidx.room.Database
import androidx.room.RoomDatabase
import studio1a23.lyricovaJukebox.data.musicFile.MusicFileDao
import studio1a23.lyricovaJukebox.data.musicFile.MusicFileEntity

@Database(entities = [MusicFileEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun musicFileDao(): MusicFileDao
}