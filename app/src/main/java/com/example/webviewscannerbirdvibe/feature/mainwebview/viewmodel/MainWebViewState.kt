package com.example.webviewscannerbirdvibe.feature.mainwebview.viewmodel

// MVI — Intent (eventos da UI)
sealed class MainWebViewIntent {
    data object LoadUrl : MainWebViewIntent()
    data object OnBackPressed : MainWebViewIntent()
}

// MVI — Effect (efeitos colaterais únicos)
sealed class MainWebViewEffect {
    data class HandleBackNavigation(val canGoBack: Boolean) : MainWebViewEffect()
}

// MVI — State (estado único da tela)
data class MainWebViewState(
    val url: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)
