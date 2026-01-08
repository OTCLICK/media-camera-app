package com.example.mediacameraapp.camera

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.mediacameraapp.data.media.MediaItem
import com.example.mediacameraapp.data.media.MediaStoreNotifier
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null

    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    fun startPhotoCamera(
        previewView: PreviewView,
        cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            cameraProvider?.unbindAll()

            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )

        }, ContextCompat.getMainExecutor(context))
    }

    fun takePhoto(
        onSuccess: (Uri) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat(
            "yyyy-MM-dd_HH-mm-ss",
            Locale.getDefault()
        ).format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "Pictures/MediaCameraApp"
            )
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(
                    outputFileResults: ImageCapture.OutputFileResults
                ) {
                    val savedUri = outputFileResults.savedUri ?: run {
                        val id = outputFileResults.savedUri?.lastPathSegment
                        null
                    }

                    val uriToSend = savedUri ?: Uri.EMPTY
                    try {
                        if (uriToSend != Uri.EMPTY) {
                            lifecycleOwner.lifecycleScope.launch {
                                try {
                                    MediaStoreNotifier.emit(MediaItem(uriToSend, isVideo = false))
                                } catch (_: Exception) {
                                }
                            }
                        }
                    } catch (e: Exception) {
                    }

                    onSuccess(uriToSend)
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }

    fun startVideoCamera(
        previewView: PreviewView,
        cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    ) {
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
            .build()

        videoCapture = VideoCapture.withOutput(recorder)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            cameraProvider?.unbindAll()

            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                videoCapture
            )

        }, ContextCompat.getMainExecutor(context))
    }

    fun startVideoRecording(
        onError: (Throwable) -> Unit,
        onSaved: ((Uri) -> Unit)? = null
    ) {
        val videoCapture = videoCapture ?: return

        val name = SimpleDateFormat(
            "yyyy-MM-dd_HH-mm-ss",
            Locale.getDefault()
        ).format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(
                MediaStore.Video.Media.RELATIVE_PATH,
                "Movies/MediaCameraApp"
            )
        }

        val outputOptions = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
            .setContentValues(contentValues)
            .build()

        try {
            val pendingRecording = videoCapture.output
                .prepareRecording(context, outputOptions)

            val canRecordAudio = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            val recordingBuilder = try {
                if (canRecordAudio) {
                    pendingRecording.withAudioEnabled()
                } else {
                    pendingRecording
                }
            } catch (se: SecurityException) {
                pendingRecording
            }

            recording = recordingBuilder.start(ContextCompat.getMainExecutor(context)) { event ->
                when (event) {
                    is VideoRecordEvent.Finalize -> {
                        if (event.hasError()) {
                            onError(event.cause ?: RuntimeException("Video recording error"))
                        } else {
                            val savedUri: Uri? = event.outputResults.outputUri
                            if (savedUri != null) {
                                try {
                                    lifecycleOwner.lifecycleScope.launch {
                                        try {
                                            MediaStoreNotifier.emit(MediaItem(savedUri, isVideo = true))
                                        } catch (_: Exception) {
                                        }
                                    }
                                } catch (e: Exception) {
                                }
                                onSaved?.invoke(savedUri)
                            }
                        }
                    }
                    else -> {}
                }
            }
        } catch (se: SecurityException) {
            onError(se)
        } catch (t: Throwable) {
            onError(t)
        }
    }

    fun stopVideoRecording() {
        recording?.stop()
        recording = null
    }

    fun setZoom(scaleFactor: Float) {
        val camera = camera ?: return
        val zoomState = camera.cameraInfo.zoomState.value ?: return

        val currentZoom = zoomState.zoomRatio
        val newZoom = currentZoom * scaleFactor

        val clampedZoom = newZoom.coerceIn(
            zoomState.minZoomRatio,
            zoomState.maxZoomRatio
        )

        camera.cameraControl.setZoomRatio(clampedZoom)
    }

    fun focusOnPoint(previewView: PreviewView, x: Float, y: Float) {
        val cam = camera ?: return
        try {
            val factory = previewView.meteringPointFactory
            val point = factory.createPoint(x, y)
            val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AE or FocusMeteringAction.FLAG_AF)
                .setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            cam.cameraControl.startFocusAndMetering(action)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopCamera() {
        cameraProvider?.unbindAll()
        camera = null
        imageCapture = null
        videoCapture = null
    }
}