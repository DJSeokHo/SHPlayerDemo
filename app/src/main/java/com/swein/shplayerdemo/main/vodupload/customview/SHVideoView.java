package com.swein.shplayerdemo.main.vodupload.customview;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

import com.swein.shplayerdemo.R;
import com.swein.shplayerdemo.framework.util.debug.log.ILog;

import java.io.IOException;

public class SHVideoView extends SurfaceView {

    private static final String TAG = "SHVideoView";
    private boolean isReady = false;
    private int position = 0;
    private String filePath = "";
    private MediaPlayer player;

    public SHVideoView(Context context) {
        super(context);
    }

    public SHVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);

        player = new MediaPlayer();

        getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                ILog.iLogDebug(TAG, "surfaceCreated");

                isReady = true;
                player.setDisplay(getHolder());

                if (!"".equals(filePath) && !player.isPlaying()) {
                    try {
                        player.reset();
                        player.setDataSource(filePath);
                        player.prepare();
                        player.seekTo(position);
                        ILog.iLogDebug(TAG, "current time：" + position);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                ILog.iLogDebug(TAG, "surfaceChanged");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                isReady = false;

                ILog.iLogDebug(TAG, "surfaceDestroyed");

                if (player.isPlaying()) {

                    position = player.getCurrentPosition();
                    ILog.iLogDebug(TAG, "current time：" + position);
                    player.stop();
                }
            }
        });
    }

    public SHVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setVideoPath(String filePath) {
        this.filePath = filePath;

        if (isReady) {

            try {
                player.reset();
                player.setDataSource(filePath);
                player.prepare();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getCurrentPosition() {
        if (player != null) {
            return player.getCurrentPosition();
        }
        return 0;
    }

    public void start() {
        if (player != null && !player.isPlaying()) {
            player.start();
        }
    }

    public void seekTo(int startTime) {
        if (player != null && player.isPlaying()) {
            player.seekTo(startTime);
        }
    }

    public void pause() {
        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }

    public void stop() {
        if (player != null) {
            player.stop();
        }
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener listener) {
        if (player != null) {
            player.setOnPreparedListener(listener);
        }
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        if (player != null) {
            player.setOnCompletionListener(listener);
        }
    }

    public void setOnErrorListener(MediaPlayer.OnErrorListener listener) {
        if (player != null) {
            player.setOnErrorListener(listener);
        }
    }

    public void setOnVideoSizeChangedListener() {
        player.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                changeVideoSize();
            }
        });
    }

    public void changeVideoSize() {
        int videoWidth = player.getVideoWidth();
        int videoHeight = player.getVideoHeight();

        int surfaceWidth = getWidth();
        int surfaceHeight = getHeight();

        float max;
        if (getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {

            max = Math.max((float) videoWidth / (float) surfaceWidth, (float) videoHeight / (float) surfaceHeight);
        }
        else {
            //横屏模式下按视频高度计算放大倍数值
            max = Math.max(((float) videoWidth / (float) surfaceHeight), (float) videoHeight / (float) surfaceWidth);
        }

        videoWidth = (int) Math.ceil((float) videoWidth / max);
        videoHeight = (int) Math.ceil((float) videoHeight / max);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(videoWidth, videoHeight);
        params.addRule(RelativeLayout.CENTER_VERTICAL, R.id.shVideoViewRelativeLayoutParent);
        setLayoutParams(params);
    }


    public void release() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }
}
