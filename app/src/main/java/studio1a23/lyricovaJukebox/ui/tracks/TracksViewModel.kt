package studio1a23.lyricovaJukebox.ui.tracks

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import studio1a23.lyricovaJukebox.data.musicFile.MusicFileRepo
import javax.inject.Inject

@HiltViewModel
class TracksViewModel @Inject constructor(
    private val musicFileRepo: MusicFileRepo,
) : ViewModel() {
    val musicFiles = musicFileRepo.getMusicFilesFlow()
    val groupedMusicFiles = musicFiles.map { musicFiles ->
        musicFiles.sortedBy { getTrackHeaderLabel(it) }.groupBy { getTrackHeaderLabel(it) }
    }.flowOn(Dispatchers.IO)

    fun getMediaItems() = musicFileRepo.getMediaItems()
}