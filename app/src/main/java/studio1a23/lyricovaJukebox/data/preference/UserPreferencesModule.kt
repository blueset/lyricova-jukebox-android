package studio1a23.lyricovaJukebox.data.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

const val USER_PREFERENCES_NAME = "user_preferences"
val Context.dataStore by preferencesDataStore(name = USER_PREFERENCES_NAME)

@Module
@InstallIn(SingletonComponent::class)
//@InstallIn(ViewModelComponent::class)
abstract class UserPreferencesModule {

    // binds instance of UserPreferencesDataSource
//    @Binds
//    @Singleton
//    abstract fun bindUserPrefDataSource(
//        userPreferencesDataSource: UserPreferencesDataSource
//    ): UserPreferencesDataSource

    companion object {

        // provides instance of DataStore
        @Provides
        @Singleton
//        @ViewModelScoped
        fun provideUserDataStorePreferences(
            @ApplicationContext applicationContext: Context
        ): DataStore<Preferences> {
            return applicationContext.dataStore
        }
    }
}