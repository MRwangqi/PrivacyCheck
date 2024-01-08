package com.codelang.privacycheck

import android.app.Application
import com.codelang.runtimecheck.RuntimeCheck

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        RuntimeCheck.init(this, true, "api.json")
    }
}