package com.swein.shplayerdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.swein.shplayerdemo.framework.util.activity.ActivityUtil;
import com.swein.shplayerdemo.framework.util.debug.log.ILog;
import com.swein.shplayerdemo.framework.util.eventsplitshot.eventcenter.EventCenter;
import com.swein.shplayerdemo.framework.util.eventsplitshot.subject.ESSArrows;
import com.swein.shplayerdemo.framework.util.thread.ThreadUtil;
import com.swein.shplayerdemo.main.biglistitem.ListAutoTinyWindowActivity;

import java.util.HashMap;

import cn.jzvd.Jzvd;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";

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
                        ILog.iLogDebug(TAG, "MainActivity exit");
                        MainActivity.this.finish();
                    }
                });
            }
        });

//        ActivityUtil.startNewActivityWithFinish(this, HeaderAutoPlayerActivity.class);
//        ActivityUtil.startNewActivityWithFinish(this, VODAutoPlayerActivity.class);
//        ActivityUtil.startNewActivityWithFinish(this, WatchingDetailActivity.class);
        ActivityUtil.startNewActivityWithFinish(this, ListAutoTinyWindowActivity.class);
    }
}
