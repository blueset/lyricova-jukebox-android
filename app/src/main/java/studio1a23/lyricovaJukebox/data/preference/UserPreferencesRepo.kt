package studio1a23.lyricovaJukebox.data.preference

import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

data class UserPreferences(
    var jwtToken: String = "",
    var lastSynced: Date? = null,
)

object PreferencesKeys {
    val JWT_TOKEN = stringPreferencesKey("jwt_token")
    val LAST_SYNCED = longPreferencesKey("last_synced")
}

class UserPreferencesRepo @Inject constructor (
    private val dataSource: UserPreferencesDataSource,
) {
    val userPreferencesFlow: Flow<UserPreferences> = dataSource.userData

    suspend fun updateJwtToken(jwtToken: String) {
        dataSource.updateJwtToken(jwtToken)
    }

    suspend fun updateLastSynced(lastSynced: Date) {
        dataSource.updateLastSynced(lastSynced)
    }
}