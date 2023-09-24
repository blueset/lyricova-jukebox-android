package studio1a23.lyricovaJukebox.ui.tracks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import studio1a23.lyricovaJukebox.data.musicFile.MusicFileEntity
import studio1a23.lyricovaJukebox.data.musicFile.toMediaItem
import studio1a23.lyricovaJukebox.services.MediaItemIds
import studio1a23.lyricovaJukebox.ui.player.PlayerViewModel
import java.text.Normalizer

@Composable
fun TrackItem(musicFile: MusicFileEntity, onClick: () -> Unit = {}) {
    ListItem(
        headlineContent = {
            Text(
                musicFile.trackName ?: "Unknown track",
            )
        },
        supportingContent = {
            Text(
                "${musicFile.artistName ?: "Unknown artist"} / ${musicFile.albumName ?: "Unknown Album"}",
            )
        },
        modifier = Modifier
            .clickable(onClick = onClick)
    )
    HorizontalDivider()
}

@Composable
fun TrackHeader(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.surfaceBright)
            .padding(16.dp, 4.dp)
            .fillMaxWidth()
    ) {
        Text(label)
    }
}

fun getTrackHeaderLabel(musicFile: MusicFileEntity): String {
    return musicFile.trackSortOrder?.let {
        Normalizer.normalize(it, Normalizer.Form.NFD)
    }?.firstOrNull()?.uppercaseChar()?.let { char ->
        when {
            char.isLetter() -> char.uppercaseChar().toString()
            else -> "#"
        }
    } ?: "?"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SectionSelector(
    openState: MutableState<Boolean>,
    skipToIndex: Map<String, Int>,
    listState: LazyListState
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    if (openState.value) {
        AlertDialog(
            onDismissRequest = { openState.value = false },
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
        ) {
            Surface(
                modifier = Modifier
                    .width(((56 + 8) * 5 - 4).dp)
                    .wrapContentHeight()
                    .padding(0.dp, 12.dp),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                FlowRow(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(scrollState)
                ) {
                    skipToIndex.forEach { (label, index) ->
                        FilledTonalButton(
                            modifier = Modifier
                                .width(56.dp)
                                .height(56.dp)
                                .padding(4.dp),
                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(0.dp),
                            onClick = {
                                openState.value = false
                                coroutineScope.launch {
                                    listState.scrollToItem(index)
                                }
                            }
                        ) {
                            Text(label)
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun SectionSelectorPreview() {
    val openSelectorDialog = remember { mutableStateOf(true) }
    val listState = rememberLazyListState()
    val groupHeaderIndexes = mapOf(
        "A" to 0,
        "B" to 1,
        "C" to 2,
        "D" to 3,
        "E" to 4,
        "F" to 5,
        "G" to 6,
        "H" to 7,
        "I" to 8,
        "J" to 9,
        "K" to 10,
        "L" to 11,
        "M" to 12,
        "N" to 13,
        "O" to 14,
        "P" to 15,
        "Q" to 16,
        "R" to 17,
        "S" to 18,
        "T" to 19,
        "U" to 20,
        "V" to 21,
        "W" to 22,
        "X" to 23,
        "Y" to 24,
        "Z" to 25,
        "#" to 26,
        "あ" to 27, "い" to 28, "う" to 29, "え" to 30, "お" to 31,
        "か" to 32, "き" to 33, "く" to 34, "け" to 35, "こ" to 36,
        "さ" to 37, "し" to 38, "す" to 39, "せ" to 40, "そ" to 41,
        "た" to 42, "ち" to 43, "つ" to 44, "て" to 45, "と" to 46,
        "な" to 47, "に" to 48, "ぬ" to 49, "ね" to 50, "の" to 51,
        "は" to 52, "ひ" to 53, "ふ" to 54, "へ" to 55, "ほ" to 56,
        "ま" to 57, "み" to 58, "む" to 59, "め" to 60, "も" to 61,
        "や" to 62, "ゆ" to 63, "よ" to 64,
        "ら" to 65, "り" to 66, "る" to 67, "れ" to 68, "ろ" to 69,
        "わ" to 70, "を" to 71, "ん" to 72,
        "が" to 73, "ぎ" to 74, "ぐ" to 75, "げ" to 76, "ご" to 77,
        "ざ" to 78, "じ" to 79, "ず" to 80, "ぜ" to 81, "ぞ" to 82,
        "だ" to 83, "ぢ" to 84, "づ" to 85, "で" to 86, "ど" to 87,
    )
    SectionSelector(openSelectorDialog, groupHeaderIndexes, listState)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tracks(
    modifier: Modifier = Modifier,
    viewModel: TracksViewModel = hiltViewModel<TracksViewModel>(),
    playerViewModel: PlayerViewModel = hiltViewModel<PlayerViewModel>(),
) {
    val context = LocalContext.current
    val openSelectorDialog = remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val musicFiles = viewModel.musicFiles.collectAsState(initial = emptyList())
    val groups =
        musicFiles.value.sortedBy { getTrackHeaderLabel(it) }.groupBy { getTrackHeaderLabel(it) }
    val groupKeys = groups.keys.toList()
    val groupHeaderIndexes = groupKeys.foldIndexed(mutableMapOf<String, Int>()) { index, map, key ->
        when (index) {
            0 -> map.apply { this[key] = 0 }
            else -> map.apply {
                this[key] =
                    (map.getValue(groupKeys[index - 1]) + groups.getValue(groupKeys[index - 1]).size) + 1
            }
        }
    }

    LazyColumn(modifier, state = listState) {
        groups.forEach { (label, musicFiles) ->
            stickyHeader {
                TrackHeader(label, modifier = Modifier.clickable {
                    openSelectorDialog.value = true
                })
            }
            items(musicFiles) { musicFile ->
                TrackItem(musicFile, onClick = {
                    musicFile.toMediaItem("${MediaItemIds.SONG}/")?.let {
                        playerViewModel.playFromItem(it)
                    }
                })
            }
        }
    }

    SectionSelector(openSelectorDialog, groupHeaderIndexes, listState)
}