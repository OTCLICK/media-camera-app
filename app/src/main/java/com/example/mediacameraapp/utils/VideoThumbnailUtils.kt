package com.example.mediacameraapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun getVideoThumbnail(context: Context, uri: Uri, width: Int, height: Int): Bitmap? =
    withContext(Dispatchers.IO) {
        try {
            context.contentResolver.loadThumbnail(uri, Size(width, height), null)
        } catch (e: Exception) {
            null
        }
    }