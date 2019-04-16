package com.swein.shplayerdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.swein.shplayerdemo.framework.util.activity.ActivityUtil;
import com.swein.shplayerdemo.main.floatingwindow.FloatingWindowActivity;
import com.swein.shplayerdemo.main.headautoplayer.HeaderAutoPlayerActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        ActivityUtil.startNewActivityWithoutFinish(this, HeaderAutoPlayerActivity.class);
        ActivityUtil.startNewActivityWithoutFinish(this, FloatingWindowActivity.class);

    }
}
