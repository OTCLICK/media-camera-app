package com.example.mediacameraapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun CameraBottomBar(
    currentMode: CameraMode,
    onPhotoClick: () -> Unit,
    onVideoClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentMode == CameraMode.PHOTO,
            onClick = onPhotoClick,
            icon = { Icon(Icons.Default.CameraAlt, null) },
            label = { Text("Фото") }
        )

        NavigationBarItem(
            selected = currentMode == CameraMode.VIDEO,
            onClick = onVideoClick,
            icon = { Icon(Icons.Default.Videocam, null) },
            label = { Text("Видео") }
        )

        NavigationBarItem(
            selected = false,
            onClick = onGalleryClick,
            icon = { Icon(Icons.Default.PhotoLibrary, null) },
            label = { Text("Галерея") }
        )
    }
}
