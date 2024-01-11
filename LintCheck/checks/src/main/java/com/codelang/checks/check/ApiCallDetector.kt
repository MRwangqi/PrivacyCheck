package com.codelang.checks.check

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.codelang.checks.config.ApiConfig
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UastCallKind
import org.jetbrains.uast.util.isMethodCall

class ApiCallDetector : BaseDetector(), Detector.UastScanner {

    companion object {
        private const val REPORT_MESSAGE =
            "禁止调用隐私合规配置文件 privacy_api.json 中描述的方法"
        val ISSUE = Issue.create(
            "ApiCheck",
            REPORT_MESSAGE,
            REPORT_MESSAGE,
            Category.CORRECTNESS,
            10,
            Severity.ERROR,
            Implementation(ApiCallDetector::class.java, Scope.JAVA_FILE_SCOPE)
        )
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>>? {
        return listOf(UCallExpression::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return ApiCallUastHandler(context)
    }

    private class ApiCallUastHandler(val context: JavaContext?) : UElementHandler() {

        override fun visitCallExpression(node: UCallExpression) {
//            println("ApiCallUastHandler====${node.resolve()}")
            if (node.isMethodCall()) {
//                println("ApiCallUastHandler====${node.resolve()?.containingClass?.qualifiedName}  -> ${node.methodName}")
                ApiConfig.getApiNode().find {
                    context?.evaluator?.isMemberInClass(node.resolve(), it.clazz) == true
                            && it.method.find { m -> m == node.methodName } != null
                }?.let {
                    context?.report(
                        ISSUE,
                        node,
                        context.getLocation(node),
                        REPORT_MESSAGE
                    )
                }
            }
        }
    }
}