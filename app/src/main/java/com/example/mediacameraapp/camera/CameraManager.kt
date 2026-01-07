package com.example.mediacameraapp.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import android.content.ContentValues
import android.provider.MediaStore
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.camera.core.Camera
import androidx.camera.video.*

class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private var imageCapture: ImageCapture? = null,
    private var camera: Camera? = null,
    private var videoCapture: VideoCapture<Recorder>? = null,
    private var recording: Recording? = null

) {

    private var cameraProvider: ProcessCameraProvider? = null

    fun startCamera(
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

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            cameraProvider?.unbindAll()

            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                videoCapture
            )

        }, ContextCompat.getMainExecutor(context))
    }


    fun stopCamera() {
        cameraProvider?.unbindAll()
    }

    fun takePhoto(
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat(
            "yyyy-MM-dd_HH-mm-ss",
//            Locale.US
            Locale.getDefault() //думаю на локалке так оставить
        ).format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "Pictures/MediaCameraApp"
            )
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(
                    outputFileResults: ImageCapture.OutputFileResults
                ) {
                    onSuccess()
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
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

    fun startVideoRecording(
        onError: (Throwable) -> Unit
    ) {
        val videoCapture = videoCapture ?: return

        val name = SimpleDateFormat(
            "yyyy-MM-dd_HH-mm-ss",
            Locale.getDefault()
        ).format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/MediaCameraApp")
        }

        val outputOptions = MediaStoreOutputOptions
            .Builder(
                context.contentResolver,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )
            .setContentValues(contentValues)
            .build()

        recording = videoCapture.output
            .prepareRecording(context, outputOptions)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { event ->
                if (event is VideoRecordEvent.Finalize && event.hasError()) {
                    onError(event.cause ?: RuntimeException("Video recording error"))
                }
            }
    }

    fun stopVideoRecording() {
        recording?.stop()
        recording = null
    }

}
