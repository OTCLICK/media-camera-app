package com.example.mediacameraapp.ui.video

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mediacameraapp.camera.CameraManager


@Composable
fun VideoScreen(
    onBack: () -> Unit
) {

    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val cameraManager = remember {
        CameraManager(context, lifecycleOwner)
    }

    var hasPermissions by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = permissions.values.all { it }
    }

    val permissions = remember {
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(permissions.toTypedArray())
    }

    var isRecording by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            factory = {
                PreviewView(it).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                cameraManager.startCamera(previewView)
            }
        )

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

        FloatingActionButton(
            onClick = {
                if (isRecording) {
                    cameraManager.stopVideoRecording()
                } else {
                    cameraManager.startVideoRecording(
                        onError = { it.printStackTrace() }
                    )
                }
                isRecording = !isRecording
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            containerColor = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = if (isRecording)
                    Icons.Filled.Stop
                else
                    Icons.Filled.FiberManualRecord,
                contentDescription = "Запись видео"
            )
        }
    }
}
