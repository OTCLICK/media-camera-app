package com.example.mediacameraapp.data.media

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object MediaStoreNotifier {
    private val _newMedia = MutableSharedFlow<MediaItem>(extraBufferCapacity = 10)
    val newMedia = _newMedia.asSharedFlow()

    suspend fun emit(item: MediaItem) {
        _newMedia.emit(item)
    }
}

