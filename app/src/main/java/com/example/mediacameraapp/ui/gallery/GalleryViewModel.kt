package com.example.mediacameraapp.ui.gallery

import android.content.ContentResolver
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediacameraapp.data.media.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.net.Uri

class GalleryViewModel(
    private val contentResolver: ContentResolver
) : ViewModel() {

    private val _mediaItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val mediaItems: StateFlow<List<MediaItem>> = _mediaItems

    fun loadMedia() {
        viewModelScope.launch(Dispatchers.IO) {
            val items = mutableListOf<MediaItem>()

            val imageCursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Images.Media._ID),
                null,
                null,
                "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )

            imageCursor?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val uri = Uri.withAppendedPath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id.toString()
                    )
                    items.add(MediaItem(uri, isVideo = false))
                }
            }

            val videoCursor = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Video.Media._ID),
                null,
                null,
                "${MediaStore.Video.Media.DATE_ADDED} DESC"
            )

            videoCursor?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val uri = Uri.withAppendedPath(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id.toString()
                    )
                    items.add(MediaItem(uri, isVideo = true))
                }
            }

            _mediaItems.value = items
        }
    }
}
