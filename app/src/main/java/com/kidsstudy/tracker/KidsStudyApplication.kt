package com.kidsstudy.tracker

import android.app.Application
import com.kidsstudy.tracker.data.AppDatabase

class KidsStudyApplication : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
    }
}
