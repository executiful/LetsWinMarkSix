package com.cmlee.executiful.letswinmarksix

import android.app.Application
import com.cmlee.executiful.letswinmarksix.helper.AppContainer

class App : Application() {
    lateinit var appContainer: AppContainer
        private set
    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}