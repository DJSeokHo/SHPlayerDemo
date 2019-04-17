package com.swein.shplayerdemo.main.headautoplayer;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;

import com.swein.shplayerdemo.R;
import com.swein.shplayerdemo.constants.Constants;
import com.swein.shplayerdemo.custom.JZMediaIjkplayer;
import com.swein.shplayerdemo.framework.util.picasso.SHPicasso;
import com.swein.shplayerdemo.framework.util.thread.ThreadUtil;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class HeaderAutoPlayerActivity extends Activity {

    private JzvdStd jzvdStd;
    private JZMediaIjkplayer jzMediaIjkplayer;
    private Jzvd.JZAutoFullscreenListener sensorEventListener;
    private SensorManager sensorManager;

    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_header_auto_player);

        jzvdStd = findViewById(R.id.jzvdStd);

//        shJzvdStd.setUp("http://jzvd.nathen.cn/342a5f7ef6124a4a8faf00e738b8bee4/cf6d9db0bd4d41f59d09ea0a81e918fd-5287d2089db37e62345123a1be272f8b.mp4", "test", JzvdStd.SCREEN_WINDOW_NORMAL);
//        shJzvdStd.setUp("rtmp://184.72.239.149/vod/mp4:bigbuckbunny_1500.mp4", "test", JzvdStd.SCREEN_WINDOW_NORMAL);

        SHPicasso.getInstance().loadImage(this, "http://jzvd-pic.nathen.cn/jzvd-pic/1bb2ebbe-140d-4e2e-abd2-9e7e564f71ac.png", jzvdStd.thumbImageView);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorEventListener = new Jzvd.JZAutoFullscreenListener();

        initLivePlayer();
    }

    private void initLivePlayer() {

        Jzvd.clearSavedProgress(this, Constants.RTMP_URL);

        if(jzvdStd == null) {
            jzvdStd = findViewById(R.id.jzvdStd);
        }

        jzMediaIjkplayer = new JZMediaIjkplayer();
        jzvdStd.setUp(Constants.RTMP_URL, "test", JzvdStd.SCREEN_WINDOW_NORMAL);
        JzvdStd.setMediaInterface(jzMediaIjkplayer);

        if(isFirst) {
            ThreadUtil.startUIThread(3000, new Runnable() {
                @Override
                public void run() {
                    jzvdStd.startButton.performClick();
                    isFirst = false;
                }
            });
        }
        else {
            jzvdStd.startButton.performClick();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        if(jzMediaIjkplayer == null || jzMediaIjkplayer.getIjkMediaPlayer() == null) {
            /*
                check null because jzMediaIjkplayer.getIjkMediaPlayer() on null sometimes...
             */
            Jzvd.releaseAllVideos();

            initLivePlayer();
        }

        Jzvd.goOnPlayOnResume();
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(sensorEventListener);
        Jzvd.goOnPlayOnPause();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (Jzvd.backPress()) {
            return;
        }
        Jzvd.clearSavedProgress(this, Constants.RTMP_URL);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        Jzvd.releaseAllVideos();
        Jzvd.clearSavedProgress(this, Constants.RTMP_URL);
        super.onDestroy();
    }
}
