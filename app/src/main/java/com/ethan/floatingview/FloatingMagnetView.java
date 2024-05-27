package com.ethan.floatingview;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * 悬浮窗视图包装类，充当自定义内容的外壳，包含具体的滑动&吸附逻辑
 */
public class FloatingMagnetView extends FrameLayout {

    private final View centerLine = new View(getContext());
    protected int mScreenWidth;
    private float mOriginalRawX;
    private float mOriginalRawY;
    private float mOriginalX;
    private float mOriginalY;
    private MagnetViewListener mMagnetViewListener;
    private long mLastTouchDownTime;
    private MoveAnimator mMoveAnimator;
    private int mScreenHeight; // 整个屏幕高度，包含状态栏
    private int mStatusBarHeight;
    private boolean isNearestLeft = true;
    private float mPortraitY;
    private boolean mIsDragging = false;
    private ImageView mContentView;

    public FloatingMagnetView(Context context) {
        this(context, null);
    }

    public FloatingMagnetView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingMagnetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void addView(View child, int width, int height) {
        super.addView(child, width, height);
        mContentView = (ImageView) child;
    }

    public ImageView getContentView() {
        return mContentView;
    }

    public void setMagnetViewListener(MagnetViewListener magnetViewListener) {
        this.mMagnetViewListener = magnetViewListener;
    }

    private void init() {
        mMoveAnimator = new MoveAnimator();
        mStatusBarHeight = getStatusBarHeight();
        setClickable(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                changeOriginalTouchParams(event);
                updateSize();
                mMoveAnimator.stop();
                mIsDragging = false;
                break;
            case MotionEvent.ACTION_MOVE:
                updateViewPosition(event);
                if (!mIsDragging && !isOnClickEvent()) {
                    mMagnetViewListener.onDragStart(this);
                    mIsDragging = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                clearPortraitY();
                moveToEdge();
                if (isOnClickEvent()) {
                    dealClickEvent();
                }
                if (mIsDragging) {
                    // 暂时先不认为拖动状态结束，因为后续还有贴边动画
                    mMagnetViewListener.onDragEnd(this);
                }
                break;
        }
        return true;
    }

    protected void dealClickEvent() {
        if (mMagnetViewListener != null) {
            mMagnetViewListener.onClick(this);
        }
    }

    protected boolean isOnClickEvent() {
        return System.currentTimeMillis() - mLastTouchDownTime < Config.TOUCH_TIME_THRESHOLD;
    }

    private void updateViewPosition(MotionEvent event) {
        setX(mOriginalX + event.getRawX() - mOriginalRawX);
        // 限制不可超出屏幕高度
        float desY = mOriginalY + event.getRawY() - mOriginalRawY;
        if (desY < mStatusBarHeight) {
            desY = mStatusBarHeight;
        }
        if (desY > mScreenHeight - getHeight()) {
            desY = mScreenHeight - getHeight();
        }
        setY(desY);

        if (Config.SHOW_CENTER_LINE) {
            updateCenterLine();
        }
    }

    private void updateCenterLine() {
        if (centerLine.getParent() != null) {
            ((ViewGroup) centerLine.getParent()).removeView(centerLine);
        }

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT);
        centerLine.setX(mScreenWidth / 2.0f);
        centerLine.setLayoutParams(params);
        centerLine.setBackgroundColor(Color.BLUE);
        ((ViewGroup) getParent()).addView(centerLine);
    }

    private void changeOriginalTouchParams(MotionEvent event) {
        mOriginalX = getX();
        mOriginalY = getY();
        mOriginalRawX = event.getRawX();
        mOriginalRawY = event.getRawY();
        mLastTouchDownTime = System.currentTimeMillis();
    }

    protected void updateSize() {
        ViewGroup viewGroup = (ViewGroup) getParent();
        if (viewGroup != null) {
            mScreenWidth = viewGroup.getWidth() - getWidth();
            mScreenHeight = viewGroup.getHeight();
        }
    }

    public void moveToEdge() {
        moveToEdge(isNearestLeft(), false);
    }

    protected void moveToEdge(boolean isLeft, boolean isLandscape) {
        // 距离边缘还有一点空隙
        float moveDestination = isLeft ? Config.MARGIN_EDGE : mScreenWidth - Config.MARGIN_EDGE;
        float y = getY();
        if (!isLandscape && mPortraitY != 0) {
            y = mPortraitY;
            clearPortraitY();
        }
        mMoveAnimator.start(moveDestination, Math.min(Math.max(getStatusBarHeight(), y), mScreenHeight - getHeight()));
    }

    public void clearPortraitY() {
        mPortraitY = 0;
    }

    protected boolean isNearestLeft() {
        int middle = mScreenWidth / 2;
        isNearestLeft = getX() < middle;
        return isNearestLeft;
    }

    public void onRemove() {
        if (mMagnetViewListener != null) {
            mMagnetViewListener.onRemove(this);
        }
    }

    private void move(float deltaX, float deltaY) {
        setX(getX() + deltaX);
        setY(getY() + deltaY);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (getParent() != null) {
            final boolean isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
            markPortraitY(isLandscape);
            ((ViewGroup) getParent()).post(new Runnable() {
                @Override
                public void run() {
                    updateSize();
                    moveToEdge(isNearestLeft, isLandscape);
                }
            });
        }
    }

    private void markPortraitY(boolean isLandscape) {
        if (isLandscape) {
            mPortraitY = getY();
        }
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = Resources.getSystem().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private class MoveAnimator implements Runnable {

        private final Handler handler = new Handler(Looper.getMainLooper());
        private float destinationX;
        private float destinationY;
        private long startingTime;

        void start(float x, float y) {
            this.destinationX = x;
            this.destinationY = y;
            startingTime = System.currentTimeMillis();
            handler.post(this);
        }

        @Override
        public void run() {
            if (getRootView() == null || getRootView().getParent() == null) {
                return;
            }
            float progress = Math.min(1, (System.currentTimeMillis() - startingTime) / Config.ANIMATION_DURATION);
            // 计算单次位移量，由于 getX() 和 getY() 每次都在改变，所以并非线性动画
            float deltaX = (destinationX - getX()) * progress;
            float deltaY = (destinationY - getY()) * progress;
            move(deltaX, deltaY);
            if (progress < 1) {
                handler.post(this);
            } else if (mIsDragging) {
                mMagnetViewListener.onMoveToEdge(FloatingMagnetView.this);
            }
        }

        private void stop() {
            handler.removeCallbacks(this);
        }
    }

}
