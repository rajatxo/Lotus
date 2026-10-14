package com.rajatxo.lotus.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rajatxo.lotus.data.MusicScanner
import com.rajatxo.lotus.data.Song
import com.rajatxo.lotus.playback.PlaybackConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LotusApp() {
    val context = LocalContext.current
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }

    LaunchedEffect(Unit) {
        PlaybackConnection.connect(context)
        songs = withContext(Dispatchers.IO) { MusicScanner.scan(context) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Lotus",
            modifier = Modifier.padding(24.dp, 32.dp, 24.dp, 16.dp)
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(songs) { song ->
                SongItem(song = song, onClick = {
                    PlaybackConnection.playSong(song, songs)
                })
            }
        }
    }
}

@Composable
private fun SongItem(song: Song, onClick: () -> Unit) {
    Column(modifier = Modifier.padding(24.dp, 8.dp)) {
        Text(text = song.title)
        Text(text = song.artist)
    }
}
