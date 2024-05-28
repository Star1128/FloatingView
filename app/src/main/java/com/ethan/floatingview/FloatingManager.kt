package com.ethan.floatingview

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat

/**
 * 悬浮窗全局管理类，与 Activity 生命周期绑定
 */
object FloatingManager : ActivityLifecycleCallbacks, MagnetViewListener {

    private const val TAG = "FloatingManager"

    fun init(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
        // 将自定义 ViewGroup 添加进 FloatingView（外壳）中
        val contentView = buildInitialContent(application)
        FloatingViewImpl.get().add(contentView, false)
        // 监听点击和移除事件
        FloatingViewImpl.get().listener(this)
    }

    private fun buildInitialContent(application: Application): ImageView {
        val view = ImageView(application)
        view.setImageResource(R.drawable.duer_half)
        return view
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {
        if (isEnableFloatingView(activity)) {
            // 把 FloatingView 添加到 Activity 的根布局中
            FloatingViewImpl.get().attach(activity)
        }
    }

    /**
     * 判断页面是否支持悬浮窗
     */
    private fun isEnableFloatingView(activity: Activity): Boolean {
//        if (activity is FlutterBoostActivity) {
//            val url = activity.intent.extras?.getString("url")
//        } else {
//            val componentName = activity.componentName
//            Log.d(TAG, "componentName: ${componentName.className}")
//            return true
//        }
        return true
    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {
        if (activity.isFinishing) {
            // 把 FloatingView 从 Activity 移除
            FloatingViewImpl.get().detach(activity)
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

    override fun onRemove(magnetView: FloatingMagnetView) {}

    override fun onClick(magnetView: FloatingMagnetView) {
        Log.d(TAG, "onClick")
    }

    override fun onDragStart(magnetView: FloatingMagnetView) {
        magnetView.contentView?.setImageResource(R.drawable.checkbox_selected)
    }

    override fun onDragEnd(magnetView: FloatingMagnetView) {}

    override fun onMoveToEdge(magnetView: FloatingMagnetView, isLeft: Boolean) {
        if (isLeft) {
            val drawable = ResourcesCompat.getDrawable(App.context.resources, R.drawable.duer_half, null)
            drawable?.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            val originBitmap = drawableToBitmap(drawable)
            Matrix().apply {
                preScale(-1f, 1f)
                originBitmap?.let {
                    val reverseBitmap = Bitmap.createBitmap(originBitmap, 0, 0, originBitmap.width, originBitmap.height, this, true)
                    magnetView.contentView?.setImageBitmap(reverseBitmap)
                }
            }
        } else {
            magnetView.contentView?.setImageResource(R.drawable.duer_half)
        }
    }

    private fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) return null
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}