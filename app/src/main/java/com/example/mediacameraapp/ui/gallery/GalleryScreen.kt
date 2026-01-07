package com.example.mediacameraapp.ui.gallery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mediacameraapp.data.media.MediaItem

@Composable
fun GalleryScreen(
    onOpenPhoto: () -> Unit,
    onOpenMedia: (MediaItem) -> Unit,
    viewModel: GalleryViewModel = viewModel()
) {
    val mediaItems by viewModel.mediaItems.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMedia()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(mediaItems) { item ->
                MediaGridItem(item) {
                    onOpenMedia(item)
                }
            }
        }


        IconButton(
            onClick = onOpenPhoto,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад"
            )
        }
    }
}

@Composable
fun MediaGridItem(
    item: MediaItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = item.uri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

