package studio1a23.lyricovaJukebox.ui.settings

import android.content.res.Configuration
import android.icu.text.MessageFormat
import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.work.WorkInfo
import studio1a23.lyricovaJukebox.R
import studio1a23.lyricovaJukebox.data.preference.UserPreferences
import studio1a23.lyricovaJukebox.ui.theme.JukeboxTheme
import studio1a23.lyricovaJukebox.util.getJwtExpiryDate
import java.util.Date


@Composable
fun SettingsListItem(
    modifier: Modifier = Modifier,
    headlineContent: @Composable () -> Unit = {},
    supportingContent: @Composable () -> Unit = {},
    onClick: () -> Unit = {},
) {
    ListItem(
        headlineContent = headlineContent,
        supportingContent = supportingContent,
        modifier = modifier.clickable(onClick = onClick),
    )
    HorizontalDivider()
}

@Composable
fun JwtSetting(jwtToken: String, updateToken: (String) -> Unit = {}) {
    val openDialog = remember { mutableStateOf(false) }
    var tokenVal by remember { mutableStateOf(jwtToken) }
    val expiry: String? = if (jwtToken != "") {
        val expiry = getJwtExpiryDate(jwtToken)
        if (expiry !== null) DateUtils.getRelativeTimeSpanString(
            expiry.time,
            System.currentTimeMillis(),
            DateUtils.DAY_IN_MILLIS
        ).toString() else null
    } else null

    SettingsListItem(
        headlineContent = { Text(stringResource(R.string.JwtToken)) },
        supportingContent = {
            if (jwtToken === "") {
                Text(
                    stringResource(R.string.JwtTokenNotSet),
                    fontStyle = FontStyle.Italic,
                )
            } else if (expiry === null) {
                Text(
                    stringResource(R.string.JwtTokenInvalid),
                    fontStyle = FontStyle.Italic,
                )
            } else {
                Text(stringResource(R.string.JwtTokenExpiry, expiry))
            }
        },
        onClick = {
            tokenVal = jwtToken
            openDialog.value = true
        }
    )
    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.JwtToken)) },
            confirmButton = {
                TextButton(onClick = {
                    updateToken(tokenVal)
                    openDialog.value = false
                }) {
                    Text(stringResource(R.string.Save))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    tokenVal = jwtToken
                    openDialog.value = false
                }) {
                    Text(stringResource(R.string.Cancel))
                }
            },
            text = {
                OutlinedTextField(
                    value = tokenVal,
                    onValueChange = { tokenVal = it },
                )
            },
        )
    }
}

@Composable
fun SyncSettings(
    lastSynced: Date? = null,
    syncState: List<WorkInfo>? = null,
    sync: () -> Unit = {},
) {
    SettingsListItem(
        headlineContent = { Text(stringResource(R.string.SyncData)) },
        supportingContent = {
            Text(when {
                syncState?.any { it.state == WorkInfo.State.RUNNING } ?: false -> "Syncing..."
                syncState?.any { it.state == WorkInfo.State.ENQUEUED }
                    ?: false -> "Sync in queue..."

                syncState?.any { it.state == WorkInfo.State.FAILED } ?: false -> "Sync failed..."

                syncState?.any { it.state == WorkInfo.State.BLOCKED } ?: false -> "Sync blocked..."

                lastSynced !== null -> "Last synced: ${
                    DateUtils.getRelativeTimeSpanString(
                        lastSynced.time,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    )
                }"

                else -> "Never synced."
            })
        },
        onClick = {
            sync()
        }
    )
}

@Composable
fun Settings(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel<SettingsViewModel>(),
) {
    val perfState by settingsViewModel.prefState.collectAsState(UserPreferences())
    val syncState by settingsViewModel.syncState.observeAsState()
    val downloadTestState by settingsViewModel.downloadTestState.observeAsState()
    LazyColumn(modifier) {
        item {
            JwtSetting(
                jwtToken = perfState.jwtToken,
                updateToken = { token -> settingsViewModel.updateJwtToken(token) }
            )
        }
        item {
            SyncSettings(
                lastSynced = perfState.lastSynced,
                syncState = syncState,
                sync = { settingsViewModel.requestSync() }
            )
        }
        item {
            SettingsListItem(
                headlineContent = { Text("Reset tasks") },
                onClick = {
                    settingsViewModel.resetTasks()
                }
            )
        }
        item {
            SettingsListItem(
                headlineContent = { Text("Download test task") },
                supportingContent = {
                    Text(
                        downloadTestState?.joinToString(", ") { it.state.toString() }?.ifBlank { "0 tasks." }
                            ?: "No task."
                    )
                },
                onClick = {
                    settingsViewModel.requestDownloadTest()
                }
            )
        }
    }
}

@Preview
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
@UnstableApi
fun SettingsPreview() {
    JukeboxTheme(Color.Unspecified) {
        Column {
            ListItem(
                headlineContent = { Text(MessageFormat.format("{0, plural, one {# file} other {# files}} processed, {1, plural, one {# file} other {# files}} downloaded.", 10, 1)) },
            )
        }
    }
}