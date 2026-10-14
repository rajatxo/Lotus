package com.rajatxo.lotus.data

import android.net.Uri

/**
 * Represents a single audio track on the device.
 */
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val uri: Uri,
    val albumArtUri: Uri?,
    val dateAdded: Long = 0L
)
