package com.ethan.floatingview;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.core.view.ViewCompat;

import java.lang.ref.WeakReference;


public class FloatingViewImpl implements IFloatingView {

    private FloatingMagnetView mFloatingMagnetView;
    private static volatile FloatingViewImpl mInstance;
    private WeakReference<ViewGroup> mContainer;

    private FloatingViewImpl() {
    }

    public static FloatingViewImpl get() {
        if (mInstance == null) {
            synchronized (FloatingViewImpl.class) {
                if (mInstance == null) {
                    mInstance = new FloatingViewImpl();
                }
            }
        }
        return mInstance;
    }

    @Override
    public FloatingViewImpl remove() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mFloatingMagnetView == null) {
                    return;
                }
                if (ViewCompat.isAttachedToWindow(mFloatingMagnetView) && getContainer() != null) {
                    getContainer().removeView(mFloatingMagnetView);
                }
                mFloatingMagnetView = null;
            }
        });
        return this;
    }

    @Override
    public FloatingViewImpl add(View view) {
        synchronized (this) {
            if (mFloatingMagnetView != null) {
                return this;
            }
            mFloatingMagnetView = new FloatingMagnetView(view.getContext());
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = view.getResources().getDisplayMetrics().heightPixels >> 1;
            mFloatingMagnetView.setLayoutParams(params);
            mFloatingMagnetView.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            addViewToWindow(mFloatingMagnetView);
        }
        return this;
    }

    @Override
    public FloatingViewImpl attach(Activity activity) {
        attach(getActivityRoot(activity));
        return this;
    }

    @Override
    public FloatingViewImpl attach(ViewGroup container) {
        // 如果还没 Add，只初始化 Container
        if (container == null || mFloatingMagnetView == null) {
            mContainer = new WeakReference<>(container);
            return this;
        }
        // 避免重复 Attach
        if (mFloatingMagnetView.getParent() == container) {
            return this;
        }
        // 更换宿主
        if (mFloatingMagnetView.getParent() != null) {
            ((ViewGroup) mFloatingMagnetView.getParent()).removeView(mFloatingMagnetView);
        }
        mContainer = new WeakReference<>(container);
        container.addView(mFloatingMagnetView);
        return this;
    }

    @Override
    public FloatingViewImpl detach(Activity activity) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            return this;
        }
        detach(getActivityRoot(activity));
        return this;
    }

    @Override
    public FloatingViewImpl detach(ViewGroup container) {
        if (mFloatingMagnetView != null && container != null && ViewCompat.isAttachedToWindow(mFloatingMagnetView)) {
            container.removeView(mFloatingMagnetView);
        }
        if (getContainer() == container) {
            mContainer = null;
        }
        return this;
    }

    @Override
    public FloatingMagnetView getView() {
        return mFloatingMagnetView;
    }

    @Override
    public FloatingViewImpl layoutParams(ViewGroup.LayoutParams params) {
        if (mFloatingMagnetView != null) {
            mFloatingMagnetView.setLayoutParams(params);
        }
        return this;
    }

    @Override
    public FloatingViewImpl listener(MagnetViewListener magnetViewListener) {
        if (mFloatingMagnetView != null) {
            mFloatingMagnetView.setMagnetViewListener(magnetViewListener);
        }
        return this;
    }

    private void addViewToWindow(final View view) {
        if (getContainer() == null) {
            return;
        }
        getContainer().addView(view);
    }

    private ViewGroup getContainer() {
        if (mContainer == null) {
            return null;
        }
        return mContainer.get();
    }

    private FrameLayout getActivityRoot(Activity activity) {
        if (activity == null) {
            return null;
        }
        try {
            return (FrameLayout) activity.getWindow().getDecorView();
        } catch (Exception ignored) {
        }
        return null;
    }
}