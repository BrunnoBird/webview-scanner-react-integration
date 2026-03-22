package com.example.webviewscannerbirdvibe.feature.mainwebview.ui

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import com.example.webviewscannerbirdvibe.feature.mainwebview.viewmodel.MainWebViewIntent
import com.example.webviewscannerbirdvibe.feature.mainwebview.viewmodel.MainWebViewViewModel

@Composable
fun MainWebViewScreen(
    viewModel: MainWebViewViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var webView: WebView? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is com.example.webviewscannerbirdvibe.feature.mainwebview.viewmodel.MainWebViewEffect.HandleBackNavigation -> {
                    if (!effect.canGoBack) {
                        onNavigateBack()
                    }
                }
            }
        }
    }

    BackHandler {
        webView?.let {
            if (it.canGoBack()) {
                viewModel.notifyBackNavigationResult(canGoBack = true)
                it.goBack()
            } else {
                viewModel.notifyBackNavigationResult(canGoBack = false)
            }
        } ?: run {
            viewModel.processIntent(MainWebViewIntent.OnBackPressed)
        }
    }

    MainWebViewContent(
        uiState = uiState,
        onWebViewCreated = { webView = it },
        onPageFinished = { viewModel.onPageFinished() },
        onPageError = { error -> viewModel.onPageError(error) }
    )
}

@Composable
private fun MainWebViewContent(
    uiState: com.example.webviewscannerbirdvibe.feature.mainwebview.viewmodel.MainWebViewState,
    onWebViewCreated: (WebView) -> Unit,
    onPageFinished: () -> Unit,
    onPageError: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (uiState.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.error ?: "Erro desconhecido",
                    color = Color.Red
                )
            }
        } else {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView, url: String) {
                                super.onPageFinished(view, url)
                                onPageFinished()
                            }

                            override fun onReceivedError(
                                view: WebView,
                                errorCode: Int,
                                description: String,
                                failingUrl: String
                            ) {
                                super.onReceivedError(view, errorCode, description, failingUrl)
                                onPageError("Não foi possível carregar a página: $description")
                            }
                        }
                        loadUrl(uiState.url)
                        onWebViewCreated(this)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
