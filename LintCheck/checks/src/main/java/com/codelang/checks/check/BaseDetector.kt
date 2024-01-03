package com.codelang.checks.check

import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Detector
import com.codelang.checks.config.ApiConfig

open class BaseDetector : Detector() {
    override fun beforeCheckFile(context: Context) {
        super.beforeCheckFile(context)
        ApiConfig.init(context)
        println(ApiConfig.getApiNode())
    }
}