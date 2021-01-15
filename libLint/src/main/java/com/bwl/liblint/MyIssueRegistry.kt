package com.bwl.liblint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue
import com.bwl.liblint.detector.FixOrientationTransDetector
import com.bwl.liblint.detector.ParseColorDetector

/**
 * Created by baiwenlong on 1/14/21.
 */
class MyIssueRegistry : IssueRegistry() {
    override val issues: List<Issue>
        get() = listOf(
            FixOrientationTransDetector.ISSUE,
            ParseColorDetector.ISSUE
        )

    override val api: Int
        get() = CURRENT_API
}