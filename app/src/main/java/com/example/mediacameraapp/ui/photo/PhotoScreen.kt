package com.example.mediacameraapp.ui.photo

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.mediacameraapp.camera.CameraManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PhotoScreen(
    cameraManager: CameraManager,
    onOpenVideo: () -> Unit,
    onOpenGallery: () -> Unit
) {

    val coroutineScope = rememberCoroutineScope()
    var showFlash by remember { mutableStateOf(false) }

    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermissions by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = permissions.values.all { it }
    }

    val permissions = remember {
        mutableListOf(
            Manifest.permission.CAMERA
        ).apply {
            add(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(permissions.toTypedArray())
    }

    Box(modifier = Modifier.fillMaxSize()) {

        if (hasPermissions) {
            AndroidView(
                factory = {
                    PreviewView(it).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            cameraManager.setZoom(zoom)
                        }
                    },
                update = { previewView ->
                    cameraManager.startPhotoCamera(
                        previewView = previewView,
                        cameraSelector = cameraSelector
                    )
                }
            )
        }

        IconButton(
            onClick = {
                cameraSelector =
                    if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Cameraswitch,
                contentDescription = "Переключить камеру",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        showFlash = true
                        delay(100)
                        showFlash = false
                    }

                    cameraManager.takePhoto(
                        onSuccess = {},
                        onError = { it.printStackTrace() }
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = "Сделать фото"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = onOpenVideo) {
                Text("Видео")
            }

            Button(onClick = onOpenGallery) {
                Text("Галерея")
            }
        }

        if (showFlash) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.8f))
            )
        }
    }
}
