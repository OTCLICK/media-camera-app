package com.example.mediacameraapp.ui.gallery

import android.graphics.Bitmap
import android.net.Uri
import android.text.format.DateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mediacameraapp.data.media.MediaItem
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.example.mediacameraapp.utils.getVideoThumbnail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onOpenPhoto: () -> Unit,
    onOpenMedia: (Int) -> Unit
) {
    val context = LocalContext.current

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Галерея") },
                navigationIcon = {
                    IconButton(onClick = onOpenPhoto) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.6f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(4.dp)
            ) {
                itemsIndexed(mediaItems) { index, item ->
                    MediaGridItem(
                        item,
                        onClick = { onOpenMedia(index) },
                        onLongPress = { showDeleteDialog = true to item }
                    )
                }

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
                    text = { Text("Вы уверены, что хотите удалить выбранный медиафайл?") }
                )
            }
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
        if (item.isVideo) {
            VideoGridItem(
                uri = item.uri,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AsyncImage(
                model = item.uri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        if (item.isVideo) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(36.dp)
                    .background(Color.Black.copy(alpha = 0.5f), shape = MaterialTheme.shapes.large)
                    .padding(6.dp)
            )
        }

        if (item.isVideo || item.dateAdded != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
            ) {
                item.dateAdded?.let { date ->
                    val formatted = DateFormat.format("dd.MM.yyyy HH:mm", date * 1000L).toString()
                    Text(text = formatted, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun VideoGridItem(uri: Uri, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val thumbnail by produceState<Bitmap?>(initialValue = null, key1 = uri) {
        value = getVideoThumbnail(context, uri, 300, 300)
    }

    Box(
        modifier = modifier
            .background(Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        if (thumbnail != null) {
            Image(
                bitmap = thumbnail!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}