package studio1a23.lyricovaJukebox.workers

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerSingletonModule {
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext appContext: Context): WorkManager =
        WorkManager.getInstance(appContext)
}

//@Module
//@InstallIn(ViewModelComponent::class)
//object WorkerViewModelModule {
//    @Provides
//    @ViewModelScoped
//    fun provideWorkManager(@ApplicationContext appContext: Context): WorkManager =
//        WorkManager.getInstance(appContext)
//}