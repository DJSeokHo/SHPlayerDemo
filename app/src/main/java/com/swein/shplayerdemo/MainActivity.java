package com.swein.shplayerdemo;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.swein.shplayerdemo.constants.Constants;
import com.swein.shplayerdemo.framework.util.activity.ActivityUtil;
import com.swein.shplayerdemo.framework.util.debug.log.ILog;
import com.swein.shplayerdemo.framework.util.eventsplitshot.eventcenter.EventCenter;
import com.swein.shplayerdemo.framework.util.eventsplitshot.subject.ESSArrows;
import com.swein.shplayerdemo.framework.util.intent.IntentUtil;
import com.swein.shplayerdemo.framework.util.size.DensityUtil;
import com.swein.shplayerdemo.framework.util.thread.ThreadUtil;
import com.swein.shplayerdemo.main.biglistitem.ListAutoTinyWindowActivity;
import com.swein.shplayerdemo.main.floatingview.live.FloatingViewHolder;
import com.swein.shplayerdemo.main.headautoplayer.live.HeaderAutoPlayerActivity;
import com.swein.shplayerdemo.main.headautoplayer.vod.VODAutoPlayerActivity;
import com.swein.shplayerdemo.main.watchdetail.WatchingDetailActivity;

import java.util.HashMap;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdMgr;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";

    private Button buttonLiveHeadAuto;
    private Button buttonVODHeadAuto;
    private Button buttonWatchDetail;
    private Button buttonListAuto;

    private WindowManager.LayoutParams layoutParams;
    private WindowManager windowManager;
    private FloatingViewHolder floatingViewHolder;



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

        EventCenter.getInstance().addEventObserver(ESSArrows.OPEN_FLOATING_WINDOW, this, new EventCenter.EventRunnable() {
            @Override
            public void run(String arrow, Object poster, HashMap<String, Object> data) {

               createFloatingWindow();

            }
        });



        buttonLiveHeadAuto = findViewById(R.id.buttonLiveHeadAuto);
        buttonLiveHeadAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.startNewActivityWithoutFinish(MainActivity.this, HeaderAutoPlayerActivity.class);
            }
        });

        buttonVODHeadAuto = findViewById(R.id.buttonVODHeadAuto);
        buttonVODHeadAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.startNewActivityWithoutFinish(MainActivity.this, VODAutoPlayerActivity.class);
            }
        });

        buttonWatchDetail = findViewById(R.id.buttonWatchDetail);
        buttonWatchDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.startNewActivityWithoutFinish(MainActivity.this, WatchingDetailActivity.class);
            }
        });

        buttonListAuto = findViewById(R.id.buttonListAuto);
        buttonListAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.startNewActivityWithoutFinish(MainActivity.this, ListAutoTinyWindowActivity.class);
            }
        });

//        ActivityUtil.startNewActivityWithFinish(this, VodUploadActivity.class);
//        ActivityUtil.startNewActivityWithFinish(this, BJLiveActivity.class);

    }

    private void openDetail() {

        removeFloatingViewHolder();
        Intent intent = new Intent(getApplicationContext(), HeaderAutoPlayerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.FROM_FLOATING_KEY, false);
        intent.putExtra(Constants.BUNDLE_KEY, bundle);
        startActivity(intent);

    }

    private void createFloatingWindow() {

        ILog.iLogDebug(TAG, "createFloatingWindow");

        if(JzvdMgr.getCurrentJzvd().currentScreen == Jzvd.SCREEN_WINDOW_FULLSCREEN) {
            Jzvd.backPress();
        }

        IntentUtil.intentStartActionBackToHome(MainActivity.this);

        ThreadUtil.startThread(new Runnable() {
            @Override
            public void run() {

                windowManager = (WindowManager) getApplication().getSystemService(WINDOW_SERVICE);

                if (floatingViewHolder != null){
                    windowManager.removeView(floatingViewHolder.getView());
                }

                floatingViewHolder = new FloatingViewHolder(MainActivity.this, new FloatingViewHolder.FloatingViewHolderDelegate() {

                    private float lastX;
                    private float lastY;
                    private float nowX;
                    private float nowY;
                    private float tranX;
                    private float tranY;

                    @Override
                    public void onButtonCloseClicked() {
                        removeFloatingViewHolderAndExit();
                    }

                    @Override
                    public void onButtonBackClicked() {
                        openDetail();
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
                        windowManager.updateViewLayout(floatingViewHolder.getView(), layoutParams);

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
                layoutParams.width = DensityUtil.dip2px(MainActivity.this, 250);
                layoutParams.height = DensityUtil.dip2px(MainActivity.this, 180);

                // floating window background
                layoutParams.format = PixelFormat.TRANSPARENT;


                ThreadUtil.startUIThread(1000, new Runnable() {
                    @Override
                    public void run() {
                        windowManager.addView(floatingViewHolder.getView(), layoutParams);
                        floatingViewHolder.setThumbnail();
                        floatingViewHolder.initLivePlayer();
                    }
                });
            }
        });
    }

    private boolean removeFloatingViewHolder() {
        if (floatingViewHolder != null){
            windowManager.removeView(floatingViewHolder.getView());
            floatingViewHolder = null;

            return true;
        }

        return false;
    }

    private boolean removeFloatingViewHolderAndExit() {
        if (floatingViewHolder != null){
            windowManager.removeView(floatingViewHolder.getView());
            floatingViewHolder = null;

            EventCenter.getInstance().sendEvent(ESSArrows.EXIT_APP, this, null);

            return true;
        }

        return false;
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        ILog.iLogDebug(TAG, "onRestart");

        if(floatingViewHolder != null) {
            openDetail();
        }
    }
}
