package com.ethan.floatingview;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.core.view.ViewCompat;

import com.ethan.floatingview.utils.ScreenUtil;

import java.lang.ref.WeakReference;

/**
 * 悬浮窗生命周期控制类，执行添加和移除逻辑
 */
public class FloatingViewImpl implements IFloatingView {

    private static volatile FloatingViewImpl mInstance;
    private FloatingMagnetView mFloatingMagnetView;
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

    public ImageView getContentView() {
        if (mFloatingMagnetView == null) {
            return null;
        }
        return mFloatingMagnetView.getContentView();
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
    public FloatingViewImpl add(View view, boolean isLeft) {
        synchronized (this) {
            if (mFloatingMagnetView != null) {
                return this;
            }
            mFloatingMagnetView = new FloatingMagnetView(view.getContext());
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = view.getResources().getDisplayMetrics().heightPixels >> 1;
            mFloatingMagnetView.setLayoutParams(params);
            mFloatingMagnetView.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            // 初始化贴右边，需要额外计算 leftMargin
            if (!isLeft) {
                // 测量 view 的宽度
                view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int viewWidth = view.getMeasuredWidth();
                // 获取屏幕宽度，并计算 leftMargin
                int screenWidth = ScreenUtil.INSTANCE.getScreenWidth(App.context);
                params.leftMargin = screenWidth - viewWidth;
                // 更新布局参数
                mFloatingMagnetView.setLayoutParams(params);
            }
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
        mFloatingMagnetView.post(
                this::resetInitEdge
        );
        return this;
    }

    public void resetInitEdge() {
        // 如果没贴边的情况下打开了新页面，需要重新贴一次边
        mFloatingMagnetView.clearPortraitY();
        mFloatingMagnetView.updateSize();
        mFloatingMagnetView.moveToEdge();
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