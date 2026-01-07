package com.example.mediacameraapp.ui.viewer

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage

@Composable
fun MediaViewerScreen(
    uri: String,
    isVideo: Boolean,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        if (isVideo) {
            VideoPlayer(uri)
        } else {
            AsyncImage(
                model = Uri.parse(uri),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun VideoPlayer(uri: String) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(androidx.media3.common.MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
        }
    }

    AndroidView(
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
