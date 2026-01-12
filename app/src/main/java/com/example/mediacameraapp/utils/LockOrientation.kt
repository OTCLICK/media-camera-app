//package com.example.mediacameraapp.utils
//
//import android.annotation.SuppressLint
//import android.app.Activity
//import android.content.pm.ActivityInfo
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.ui.platform.LocalContext
//
//@SuppressLint("ContextCastToActivity")
//@Composable
//fun LockOrientationPortrait() {
//    val activity = LocalContext.current as Activity
//
//    DisposableEffect(Unit) {
//        val previousOrientation = activity.requestedOrientation
//
//        activity.requestedOrientation =
//            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//
//        onDispose {
//            activity.requestedOrientation = previousOrientation
//        }
//    }
//}
