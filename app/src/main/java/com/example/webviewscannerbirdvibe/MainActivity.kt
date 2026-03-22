package com.example.webviewscannerbirdvibe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.webviewscannerbirdvibe.feature.mainwebview.ui.MainWebViewScreen
import com.example.webviewscannerbirdvibe.ui.theme.WebviewScannerBirdVibeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WebviewScannerBirdVibeTheme {
                MainWebViewScreen(
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}