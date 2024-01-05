package com.codelang.module.file

import com.codelang.module.bean.ApiCallResult
import com.google.gson.Gson
import org.gradle.api.Project
import java.io.File

object FileUtils {

    fun generatorFile(project: Project, analysisList: List<ApiCallResult>) {
        val text = Gson().toJson(analysisList)
        if (!project.buildDir.exists()) {
            project.buildDir.mkdir()
        }
        val outputFile = File(project.buildDir.absolutePath + File.separator + "ApiCall.json")
        outputFile.writeText(text)
        println("配置文件生成----> $outputFile")
    }
}