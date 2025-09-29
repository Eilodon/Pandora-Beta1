package com.pandora.app

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.perf.FirebasePerformance
import timber.log.Timber
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PandoraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // FIXED: init Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        // FIXED: guarded Firebase init (skip safely if misconfigured)
        try {
            if (BuildConfig.ENABLE_FIREBASE && FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
            if (BuildConfig.ENABLE_FIREBASE) {
                FirebasePerformance.getInstance().isPerformanceCollectionEnabled = BuildConfig.ENABLE_PERF
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Timber.w("Firebase init skipped: ${e.message}")
        }
        // Các khởi tạo toàn cục khác sẽ ở đây
    }
}
