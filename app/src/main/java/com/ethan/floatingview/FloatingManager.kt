package com.ethan.floatingview;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class FloatingManager implements Application.ActivityLifecycleCallbacks, MagnetViewListener {

    public static void init(Application application, View view) {
        FloatingManager manager = new FloatingManager();
        application.registerActivityLifecycleCallbacks(manager);
        // 将自定义 ViewGroup 添加进 FloatingView（外壳）中
        FloatingViewImpl.get().add(view);
        // 监听点击和移除事件
        FloatingViewImpl.get().listener(manager);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        // 把 FloatingView 添加到 Activity 的根布局中
        FloatingViewImpl.get().attach(activity);
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (activity.isFinishing()) {
            // 把 FloatingView 从 Activity 移除
            FloatingViewImpl.get().detach(activity);
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    @Override
    public void onRemove(FloatingMagnetView magnetView) {

    }

    @Override
    public void onClick(FloatingMagnetView magnetView) {

    }
}