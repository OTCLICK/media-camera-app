package com.example.mediacameraapp.permissions

import android.Manifest
import android.os.Build

object PermissionHandler {

    fun cameraPermission() = Manifest.permission.CAMERA

    fun mediaPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}
