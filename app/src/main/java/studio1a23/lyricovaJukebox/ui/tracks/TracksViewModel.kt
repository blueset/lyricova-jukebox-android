package studio1a23.lyricovaJukebox.ui.tracks

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import studio1a23.lyricovaJukebox.data.musicFile.MusicFileRepo
import javax.inject.Inject

@HiltViewModel
class TracksViewModel @Inject constructor(
    private val musicFileRepo: MusicFileRepo,
): ViewModel() {
    val musicFiles = musicFileRepo.getMusicFilesFlow()
    fun getMediaItems() = musicFileRepo.getMediaItems()
}