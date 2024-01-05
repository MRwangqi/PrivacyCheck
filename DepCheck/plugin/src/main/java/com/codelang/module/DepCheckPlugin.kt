package com.codelang.module

import com.codelang.module.analysis.AnalysisModule
import com.codelang.module.bean.ApiNode
import com.codelang.module.collect.ClazzCollectModule
import com.codelang.module.extension.DepCheckExtension
import com.codelang.module.file.FileUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.lang.reflect.Type


class DepCheckPlugin : Plugin<Project> {

    companion object {
        const val TASK_NAME = "depCheck"
        const val BUILD = "build"
        const val EXT_NAME = "depConfig"
    }


    override fun apply(project: Project) {
        // ./gradlew depCheck -Pbuild=debug
        val params = project.gradle.startParameter.projectProperties
        val build = if (params.containsKey(BUILD)) {
            params[BUILD] ?: "debug"
        } else {
            // 默认 debug 兜底
            "debug"
        }

        project.extensions.create(EXT_NAME, DepCheckExtension::class.java)

        project.afterEvaluate {
            val configurationName = "${build}RuntimeClasspath"
            val depCheckExtension = project.extensions.findByName(EXT_NAME) as DepCheckExtension

            project.tasks.create(TASK_NAME) {
                it.group = TASK_NAME
                it.doLast {

                    val configPath = depCheckExtension.path
                    if (configPath.isEmpty()) {
                        println("请配置 api.json 文件路径")
                        return@doLast
                    }
                    val configFile = File(configPath)
                    if (!configFile.exists()) {
                        println("api.json 文件不存在")
                        return@doLast
                    }

                    val type: Type = object : TypeToken<List<ApiNode>>() {}.type
                    val apiCallList: List<ApiNode> = Gson().fromJson(configFile.bufferedReader(), type)

                    // 收集依赖里的所有 class 文件
                    val clazzList = ClazzCollectModule.collectClazz(project, configurationName)

                    // 分析 class 引用情况
                    val analysisList =
                        AnalysisModule.analysis(clazzList, apiCallList)

                    // 生成文件
                    FileUtils.generatorFile(project, analysisList)
                }
            }
        }
    }
}