package com.example.mediacameraapp.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mediacameraapp.camera.CameraManager
import com.example.mediacameraapp.ui.gallery.GalleryScreen
import com.example.mediacameraapp.ui.photo.PhotoScreen
import com.example.mediacameraapp.ui.video.VideoScreen
import com.example.mediacameraapp.ui.viewer.MediaViewerScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraManager = remember(context, lifecycleOwner) {
        CameraManager(context, lifecycleOwner)
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraManager.stopCamera()
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Photo.route
    ) {

        composable(Screen.Photo.route) {
            PhotoScreen(
                cameraManager,
                onOpenVideo = { navController.navigate(Screen.Video.route) },
                onOpenGallery = { navController.navigate(Screen.Gallery.route) }
            )
        }

        composable(Screen.Video.route) {
            VideoScreen(
                cameraManager = cameraManager,
                onOpenPhoto = { navController.navigate(Screen.Photo.route) },
                onOpenGallery = { navController.navigate(Screen.Gallery.route) }
            )
        }

        composable(Screen.Gallery.route) {
            GalleryScreen(
                onOpenPhoto = {
                    navController.navigate(Screen.Photo.route)
                },
                onOpenMedia = { item ->
                    navController.navigate(
                        Screen.MediaViewer.createRoute(
                            uri = Uri.encode(item.uri.toString()),
                            isVideo = item.isVideo
                        )
                    )
                }
            )
        }


        composable(
            route = Screen.MediaViewer.route,
            arguments = listOf(
                navArgument("uri") { type = NavType.StringType },
                navArgument("isVideo") { type = NavType.BoolType }
            )
        ) { backStackEntry ->

            val uri = backStackEntry.arguments?.getString("uri")!!
            val isVideo = backStackEntry.arguments?.getBoolean("isVideo")!!

            MediaViewerScreen(
                uri = uri,
                isVideo = isVideo,
                onBack = { navController.popBackStack() }
            )
        }

    }
}