package com.ethan.floatingview;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ethan.floatingview.R;

public class MainActivity extends AppCompatActivity {
    static int pageNum = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ((TextView) findViewById(R.id.page_num)).setText("页面" + pageNum);
    }

    public void createActivity(View view) {
        pageNum++;
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        startActivity(intent);
    }
}
