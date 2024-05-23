package com.ethan.floatingview;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

public interface IFloatingView {

    FloatingViewImpl remove();

    FloatingViewImpl add(View view);

    FloatingViewImpl attach(Activity activity);

    FloatingViewImpl attach(ViewGroup container);

    FloatingViewImpl detach(Activity activity);

    FloatingViewImpl detach(ViewGroup container);

    FloatingMagnetView getView();

    FloatingViewImpl layoutParams(ViewGroup.LayoutParams params);

    FloatingViewImpl listener(MagnetViewListener magnetViewListener);

}
