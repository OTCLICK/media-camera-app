package com.example.mediacameraapp.data.media

import android.net.Uri

data class MediaItem(
    val uri: Uri,
    val isVideo: Boolean,
    val dateAdded: Long? = null
)
