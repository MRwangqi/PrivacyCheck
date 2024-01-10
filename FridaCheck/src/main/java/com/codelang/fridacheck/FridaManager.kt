package com.codelang.fridacheck

import android.app.Application
import android.content.Context
import android.util.Log
import com.codelang.fridacheck.bean.StackLog
import java.io.File
import java.io.FileOutputStream
import java.lang.RuntimeException

object FridaManager {

    private const val TAG = "FridaManager"

    private const val JS_FILE_NAME = "script.js"
    private const val GADGET_FILE_NAME = "libgadget"
    private const val API_JSON_FILE_NAME = "api.json"

    private var context: Application? = null

    private var stackLogList = arrayListOf<StackLog>()


    // https://blog.51cto.com/u_15127527/4546627
    // https://frida.re/docs/frida-cli/
    @JvmStatic
    fun init(context: Application) {
        this.context = context
        writeJSToCacheFile(context)
        writeGadgetToCacheFile(context)
        loadGadget(context)
    }


    /**
     *  写入 js 文件到 cache
     */
    @JvmStatic
    private fun writeJSToCacheFile(context: Context) {
        val jsPath = context.cacheDir.absolutePath + File.separator + JS_FILE_NAME

        // 避免覆盖安装时，file 未更新
        val file = File(jsPath)
        if (file.exists()) file.delete()
        file.createNewFile()

        context.assets.open(JS_FILE_NAME).use { inputStream ->
            FileOutputStream(file).use { fos ->
                fos.write(inputStream.readBytes())
            }
        }
    }

    /**
     * 写入 gadget 文件到 cache
     */
    @JvmStatic
    private fun writeGadgetToCacheFile(context: Context) {
        // write gadget to cache
        val gadgetPath = context.cacheDir.absolutePath + File.separator + GADGET_FILE_NAME + ".so"
        val gadgetFile = File(gadgetPath)
        if (gadgetFile.exists()) gadgetFile.delete() // 避免覆盖安装时，file 未更新
        gadgetFile.createNewFile()
        is64bit(context).let {
            context.assets.open(GADGET_FILE_NAME + if (it) "_64" else "_32")
                .use { inputStream ->
                    FileOutputStream(gadgetPath).use { fos ->
                        fos.write(inputStream.readBytes())
                    }
                }
        }

        // write gadget config to cache
        val configFile =
            File(context.cacheDir.absolutePath + File.separator + GADGET_FILE_NAME + ".config.so")
        if (configFile.exists()) configFile.delete() // 避免覆盖安装时，file 未更新
        configFile.createNewFile()
        val jsPath = context.cacheDir.absolutePath + File.separator + JS_FILE_NAME
        val json = """
           {
             "interaction": {
               "type": "script",
               "on_change": "reload",
               "path": "$jsPath"
             }
           }
        """.trimIndent()
        configFile.writeText(json)
    }

    /**
     * 加载 gadget so 库
     *
     * 这里的 so 走本地路径而不是放置在 lib/armeabi/ 目录，是因为 config.so 使用的 script，path 没办法指定路径
     */
    @JvmStatic
    private fun loadGadget(context: Context) {
        System.load(context.cacheDir.toString() + File.separator + GADGET_FILE_NAME + ".so")
    }


    /**
     * call from frida.js
     */
    @JvmStatic
    fun getApiJson(): String {
        return context?.assets?.open(API_JSON_FILE_NAME)?.bufferedReader()?.readText() ?: ""
    }

    /**
     * call from frida.js
     */
    @JvmStatic
    fun addStackLog(callClazz: String, callMethod: String) {
        val currentTime = System.currentTimeMillis()
        val stack = Log.getStackTraceString(RuntimeException("stacktrace"))
        val m = "$callClazz.$callMethod"
        var str =
            (">>>>>> pid:${android.os.Process.myPid()}, thread(id:${android.os.Process.myTid()}, name:${Thread.currentThread().name}), timestamp:$currentTime\n");
        str += m + "\n"
        str += stack.split("\n").mapIndexed { index, s ->
            if (index < 2) "" else s // 去掉堆栈中 addStackLog 本身
        }.filter { it.isNotEmpty() }.joinToString("\n")

        stackLogList.add(0,StackLog(currentTime, m, str))

        Log.e(TAG, str)
    }

    @JvmStatic
    private fun is64bit(context: Context): Boolean {
        val file = File(context.applicationInfo.nativeLibraryDir)
        return file.name == "arm64"
    }
}