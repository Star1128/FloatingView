package com.ethan.floatingview.utils

import android.content.Context

/**
 *
 * @author wangxingchen01 2024/5/28
 */
object ScreenUtil {
    fun getScreenWidth(context: Context): Int {
        val dm = context.resources.displayMetrics
        return dm.widthPixels
    }

    fun getStatusBarHeight(context: Context): Int {
        val res = context.resources
        var result = 0
        val resourceId = res.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = res.getDimensionPixelSize(resourceId)
        }
        return result
    }
}