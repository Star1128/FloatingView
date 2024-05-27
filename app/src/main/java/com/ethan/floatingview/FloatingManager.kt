package com.ethan.floatingview

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.util.Log
import android.widget.ImageView

class FloatingManager private constructor() : ActivityLifecycleCallbacks, MagnetViewListener {

    companion object {
        private const val TAG = "FloatingManager"

        private val instance: FloatingManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            FloatingManager()
        }

        @JvmStatic
        fun init(application: Application) {
            application.registerActivityLifecycleCallbacks(instance)
            // 将自定义 ViewGroup 添加进 FloatingView（外壳）中
            val contentView = buildInitialContent(application)
            FloatingViewImpl.get().add(contentView)
            // 监听点击和移除事件
            FloatingViewImpl.get().listener(instance)
        }

        private fun buildInitialContent(application: Application): ImageView {
            val view = ImageView(application)
            view.setImageResource(R.drawable.checkbox_unselect)
            return view
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        // 把 FloatingView 添加到 Activity 的根布局中
        FloatingViewImpl.get().attach(activity)
    }

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

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
        FloatingViewImpl.get().contentView?.setImageResource(R.drawable.checkbox_selected)
    }

    override fun onDragEnd(magnetView: FloatingMagnetView) {
        FloatingViewImpl.get().contentView?.setImageResource(R.drawable.checkbox_unselect)
    }

}