package com.stefan.simpleRouter

import android.app.Application
import com.stefan.router.SimpleRouter

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        SimpleRouter.init(this)
    }
}