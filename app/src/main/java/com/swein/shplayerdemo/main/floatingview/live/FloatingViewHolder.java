package com.swein.shplayerdemo.main.floatingview.live;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.swein.shplayerdemo.R;
import com.swein.shplayerdemo.constants.Constants;
import com.swein.shplayerdemo.custom.JZMediaIjkplayer;
import com.swein.shplayerdemo.framework.util.debug.log.ILog;
import com.swein.shplayerdemo.framework.util.picasso.SHPicasso;
import com.swein.shplayerdemo.framework.util.thread.ThreadUtil;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class FloatingViewHolder {

    private final static String TAG = "FloatingViewHolder";

    private JzvdStd jzvdStd;
    private JZMediaIjkplayer jzMediaIjkplayer;

    private ImageButton imageButtonClose;
    private ImageButton imageButtonBack;

    private View viewCover;

    public interface FloatingViewHolderDelegate {
        void onButtonCloseClicked();
        void onButtonBackClicked();
        void onActionDown(MotionEvent event);
        void onActionMove(MotionEvent event);
    }

    private View view;

    private FloatingViewHolderDelegate floatingViewHolderDelegate;

    public FloatingViewHolder(Context context, FloatingViewHolderDelegate floatingViewHolderDelegate) {
        this.floatingViewHolderDelegate = floatingViewHolderDelegate;
        view = LayoutInflater.from(context).inflate(R.layout.view_holder_floating, null);

        imageButtonClose = view.findViewById(R.id.imageButtonClose);
        imageButtonBack = view.findViewById(R.id.imageButtonBack);
        viewCover = view.findViewById(R.id.viewCover);

        viewCover.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:

                        floatingViewHolderDelegate.onActionDown(event);
                        return true;

                    case MotionEvent.ACTION_MOVE:

                        floatingViewHolderDelegate.onActionMove(event);
                        return true;

                    case MotionEvent.ACTION_UP:

                        if(Jzvd.CURRENT_STATE_PLAYING != jzvdStd.currentState) {
                            jzvdStd.startButton.performClick();
                        }

                }
                return false;
            }
        });

        viewCover.setSoundEffectsEnabled(false);


        imageButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                removePlayer();
                floatingViewHolderDelegate.onButtonCloseClicked();
            }
        });

        imageButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                removePlayer();
                floatingViewHolderDelegate.onButtonBackClicked();

            }
        });

        jzvdStd = view.findViewById(R.id.jzvdStd);
        jzvdStd.setSoundEffectsEnabled(false);
        jzvdStd.startButton.setSoundEffectsEnabled(false);
    }

    public void setThumbnail() {
        SHPicasso.getInstance().loadImage(view.getContext(), "http://jzvd-pic.nathen.cn/jzvd-pic/1bb2ebbe-140d-4e2e-abd2-9e7e564f71ac.png", jzvdStd.thumbImageView);
    }

    public void initLivePlayer() {

        Jzvd.clearSavedProgress(view.getContext(), Constants.RTMP_URL);

        if(jzvdStd == null) {
            jzvdStd = view.findViewById(R.id.jzvdStd);
        }

        jzMediaIjkplayer = new JZMediaIjkplayer();
        jzvdStd.setUp(Constants.RTMP_URL, "test", JzvdStd.SCREEN_WINDOW_FLOATING);
        JzvdStd.setMediaInterface(jzMediaIjkplayer);

        ThreadUtil.startUIThread(1000, new Runnable() {
            @Override
            public void run() {
                jzvdStd.startButton.performClick();
            }
        });
    }

    private void removePlayer() {
        Jzvd.backPress();
        Jzvd.releaseAllVideos();
        Jzvd.clearSavedProgress(view.getContext(), Constants.RTMP_URL);
    }

    public View getView() {
        return view;
    }

    @Override
    protected void finalize() throws Throwable {
        ILog.iLogDebug(TAG, "??????????????????????????????????????????????????????? finalize");
        super.finalize();
    }
}
