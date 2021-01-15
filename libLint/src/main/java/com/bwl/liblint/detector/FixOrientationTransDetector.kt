package com.bwl.liblint.detector

import com.android.SdkConstants
import com.android.tools.lint.detector.api.*
import org.w3c.dom.Element
import java.util.*

/**
 * Created by baiwenlong on 1/14/21.
 */
class FixOrientationTransDetector : Detector(), Detector.XmlScanner {
    private val mFixedActivityMap = mutableMapOf<String, ElementEntry>()
    private val mAllThemeMap = mutableMapOf<String, ThemeData>()

    companion object {
        val ISSUE = Issue.create(
            "FixOrientationTransDetector",
            "不要在 AndroidManifest.xml 文件里同时设置方向和透明主题",
            "Activity 同时设置方向和透明主题在 Android 8.0 手机会 Crash",
            Category.CORRECTNESS,
            8,
            Severity.ERROR,
            Implementation(
                FixOrientationTransDetector::class.java,
                EnumSet.of(Scope.MANIFEST, Scope.ALL_RESOURCE_FILES)
            )
        )
        private val TRANSPARENT_THEMES = setOf("Theme.AppTheme.Transparent")
    }

    override fun getApplicableElements(): Collection<String>? {
        return listOf(SdkConstants.TAG_ACTIVITY, SdkConstants.TAG_STYLE)
    }

    override fun visitElement(context: XmlContext, element: Element) {
        when (element.tagName) {
            SdkConstants.TAG_ACTIVITY -> {
                println("isFixedOrientation = ${isFixedOrientation(element)}")
                if (isFixedOrientation(element)) {
                    val theme =
                        element.getAttributeNS(SdkConstants.ANDROID_URI, SdkConstants.ATTR_THEME).let {
                            it.substring(it.indexOf("/") + 1)
                        }
                    if (theme in TRANSPARENT_THEMES) {
                        reportError(context, element)
                    } else {
                        mFixedActivityMap[theme] =
                            ElementEntry(context, element)
                    }
                }
            }
            SdkConstants.TAG_STYLE -> {
                val styleName = element.getAttribute(SdkConstants.ATTR_NAME)
                val parentStyleName = element.getAttribute(SdkConstants.ATTR_PARENT)
                val isTransparent = isTranslucentOrFloating(element)
                mAllThemeMap[styleName] = ThemeData(styleName, isTransparent, parentStyleName)

                mFixedActivityMap.forEach {mapEntry ->
                    var themeData = mAllThemeMap[mapEntry.key]
                    while (themeData != null) {
                        if (themeData!!.isTransparent) {
                            mFixedActivityMap.remove(mapEntry.key)
                            reportError(context, element)
                            return@forEach
                        }
                        themeData = mAllThemeMap[themeData.parentTheme]
                    }
                }

            }
        }
    }

    private fun isFixedOrientation(element: Element): Boolean {
        return when (element.getAttributeNS(SdkConstants.ANDROID_URI, "screenOrientation")) {
            "landscape", "sensorLandscape", "reverseLandscape", "userLandscape", "portrait", "sensorPortrait", "reversePortrait", "userPortrait", "locked" -> true
            else -> false
        }
    }

    private fun isTranslucentOrFloating(element: Element): Boolean {
        if (element.getAttribute(SdkConstants.ATTR_NAME) in TRANSPARENT_THEMES) {
            return true
        }
        for (index in 0 until element.childNodes.length) {
            val childNode = element.childNodes.item(index)
            if (childNode is Element && SdkConstants.TAG_ITEM == childNode.tagName && SdkConstants.VALUE_TRUE == childNode.firstChild?.nodeValue) {
                when (childNode.getAttribute(SdkConstants.ATTR_NAME)) {
                    "android:windowIsTranslucent", "android:windowSwipeToDismiss", "android:windowIsFloating" -> return true
                }
            }
        }
        return false
    }

    private fun reportError(context: XmlContext, element: Element) {
        context.report(
            ISSUE,
            element,
            context.getLocation(element),
            ISSUE.getBriefDescription(TextFormat.TEXT)
        )
    }

    data class ElementEntry(val context: XmlContext, val element: Element)

    data class ThemeData(val theme: String, val isTransparent: Boolean, val parentTheme: String?)
}