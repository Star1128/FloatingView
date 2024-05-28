package com.ethan.floatingview;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

public interface IFloatingView {

    FloatingViewImpl remove();

    /**
     * 初始化视图
     *
     * @param view   内容
     * @param isLeft 是否初始贴左边
     */
    FloatingViewImpl add(View view, boolean isLeft);

    FloatingViewImpl attach(Activity activity);

    FloatingViewImpl attach(ViewGroup container);

    FloatingViewImpl detach(Activity activity);

    FloatingViewImpl detach(ViewGroup container);

    FloatingMagnetView getView();

    FloatingViewImpl layoutParams(ViewGroup.LayoutParams params);

    FloatingViewImpl listener(MagnetViewListener magnetViewListener);

}
