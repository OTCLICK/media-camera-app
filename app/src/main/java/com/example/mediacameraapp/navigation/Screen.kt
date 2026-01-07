package com.example.mediacameraapp.navigation

sealed class Screen(val route: String) {
    object Photo : Screen("photo")
    object Video : Screen("video")
    object Gallery : Screen("gallery")

    object MediaViewer : Screen("media_viewer/{uri}/{isVideo}") {
        fun createRoute(uri: String, isVideo: Boolean) =
            "media_viewer/$uri/$isVideo"
    }
}
