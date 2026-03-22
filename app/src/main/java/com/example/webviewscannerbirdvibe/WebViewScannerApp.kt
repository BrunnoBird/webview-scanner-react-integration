package com.example.webviewscannerbirdvibe

import android.app.Application
import com.example.webviewscannerbirdvibe.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class WebViewScannerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@WebViewScannerApp)
            modules(appModule)
        }
    }
}
