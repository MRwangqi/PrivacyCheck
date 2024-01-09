@file:JvmName("RuntimeCheck")

package com.codelang.runtimecheck

import android.content.Context
import android.util.Log
import com.codelang.runtimecheck.bean.ApiNode
import com.codelang.runtimecheck.bean.StackLog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import top.canyie.pine.Pine
import top.canyie.pine.PineConfig
import top.canyie.pine.callback.MethodHook
import java.lang.reflect.Method
import java.lang.reflect.Type

object RuntimeCheck {

    private const val ASSET_FILE = "api.json"
    const val TAG = "RuntimeCheck"

    val stackList = arrayListOf<StackLog>()

    // https://github.com/canyie/pine/blob/master/README_cn.md
    @JvmStatic
    fun init(context: Context, debug: Boolean) {
        PineConfig.debug = debug // 是否debug，true会输出较详细log
        PineConfig.debuggable = debug // 该应用是否可调试，建议和配置文件中的值保持一致，否则会出现问题

        val apiJson = getApiJson(context, ASSET_FILE)

        apiJson.forEach { apiNode ->
            try {
                val clazz = Class.forName(apiNode.clazz)
                apiNode.method?.forEach { m ->
                    val method = clazz.declaredMethods.find { it.name == m }
                    if (method == null) {
                        Log.e(TAG, "api method $m not found in class ${apiNode.clazz}")
                    } else {
                        hook(method)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun hook(method: Method) {
        Pine.hook(method, object : MethodHook() {
            override fun beforeCall(callFrame: Pine.CallFrame) {
                val stack = Log.getStackTraceString(RuntimeException("stacktrace"))
                Log.e(TAG, "Pine hook method=$method$stack")

                stackList.add(
                    0, StackLog(System.currentTimeMillis(), method.toString(), stack)
                )
            }

            override fun afterCall(callFrame: Pine.CallFrame) {}
        })
    }


    private fun getApiJson(context: Context, apiFile: String): List<ApiNode> {
        val file = context.assets.open(apiFile)
        val type: Type = object : TypeToken<List<ApiNode>>() {}.type
        return Gson().fromJson(file.bufferedReader(), type)
    }
}