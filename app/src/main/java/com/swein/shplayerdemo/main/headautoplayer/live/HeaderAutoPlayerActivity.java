package com.swein.shplayerdemo.main.headautoplayer.live;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;

import com.swein.shplayerdemo.R;
import com.swein.shplayerdemo.constants.Constants;
import com.swein.shplayerdemo.custom.JZMediaIjkplayer;
import com.swein.shplayerdemo.framework.util.debug.log.ILog;
import com.swein.shplayerdemo.framework.util.eventsplitshot.eventcenter.EventCenter;
import com.swein.shplayerdemo.framework.util.eventsplitshot.subject.ESSArrows;
import com.swein.shplayerdemo.framework.util.picasso.SHPicasso;
import com.swein.shplayerdemo.framework.util.thread.ThreadUtil;

import java.util.HashMap;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class HeaderAutoPlayerActivity extends Activity {
    
    private final static String TAG = "HeaderAutoPlayerActivity";

    private final static int ACTION_MANAGE_OVERLAY_PERMISSION_CODE = 101;

    private JzvdStd jzvdStd;
    private JZMediaIjkplayer jzMediaIjkplayer;
    private Jzvd.JZAutoFullscreenListener sensorEventListener;
    private SensorManager sensorManager;

    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_header_auto_player);

        Intent intent = getIntent();
        if(intent != null) {
            Bundle bundle = intent.getBundleExtra(Constants.BUNDLE_KEY);
            if(bundle != null) {
                isFirst = bundle.getBoolean(Constants.FROM_FLOATING_KEY, true);
            }
        }


//        shJzvdStd.setUp("http://jzvd.nathen.cn/342a5f7ef6124a4a8faf00e738b8bee4/cf6d9db0bd4d41f59d09ea0a81e918fd-5287d2089db37e62345123a1be272f8b.mp4", "test", JzvdStd.SCREEN_WINDOW_NORMAL);
//        shJzvdStd.setUp("rtmp://184.72.239.149/vod/mp4:bigbuckbunny_1500.mp4", "test", JzvdStd.SCREEN_WINDOW_NORMAL);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorEventListener = new Jzvd.JZAutoFullscreenListener();

        initLivePlayer();
    }

    private void initLivePlayer() {

        Jzvd.releaseAllVideos();
        Jzvd.clearSavedProgress(this, Constants.RTMP_URL);

        Jzvd.jzvdDelegate = new Jzvd.JzvdDelegate() {
            @Override
            public void onPIP() {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkPermission();
                }
                else {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(Constants.RTMP_URL_KEY, Constants.RTMP_URL);
                    EventCenter.getInstance().sendEvent(ESSArrows.OPEN_FLOATING_WINDOW, this, hashMap);
                    finish();
                }


            }
        };

        if(jzvdStd == null) {
            jzvdStd = findViewById(R.id.jzvdStd);
            SHPicasso.getInstance().loadImage(this, "http://jzvd-pic.nathen.cn/jzvd-pic/1bb2ebbe-140d-4e2e-abd2-9e7e564f71ac.png", jzvdStd.thumbImageView);
        }

        jzMediaIjkplayer = new JZMediaIjkplayer();
        jzvdStd.setUp(Constants.RTMP_URL, "live", JzvdStd.SCREEN_WINDOW_NORMAL);
        JzvdStd.setMediaInterface(jzMediaIjkplayer);

    }

    private void resumeLivePlayer() {

        if(jzvdStd == null) {

            Jzvd.releaseAllVideos();
            Jzvd.clearSavedProgress(this, Constants.RTMP_URL);

            jzvdStd = findViewById(R.id.jzvdStd);
            SHPicasso.getInstance().loadImage(this, "http://jzvd-pic.nathen.cn/jzvd-pic/1bb2ebbe-140d-4e2e-abd2-9e7e564f71ac.png", jzvdStd.thumbImageView);

            jzMediaIjkplayer = new JZMediaIjkplayer();
            jzvdStd.setUp(Constants.RTMP_URL, "live", JzvdStd.SCREEN_WINDOW_NORMAL);
            JzvdStd.setMediaInterface(jzMediaIjkplayer);

        }

        if(isFirst) {
            ThreadUtil.startUIThread(1500, new Runnable() {
                @Override
                public void run() {
                    jzvdStd.startButton.performClick();
                }
            });

            isFirst = false;
        }
        else {
            jzvdStd.startButton.performClick();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermission() {
        if (!Settings.canDrawOverlays(HeaderAutoPlayerActivity.this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent,ACTION_MANAGE_OVERLAY_PERMISSION_CODE);
        }
        else {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put(Constants.RTMP_URL_KEY, Constants.RTMP_URL);
            EventCenter.getInstance().sendEvent(ESSArrows.OPEN_FLOATING_WINDOW, this, hashMap);
            finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // SYSTEM_ALERT_WINDOW permission not granted

            }
            else {

            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        ILog.iLogDebug(TAG, "onResume");
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);

        resumeLivePlayer();
    }

    @Override
    protected void onPause() {
        ILog.iLogDebug(TAG, "onPause");
        sensorManager.unregisterListener(sensorEventListener);
        Jzvd.backPress();
        Jzvd.releaseAllVideos();
        Jzvd.clearSavedProgress(this, Constants.RTMP_URL);
        super.onPause();
    }


    @Override
    public void onBackPressed() {
        if (Jzvd.backPress()) {
            return;
        }

        Jzvd.releaseAllVideos();
        Jzvd.clearSavedProgress(this, Constants.RTMP_URL);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}
