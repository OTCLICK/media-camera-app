//package com.example.mediacameraapp.utils
//
//import android.Manifest
//import android.content.Context
//import android.content.pm.PackageManager
//import android.os.Build
//import androidx.core.content.ContextCompat
//
//fun hasCameraPermission(context: Context): Boolean =
//    ContextCompat.checkSelfPermission(
//        context,
//        Manifest.permission.CAMERA
//    ) == PackageManager.PERMISSION_GRANTED
//
//fun hasAudioPermission(context: Context): Boolean =
//    ContextCompat.checkSelfPermission(
//        context,
//        Manifest.permission.RECORD_AUDIO
//    ) == PackageManager.PERMISSION_GRANTED
//
//fun hasMediaPermissions(context: Context): Boolean {
//    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//        ContextCompat.checkSelfPermission(
//            context,
//            Manifest.permission.READ_MEDIA_IMAGES
//        ) == PackageManager.PERMISSION_GRANTED &&
//                ContextCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.READ_MEDIA_VIDEO
//                ) == PackageManager.PERMISSION_GRANTED
//    } else {
//        ContextCompat.checkSelfPermission(
//            context,
//            Manifest.permission.READ_EXTERNAL_STORAGE
//        ) == PackageManager.PERMISSION_GRANTED
//    }
//}
