package com.swein.shplayerdemo.main.headautoplayer.vod;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.swein.shplayerdemo.R;
import com.swein.shplayerdemo.constants.Constants;
import com.swein.shplayerdemo.custom.JZMediaIjkplayer;
import com.swein.shplayerdemo.framework.util.eventsplitshot.eventcenter.EventCenter;
import com.swein.shplayerdemo.framework.util.eventsplitshot.subject.ESSArrows;
import com.swein.shplayerdemo.framework.util.intent.IntentUtil;
import com.swein.shplayerdemo.framework.util.picasso.SHPicasso;
import com.swein.shplayerdemo.framework.util.size.DensityUtil;
import com.swein.shplayerdemo.framework.util.thread.ThreadUtil;
import com.swein.shplayerdemo.main.floatingview.vod.VODFloatingViewHolder;

import cn.jzvd.JZMediaManager;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdMgr;
import cn.jzvd.JzvdStd;

public class VODAutoPlayerActivity extends Activity {

    private final static int ACTION_MANAGE_OVERLAY_PERMISSION_CODE = 101;

    private JzvdStd jzvdStd;
    private JZMediaIjkplayer jzMediaIjkplayer;
    private Jzvd.JZAutoFullscreenListener sensorEventListener;
    private SensorManager sensorManager;

    private WindowManager.LayoutParams layoutParams;
    private WindowManager windowManager;
    private VODFloatingViewHolder vodFloatingViewHolder;

    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vodauto_player);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorEventListener = new Jzvd.JZAutoFullscreenListener();

        initLivePlayer();
    }

    private void initLivePlayer() {

        Jzvd.releaseAllVideos();
        Jzvd.clearSavedProgress(this, null);

        Jzvd.jzvdDelegate = new Jzvd.JzvdDelegate() {

            @Override
            public void onPIP() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkPermission();
                }
                else {
                    createFloatingWindow();
                }
            }
        };

        if(jzvdStd == null) {
            jzvdStd = findViewById(R.id.jzvdStd);
            SHPicasso.getInstance().loadImage(this, "http://jzvd-pic.nathen.cn/jzvd-pic/1bb2ebbe-140d-4e2e-abd2-9e7e564f71ac.png", jzvdStd.thumbImageView);
            jzvdStd.setJzvdStdDelegate(new JzvdStd.JzvdStdDelegate() {

                @Override
                public void onCompletion() {
                    Log.d("??", "onCompletion");

                    long position = JZMediaManager.instance().getCurrentPosition();
                    Log.d("??","current " + " ---- " + position + "  total = " + jzvdStd.getDuration());

                    Constants.current = position;
                }

                @Override
                public void onAutoCompletion() {
                    Log.d("??", "onAutoCompletion");

                    // clear record here
                    Constants.current = 0;
                }
            });
        }

        jzMediaIjkplayer = new JZMediaIjkplayer();
        jzvdStd.setUp(Constants.VOD_URL, "vod", JzvdStd.SCREEN_WINDOW_NORMAL);
        JzvdStd.setMediaInterface(jzMediaIjkplayer);

    }

    private void resumeLivePlayer() {

        if(jzvdStd == null) {

            Jzvd.releaseAllVideos();
            Jzvd.clearSavedProgress(this, Constants.VOD_URL);

            jzvdStd = findViewById(R.id.jzvdStd);
            SHPicasso.getInstance().loadImage(this, "http://jzvd-pic.nathen.cn/jzvd-pic/1bb2ebbe-140d-4e2e-abd2-9e7e564f71ac.png", jzvdStd.thumbImageView);

            jzMediaIjkplayer = new JZMediaIjkplayer();
            jzvdStd.setUp(Constants.VOD_URL, "vod", JzvdStd.SCREEN_WINDOW_NORMAL);
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
            jzvdStd.seekToInAdvance = Constants.current;
            Jzvd.goOnPlayOnResume();
            jzvdStd.startButton.performClick();
        }
    }

    private boolean removeFloatingViewHolder() {
        if (vodFloatingViewHolder != null){
            windowManager.removeView(vodFloatingViewHolder.getView());
            vodFloatingViewHolder = null;

            EventCenter.getInstance().sendEvent(ESSArrows.EXIT_APP, this, null);

            return true;
        }

        return false;
    }

    private void createFloatingWindow() {

        if(JzvdMgr.getCurrentJzvd().currentScreen == Jzvd.SCREEN_WINDOW_FULLSCREEN) {
            Jzvd.backPress();
        }

        IntentUtil.intentStartActionBackToHome(VODAutoPlayerActivity.this);

        ThreadUtil.startThread(new Runnable() {
            @Override
            public void run() {

                windowManager = (WindowManager) getApplication().getSystemService(WINDOW_SERVICE);

                if (vodFloatingViewHolder != null){
                    windowManager.removeView(vodFloatingViewHolder.getView());
                }

                vodFloatingViewHolder = new VODFloatingViewHolder(VODAutoPlayerActivity.this, new VODFloatingViewHolder.VODFloatingViewHolderDelegate() {

                    private float lastX;
                    private float lastY;
                    private float nowX;
                    private float nowY;
                    private float tranX;
                    private float tranY;

                    @Override
                    public void onButtonCloseClicked() {
                        removeFloatingViewHolder();
                    }

                    @Override
                    public void onButtonBackClicked() {
                        removeFloatingViewHolder();
                        Intent intent = new Intent(VODAutoPlayerActivity.this, VODAutoPlayerActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                    @Override
                    public void onActionDown(MotionEvent event) {
                        lastX = event.getRawX();
                        lastY = event.getRawY();
                    }

                    @Override
                    public void onActionMove(MotionEvent event) {

                        nowX = event.getRawX();
                        nowY = event.getRawY();

                        tranX = nowX - lastX;
                        tranY = nowY - lastY;

                        layoutParams.x += tranX;
                        layoutParams.y += tranY;

                        // update floating  window position
                        windowManager.updateViewLayout(vodFloatingViewHolder.getView(), layoutParams);

                        lastX = nowX;
                        lastY = nowY;

                    }
                });

                // TYPE_SYSTEM_ALERT allow receive event
                // TYPE_SYSTEM_OVERLAY over system
                layoutParams = new WindowManager.LayoutParams();
                layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT | WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;

                // FLAG_NOT_TOUCH_MODAL not block event pass to behind
                // FLAG_NOT_FOCUSABLE
                layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

                // floating window position
                layoutParams.gravity = Gravity.CENTER;

                layoutParams.x = 0;
                layoutParams.y = 0;

                // floating window size
                layoutParams.width = DensityUtil.dip2px(VODAutoPlayerActivity.this, 250);
                layoutParams.height = DensityUtil.dip2px(VODAutoPlayerActivity.this, 180);

                // floating window background
                layoutParams.format = PixelFormat.TRANSPARENT;


                ThreadUtil.startUIThread(1000, new Runnable() {
                    @Override
                    public void run() {
                        windowManager.addView(vodFloatingViewHolder.getView(), layoutParams);
                        vodFloatingViewHolder.initVODPlayer();
                    }
                });
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermission() {
        if (!Settings.canDrawOverlays(VODAutoPlayerActivity.this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent,ACTION_MANAGE_OVERLAY_PERMISSION_CODE);
        }
        else {
            createFloatingWindow();
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
                createFloatingWindow();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("???", "onResume");
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        Jzvd.goOnPlayOnResume();
        resumeLivePlayer();
    }

    @Override
    protected void onPause() {
        Log.d("???", "onPause");
        sensorManager.unregisterListener(sensorEventListener);

        Jzvd.backPress();
        Jzvd.releaseAllVideos();
        Jzvd.clearSavedProgress(this, Constants.VOD_URL);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (Jzvd.backPress()) {
            return;
        }

        Jzvd.releaseAllVideos();
        Jzvd.clearSavedProgress(this, Constants.VOD_URL);
        Constants.current = 0;
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

}
