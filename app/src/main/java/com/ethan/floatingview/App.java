package com.ethan.floatingview;
import android.app.Application;
import android.widget.ImageView;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FloatingManager.init(this);
    }
}
