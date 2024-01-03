package com.codelang.checks

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.Issue
import com.codelang.checks.check.ApiCallDetector

class LintIssueRegistry: IssueRegistry() {
    override val issues: List<Issue>
        get() = listOf(
            ApiCallDetector.ISSUE
        )

}