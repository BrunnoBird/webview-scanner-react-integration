package com.example.webviewscannerbirdvibe.di

import com.example.webviewscannerbirdvibe.feature.mainwebview.viewmodel.MainWebViewViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { MainWebViewViewModel(get()) }
}
