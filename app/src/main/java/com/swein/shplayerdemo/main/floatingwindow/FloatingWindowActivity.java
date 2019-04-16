package com.swein.shplayerdemo.main.floatingwindow;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.swein.shplayerdemo.R;
import com.swein.shplayerdemo.framework.util.size.DensityUtil;
import com.swein.shplayerdemo.main.floatingwindow.floatingview.FloatingViewHolder;

public class FloatingWindowActivity extends Activity {

    private final static int ACTION_MANAGE_OVERLAY_PERMISSION_CODE = 101;

    private WindowManager.LayoutParams layoutParams;
    private WindowManager windowManager;
    private FloatingViewHolder floatingViewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floating_window);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        }
    }

    private boolean removeFloatingViewHolder() {
        if (floatingViewHolder != null){
            windowManager.removeView(floatingViewHolder.getView());
            floatingViewHolder = null;
            return true;
        }

        return false;
    }

    private void init() {

        windowManager = (WindowManager) getApplication().getSystemService(WINDOW_SERVICE);

        if (floatingViewHolder != null){
            windowManager.removeView(floatingViewHolder.getView());
        }

        // use getApplicationContext
        floatingViewHolder = new FloatingViewHolder(getApplicationContext(), new FloatingViewHolder.FloatingViewHolderDelegate() {
            @Override
            public void onButtonCloseClicked() {
                removeFloatingViewHolder();
            }
        });

        // TYPE_SYSTEM_ALERT allow receive event
        // TYPE_SYSTEM_OVERLAY over system
        layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT | WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;

        // FLAG_NOT_TOUCH_MODAL not block event pass to behind 不阻塞事件传递到后面的窗口
        // FLAG_NOT_FOCUSABLE
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

        // floating window position
        layoutParams.gravity = Gravity.CENTER;

        layoutParams.x = 0;
        layoutParams.y = 0;

        // floating window size
        layoutParams.width = DensityUtil.dip2px(this, 250);
        layoutParams.height = DensityUtil.dip2px(this, 400);

        // floating window background
        layoutParams.format = PixelFormat.TRANSPARENT;
        windowManager.addView(floatingViewHolder.getView(), layoutParams);


        floatingViewHolder.getView().setOnTouchListener(new View.OnTouchListener() {

            private float lastX;
            private float lastY;
            private float nowX;
            private float nowY;
            private float tranX;
            private float tranY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean ret = false;
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:

                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        ret = true;
                        break;
                    case MotionEvent.ACTION_MOVE:

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
                        break;

                    case MotionEvent.ACTION_UP:
                        break;
                }
                return ret;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermission() {
        if (!Settings.canDrawOverlays(FloatingWindowActivity.this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent,ACTION_MANAGE_OVERLAY_PERMISSION_CODE);
        }
        else {
            init();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // SYSTEM_ALERT_WINDOW permission not granted
                finish();
            }
            else {
                init();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(removeFloatingViewHolder()) {
            return;
        }

        super.onBackPressed();
    }

}
