package com.velaphi.fitnesstracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class FitnessTrackerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // use Debug
        if (packageManager.getApplicationInfo(packageName, 0).flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            Timber.plant(Timber.DebugTree())
        }
    }
} 