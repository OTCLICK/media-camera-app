package com.example.mediacameraapp.ui.viewer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mediacameraapp.ui.gallery.GalleryViewModel
import com.example.mediacameraapp.ui.gallery.GalleryViewModelFactory
import com.example.mediacameraapp.data.media.MediaItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaViewerScreen(
    startIndex: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: GalleryViewModel = viewModel(
        factory = GalleryViewModelFactory(context.contentResolver)
    )

    val mediaItems by viewModel.mediaItems.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableStateOf(startIndex) }

    LaunchedEffect(Unit) {
        if (mediaItems.isEmpty()) {
            viewModel.loadMedia()
        }
    }

    val pagerState = rememberPagerState(
        initialPage = startIndex,
        pageCount = { mediaItems.size }
    )

    Box(modifier = Modifier.fillMaxSize()) {

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val item = mediaItems[page]
            currentIndex = page

            if (item.isVideo) {
                VideoPlayer(
                    uri = item.uri.toString(),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AsyncImage(
                    model = item.uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 42.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.medium
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color.Red.copy(alpha = 0.7f),
                        shape = MaterialTheme.shapes.medium
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Удалить",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (showDeleteDialog && mediaItems.isNotEmpty()) {
            val itemToDelete: MediaItem = mediaItems[currentIndex]
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteMedia(itemToDelete) { success ->
                            showDeleteDialog = false
                            if (success) {
                                viewModel.loadMedia()
                                if (mediaItems.size == 1) onBack()
                            }
                        }
                    }) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Отмена")
                    }
                },
                title = { Text("Удалить этот файл?") },
                text = {
                    Text("Вы уверены, что хотите удалить этот медиафайл? Это действие необратимо.")
                }
            )
        }
    }
}


