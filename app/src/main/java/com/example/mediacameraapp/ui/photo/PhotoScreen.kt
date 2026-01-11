package com.example.mediacameraapp.ui.photo

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
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
import com.example.mediacameraapp.navigation.CameraBottomBar
import com.example.mediacameraapp.navigation.CameraMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("ClickableViewAccessibility")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoScreen(
    cameraManager: CameraManager,
    onOpenVideo: () -> Unit,
    onOpenGallery: () -> Unit
) {

    val coroutineScope = rememberCoroutineScope()
    var showFlash by remember { mutableStateOf(false) }

    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }

//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current

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

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        bottomBar = {
            CameraBottomBar(
                currentMode = CameraMode.PHOTO,
                onPhotoClick = { },
                onVideoClick = onOpenVideo,
                onGalleryClick = onOpenGallery
            )
        }) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            if (hasPermissions) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }

                        val scaleDetector = ScaleGestureDetector(
                            ctx,
                            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                                override fun onScale(detector: ScaleGestureDetector): Boolean {
                                    cameraManager.setZoom(detector.scaleFactor)
                                    return true
                                }
                            })

                        val gestureDetector = GestureDetector(
                            ctx,
                            object : GestureDetector.SimpleOnGestureListener() {
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
                    modifier = Modifier
                        .fillMaxSize(),
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
                        if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        else
                            CameraSelector.DEFAULT_BACK_CAMERA
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = MaterialTheme.shapes.medium
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Cameraswitch,
                    contentDescription = "Switch camera",
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
                            onSuccess = { uri: Uri ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Фото сохранено: ${uri}")
                                }
                            },
                            onError = { ex ->
                                ex.printStackTrace()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Ошибка при сохранении фото")
                                }
                            }
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = "Сделать фото"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

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
}
