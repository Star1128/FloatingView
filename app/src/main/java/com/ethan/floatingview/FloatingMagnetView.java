package com.ethan.floatingview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ethan.floatingview.utils.ScreenUtil;

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
        mStatusBarHeight = ScreenUtil.INSTANCE.getStatusBarHeight(App.context);
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
                addCenterLine();
                if (!mIsDragging && !isOnClickEvent()) {
                    mMagnetViewListener.onDragStart(this);
                    mIsDragging = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                clearPortraitY();
                clearCenterLine();
                moveToEdge();
                if (isOnClickEvent()) {
                    performClick();
                    dealClickEvent();
                }
                if (mIsDragging) {
                    // 暂时先不认为拖动状态结束，因为后续还有贴边动画
                    mIsDragging = false;
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

    @Override
    public boolean performClick() {
        return super.performClick();
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
    }

    private void addCenterLine() {
        if (Config.SHOW_CENTER_LINE) {
            if (centerLine.getParent() != null) {
                ((ViewGroup) centerLine.getParent()).removeView(centerLine);
            }

            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT);
            centerLine.setX(mScreenWidth / 2.0f);
            centerLine.setLayoutParams(params);
            centerLine.setBackgroundColor(Color.BLUE);
            ((ViewGroup) getParent()).addView(centerLine);
        }
    }

    private void clearCenterLine() {
        if (Config.SHOW_CENTER_LINE && centerLine.getParent() != null) {
            ((ViewGroup) centerLine.getParent()).removeView(centerLine);
        }
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
        mMoveAnimator.start(isLeft, moveDestination, Math.min(Math.max(ScreenUtil.INSTANCE.getStatusBarHeight(App.context), y), mScreenHeight - getHeight()), Config.ANIMATION_DURATION);
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

    private class MoveAnimator {
        private void start(boolean isLeft, float destinationX, float destinationY, long duration) {
            ObjectAnimator animatorX = ObjectAnimator.ofFloat(FloatingMagnetView.this, "x", getX(), destinationX);
            ObjectAnimator animatorY = ObjectAnimator.ofFloat(FloatingMagnetView.this, "y", getY(), destinationY);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(animatorX, animatorY); // 同时播放 X 和 Y 动画
            animatorSet.setDuration(duration);
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mMagnetViewListener != null) {
                        mMagnetViewListener.onMoveToEdge(FloatingMagnetView.this, isLeft);
                    }
                }
            });
            animatorSet.start();
        }

        private void stop() {
            // 停止动画
            animate().cancel();
        }
    }

}
