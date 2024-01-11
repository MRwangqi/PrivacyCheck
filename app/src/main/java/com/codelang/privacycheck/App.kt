package com.codelang.privacycheck

import android.app.Application
import com.codelang.fridacheck.FridaManager
import com.codelang.runtimecheck.RuntimeCheck

class App : Application() {


    companion object{
        val runtimeStackLog = arrayListOf<com.codelang.runtimecheck.bean.StackLog>()
        val fridaStackLog = arrayListOf<com.codelang.fridacheck.bean.StackLog>()
    }


    override fun onCreate() {
        super.onCreate()

        initRuntimeCheck()
        initFridaCheck()
    }

    private fun initRuntimeCheck() {
        RuntimeCheck.init(this, true)
        RuntimeCheck.registerStackLog(object : com.codelang.runtimecheck.StackLogListener {
            override fun onStackLog(stackLog: com.codelang.runtimecheck.bean.StackLog) {
                runtimeStackLog.add(0,stackLog)
            }
        })
    }

    private fun initFridaCheck() {
        FridaManager.init(this, true)
        FridaManager.registerStackLog(object : com.codelang.fridacheck.StackLogListener {
            override fun onStackLog(stackLog: com.codelang.fridacheck.bean.StackLog) {
                fridaStackLog.add(0,stackLog)
            }
        })
    }
}