package com.codelang.checks.config

import com.android.tools.lint.detector.api.Context
import com.codelang.checks.bean.ApiNode
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.io.File
import java.lang.reflect.Type

object ApiConfig {
    private const val API_JSON = "api.json"
    private var apiNode: List<ApiNode> = arrayListOf()

    fun init(context: Context) {
        val apiJson = File(context.project.dir.absolutePath + File.separator + API_JSON)
        println(apiJson.absolutePath)
        if (apiJson.exists() && apiJson.isFile) {
            val type: Type = object : TypeToken<List<ApiNode>>() {}.type
            apiNode = Gson().fromJson(apiJson.bufferedReader(), type)
        }
    }

    fun getApiNode(): List<ApiNode> {
        return apiNode
    }
}