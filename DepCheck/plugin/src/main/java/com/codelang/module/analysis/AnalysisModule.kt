package com.codelang.module.analysis

import com.codelang.module.bean.ApiCallResult
import com.codelang.module.bean.ApiNode
import com.codelang.module.bean.Clazz
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodInsnNode

object AnalysisModule {


    private val resultMap = hashMapOf<String, ArrayList<ApiCallResult>>()

    fun analysis(
        clazzList: List<Clazz>,
        apiList: List<ApiNode>
    ): Map<String, List<ApiCallResult>> {

        clazzList.forEach { clazz ->
            clazz.methods?.forEach {
                it.instructions
                    .filterIsInstance(MethodInsnNode::class.java)
                    .forEach Continue@{ node ->
                        val callClazz = getClassName(node.owner) ?: return@Continue
                        val callMethod = node.name
                        checkApiCall(callClazz, callMethod, it.name, clazz, apiList)
                    }
                it.instructions
                    .filterIsInstance(FieldInsnNode::class.java)
                    .forEach Continue@{ node ->
                        val callClazz = getClassName(node.owner) ?: return@Continue
                        val callField = node.name
                        checkApiCall(callClazz, callField, it.name, clazz, apiList)
                    }
            }
        }

        return resultMap
    }

    private fun checkApiCall(
        callClazz: String,
        callName: String,
        method: String,
        clazz: Clazz,
        apiList: List<ApiNode>
    ) {
        val result = apiList.find { it.clazz == callClazz }?.method?.find { it == callName }
        if (!result.isNullOrEmpty()) {
            val key = "${callClazz}_$callName"

            var list = resultMap[key]
            if (list.isNullOrEmpty()) {
                list = arrayListOf()
            }
            list.add(
                ApiCallResult(
                    clazz.className ?: "",
                    method,
                    clazz.dep ?: "",
                )
            )
            resultMap[key] = list
        }
    }


    private fun getClassName(desc: String): String? {
        var clazzName = desc
        // [java/util/ArrayList;  数组对象，也有可能是 [[java/util/ArrayList;
        if (clazzName.startsWith("[")) {
            clazzName = clazzName.substring(clazzName.lastIndexOf("[") + 1, clazzName.length)
        }
        // Ljava/util/ArrayList;  对象
        if (clazzName.startsWith("L")) {
            return clazzName.substring(1, clazzName.length - 1)
        }

        // java.util.ArrayList
        if (clazzName.contains("/")) {
            return clazzName.replace("/", ".")
        }
        // 基础类型不关心，直接返回 null
        return null
    }
}