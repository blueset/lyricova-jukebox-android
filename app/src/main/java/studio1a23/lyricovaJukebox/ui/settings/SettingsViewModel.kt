package studio1a23.lyricovaJukebox.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import studio1a23.lyricovaJukebox.data.preference.UserPreferencesRepo
import studio1a23.lyricovaJukebox.workers.DOWNLOAD_TEST_WORKER_TAG
import studio1a23.lyricovaJukebox.workers.DownloadTestWorker
import studio1a23.lyricovaJukebox.workers.MUSIC_FILE_ID_INPUT
import studio1a23.lyricovaJukebox.workers.SYNC_WORKER_TAG
import studio1a23.lyricovaJukebox.workers.SyncWorker
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepo: UserPreferencesRepo,
    private val workManager: WorkManager
) : ViewModel() {
    var prefState = userPreferencesRepo.userPreferencesFlow

    var syncState = workManager.getWorkInfosByTagLiveData(SYNC_WORKER_TAG)
    var downloadTestState = workManager.getWorkInfosByTagLiveData(DOWNLOAD_TEST_WORKER_TAG)

    fun updateJwtToken(jwtToken: String) {
        viewModelScope.launch {
            userPreferencesRepo.updateJwtToken(jwtToken)
        }
    }

    fun requestSync() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        val syncWorkerRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .addTag(SYNC_WORKER_TAG)
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniqueWork(
            SYNC_WORKER_TAG,
            ExistingWorkPolicy.REPLACE,
            syncWorkerRequest
        )
    }

    fun requestDownloadTest() {
        val downloadTestWorkerRequest = OneTimeWorkRequestBuilder<DownloadTestWorker>()
            .setInputData(workDataOf(MUSIC_FILE_ID_INPUT to 12460))
            .addTag(DOWNLOAD_TEST_WORKER_TAG)
            .build()
        workManager.enqueueUniqueWork(
            DOWNLOAD_TEST_WORKER_TAG,
            ExistingWorkPolicy.REPLACE,
            downloadTestWorkerRequest
        )
    }

    fun resetTasks() {
        workManager.cancelAllWorkByTag(SYNC_WORKER_TAG)
        workManager.pruneWork()
    }
}
