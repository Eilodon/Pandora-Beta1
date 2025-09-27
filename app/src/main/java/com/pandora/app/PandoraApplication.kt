package com.pandora.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PandoraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Các khởi tạo toàn cục khác sẽ ở đây
    }
}
