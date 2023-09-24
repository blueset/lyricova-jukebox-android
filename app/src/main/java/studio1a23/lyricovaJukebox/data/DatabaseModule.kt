package studio1a23.lyricovaJukebox.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import studio1a23.lyricovaJukebox.data.musicFile.MusicFileDao
import javax.inject.Singleton

const val JUKEBOX_DATABASE_NAME = "jukebox_database"

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            JUKEBOX_DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideMusicFileDao(appDatabase: AppDatabase): MusicFileDao {
        return appDatabase.musicFileDao()
    }
}