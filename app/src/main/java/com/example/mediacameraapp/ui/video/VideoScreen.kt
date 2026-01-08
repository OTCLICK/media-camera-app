package com.example.mediacameraapp.ui.video

import android.Manifest
import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.mediacameraapp.camera.CameraManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive


@SuppressLint("ClickableViewAccessibility")
@Composable
fun VideoScreen(
    cameraManager: CameraManager,
    onBack: () -> Unit
) {

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
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(permissions.toTypedArray())
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraManager.stopVideoRecording()
        }
    }

    var isRecording by remember { mutableStateOf(false) }

    var elapsedMillis by remember { mutableStateOf(0L) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            val startTime = System.currentTimeMillis()
            while (isActive) {
                elapsedMillis = System.currentTimeMillis() - startTime
                delay(250L)
            }
        } else {
            elapsedMillis = 0L
        }
    }

    @SuppressLint("DefaultLocale")
    fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        if (hasPermissions) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val scaleDetector = ScaleGestureDetector(ctx, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                        override fun onScale(detector: ScaleGestureDetector): Boolean {
                            cameraManager.setZoom(detector.scaleFactor)
                            return true
                        }
                    })

                    val gestureDetector = GestureDetector(ctx, object : GestureDetector.SimpleOnGestureListener() {
                        override fun onSingleTapUp(e: MotionEvent): Boolean {
                            cameraManager.focusOnPoint(previewView, e.x, e.y)
                            return true
                        }
                    })

                    previewView.setOnTouchListener { _, event ->
                        var handled = scaleDetector.onTouchEvent(event)
                        handled = gestureDetector.onTouchEvent(event) || handled
                        true
                    }

                    previewView
                },
                modifier = Modifier.fillMaxSize(),
                update = { previewView ->
                    cameraManager.startVideoCamera(previewView, cameraSelector)
                }
            )
        }

        IconButton(
            onClick = {
                if (isRecording) {
                    cameraManager.stopVideoRecording()
                    isRecording = false
                }
                onBack()
            },
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

        IconButton(
            onClick = {
                cameraSelector =
                    if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA
                    else CameraSelector.DEFAULT_BACK_CAMERA
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

        if (isRecording) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color.Red, shape = CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = formatDuration(elapsedMillis),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        FloatingActionButton(
            onClick = {
                if (isRecording) {
                    cameraManager.stopVideoRecording()
                } else {
                    cameraManager.startVideoRecording(
                        onError = { throwable ->
                            // If recording fails, stop timer/UI
                            throwable.printStackTrace()
                        }
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
