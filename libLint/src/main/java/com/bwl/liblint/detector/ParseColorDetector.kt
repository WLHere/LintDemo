package com.bwl.liblint.detector

import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiTryStatement
import com.intellij.psi.impl.source.tree.java.MethodElement
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtTryExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.kotlin.KotlinUFunctionCallExpression


/**
 * Created by baiwenlong on 1/14/21.
 */
class ParseColorDetector : Detector(), Detector.UastScanner {

    companion object {
        val ISSUE = Issue.create(
            "ParseColorDetector",
            "Color.parseColor 解析可能 crash",
            "后端下发的色值可能无法解析，导致 crash",
            Category.CORRECTNESS,
            8,
            Severity.ERROR,
            Implementation(ParseColorDetector::class.java, Scope.JAVA_FILE_SCOPE)
        )
    }

    override fun getApplicableMethodNames(): List<String>? {
        return listOf("parseColor")
    }

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (!context.evaluator.isMemberInClass(method, "android.graphics.Color")) {
            return
        }
        if (isConstColor(node)) {
            return
        }
        if (isWrappedByTryCatch(node, context)) {
            return
        }
        reportError(context, node)
    }

    private fun isConstColor(node: UCallExpression): Boolean {
        return node.valueArguments[0].evaluate().toString().startsWith("#")
    }

    private fun isWrappedByTryCatch(node: UCallExpression, context: JavaContext): Boolean {
        if (node is KotlinUFunctionCallExpression) {
            var parent = node.sourcePsi?.parent
            while (parent != null && parent !is KtFunction) {
                if (parent is KtTryExpression) {
                    return true
                }
                parent = parent.parent
            }
            return false
        } else {
            var parent = node.sourcePsi?.parent
            while (parent != null && parent !is MethodElement) {
                if (parent is PsiTryStatement) {
                    return true
                }
                parent = parent.parent
            }
            return false
        }
    }

    private fun reportError(context: JavaContext, node: UCallExpression) {
        context.report(ISSUE, node, context.getCallLocation(node,
            includeReceiver = false,
            includeArguments = false
        ), ISSUE.getBriefDescription(TextFormat.TEXT))
    }
}