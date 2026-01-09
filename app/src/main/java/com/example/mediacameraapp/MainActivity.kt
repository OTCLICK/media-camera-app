package com.example.mediacameraapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.example.mediacameraapp.navigation.AppNavGraph
import com.example.mediacameraapp.ui.theme.MediaCameraAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MediaCameraAppTheme {
                AppNavGraph()
            }
        }
    }
}
