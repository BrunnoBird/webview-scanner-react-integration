package com.example.webviewscannerbirdvibe.feature.mainwebview.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.webviewscannerbirdvibe.BuildConfig
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class MainWebViewViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        MainWebViewState(
            url = BuildConfig.WEBVIEW_BASE_URL,
            isLoading = true
        )
    )
    val uiState: StateFlow<MainWebViewState> = _uiState.asStateFlow()

    private val _effects = Channel<MainWebViewEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        processIntent(MainWebViewIntent.LoadUrl)
    }

    fun processIntent(intent: MainWebViewIntent) {
        when (intent) {
            is MainWebViewIntent.LoadUrl -> loadUrl()
            is MainWebViewIntent.OnBackPressed -> handleBackPressed()
        }
    }

    private fun loadUrl() {
        viewModelScope.launch {
            try {
                val url = BuildConfig.WEBVIEW_BASE_URL
                if (url.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        error = "URL de localhost não configurada"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        url = url,
                        isLoading = true,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erro ao carregar URL: ${e.message}"
                )
            }
        }
    }

    fun onPageFinished() {
        _uiState.value = _uiState.value.copy(isLoading = false)
    }

    fun onPageError(errorMessage: String) {
        _uiState.value = _uiState.value.copy(
            error = errorMessage,
            isLoading = false
        )
    }

    private fun handleBackPressed() {
        viewModelScope.launch {
            _effects.send(MainWebViewEffect.HandleBackNavigation(canGoBack = false))
        }
    }

    fun notifyBackNavigationResult(canGoBack: Boolean) {
        viewModelScope.launch {
            _effects.send(MainWebViewEffect.HandleBackNavigation(canGoBack = canGoBack))
        }
    }
}
