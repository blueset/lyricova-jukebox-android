package studio1a23.lyricovaJukebox.data.preference

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.Date
import javax.inject.Inject

class UserPreferencesDataSource @Inject constructor(private val dataStore: DataStore<Preferences>) {

    val userData = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map {
            UserPreferences(
                jwtToken = it[PreferencesKeys.JWT_TOKEN] ?: "",
                lastSynced = it[PreferencesKeys.LAST_SYNCED]?.let { it1 -> Date(it1) },
            )
        }

    suspend fun updateJwtToken(jwtToken: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.JWT_TOKEN] = jwtToken
        }
    }

    suspend fun updateLastSynced(lastSynced: Date) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SYNCED] = lastSynced.time
        }
    }
}