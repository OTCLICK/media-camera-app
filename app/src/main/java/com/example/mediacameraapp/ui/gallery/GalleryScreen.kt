package com.example.mediacameraapp.ui.gallery

import android.text.format.DateFormat
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun GalleryScreen(
    onOpenPhoto: () -> Unit,
    onOpenMedia: (MediaItem) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val viewModel: GalleryViewModel = viewModel(
        factory = GalleryViewModelFactory(
            contentResolver = context.contentResolver
        )
    )

    val mediaItems by viewModel.mediaItems.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<Pair<Boolean, MediaItem?>>(false to null) }

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
                MediaGridItem(item,
                    onClick = { onOpenMedia(item) },
                    onLongPress = {
                        showDeleteDialog = true to item
                    }
                )
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

        if (showDeleteDialog.first && showDeleteDialog.second != null) {
            val itemToDelete = showDeleteDialog.second!!
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false to null },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteMedia(itemToDelete) { success ->
                            showDeleteDialog = false to null
                            if (success) viewModel.loadMedia()
                        }
                    }) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false to null }) {
                        Text("Отмена")
                    }
                },
                title = { Text("Удалить этот файл?") },
                text = { Text("Вы уверены, что хотите удалить выбранный медиафайл? Это действие необратимо.") }
            )
        }
    }
}

@Composable
fun MediaGridItem(
    item: MediaItem,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongPress() }
                )
            }
    ) {
        AsyncImage(
            model = item.uri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (item.isVideo || item.dateAdded != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
            ) {
                if (item.isVideo) {
                    Text(text = "Видео", color = androidx.compose.ui.graphics.Color.White)
                }
                item.dateAdded?.let { date ->
                    val formatted = DateFormat.format("dd.MM.yyyy HH:mm", date * 1000L).toString()
                    Text(text = formatted, color = androidx.compose.ui.graphics.Color.White)
                }
            }
        }
    }
}
