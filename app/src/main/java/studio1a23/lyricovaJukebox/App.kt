package studio1a23.lyricovaJukebox

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import dagger.hilt.android.HiltAndroidApp
import studio1a23.lyricovaJukebox.workers.SYNC_NOTIFICATION_CHANNEL
import studio1a23.lyricovaJukebox.workers.SYNC_WORKER_TAG
import studio1a23.lyricovaJukebox.workers.SyncWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

    override fun onCreate() {
        super.onCreate()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .build()
        val syncWorkerRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            7, TimeUnit.DAYS, // repeatInterval (the period cycle)
            1, TimeUnit.DAYS
        ) // flexInterval
            .setConstraints(constraints)
            .addTag(SYNC_WORKER_TAG)
            .build()
        // WorkManager.getInstance(applicationContext).enqueue(syncWorkerRequest)
        registerNotificationChannels()
    }

    private fun registerNotificationChannels() {
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(NotificationChannel(
            SYNC_NOTIFICATION_CHANNEL,
            getString(R.string.sync_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ))
    }
}
