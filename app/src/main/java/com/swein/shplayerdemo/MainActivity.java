package com.swein.shplayerdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.swein.shplayerdemo.framework.util.activity.ActivityUtil;
import com.swein.shplayerdemo.framework.util.eventsplitshot.eventcenter.EventCenter;
import com.swein.shplayerdemo.framework.util.eventsplitshot.subject.ESSArrows;
import com.swein.shplayerdemo.framework.util.thread.ThreadUtil;
import com.swein.shplayerdemo.main.watchdetail.WatchingDetailActivity;

import java.util.HashMap;

import cn.jzvd.Jzvd;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Jzvd.releaseAllVideos();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventCenter.getInstance().addEventObserver(ESSArrows.EXIT_APP, this, new EventCenter.EventRunnable() {
            @Override
            public void run(String arrow, Object poster, HashMap<String, Object> data) {
                ThreadUtil.startUIThread(0, new Runnable() {
                    @Override
                    public void run() {
                        Log.d("???", "haha");
                        MainActivity.this.finish();
                    }
                });
            }
        });

//        ActivityUtil.startNewActivityWithoutFinish(this, HeaderAutoPlayerActivity.class);
        ActivityUtil.startNewActivityWithoutFinish(this, WatchingDetailActivity.class);
//        ActivityUtil.startNewActivityWithoutFinish(this, FloatingWindowActivity.class);

    }
}
