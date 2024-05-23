package com.ethan.floatingview;
import android.app.Application;
import android.widget.ImageView;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ImageView view = new ImageView(this);
        view.setImageResource(R.mipmap.ic_launcher_round);
        FloatingManager.init(this, view);
    }
}
