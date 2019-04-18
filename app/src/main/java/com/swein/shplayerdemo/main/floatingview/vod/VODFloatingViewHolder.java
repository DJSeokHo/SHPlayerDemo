package com.swein.shplayerdemo.main.floatingview.vod;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.swein.shplayerdemo.R;
import com.swein.shplayerdemo.constants.Constants;
import com.swein.shplayerdemo.framework.util.picasso.SHPicasso;
import com.swein.shplayerdemo.framework.util.thread.ThreadUtil;

import cn.jzvd.JZMediaManager;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class VODFloatingViewHolder {

    private final static String TAG = "VODFloatingViewHolder";

    private JzvdStd floatingJzvdStd;

    private ImageButton imageButtonClose;
    private ImageButton imageButtonBack;

    private View viewCover;

    public interface VODFloatingViewHolderDelegate {
        void onButtonCloseClicked();
        void onButtonBackClicked();
        void onActionDown(MotionEvent event);
        void onActionMove(MotionEvent event);
    }

    private View view;

    private VODFloatingViewHolderDelegate vodFloatingViewHolderDelegate;

    public VODFloatingViewHolder(Context context, VODFloatingViewHolderDelegate vodFloatingViewHolderDelegate) {
        this.vodFloatingViewHolderDelegate = vodFloatingViewHolderDelegate;
        view = LayoutInflater.from(context).inflate(R.layout.view_holder_vod_floating, null);

        imageButtonClose = view.findViewById(R.id.imageButtonClose);
        imageButtonBack = view.findViewById(R.id.imageButtonBack);
        viewCover = view.findViewById(R.id.viewCover);

        viewCover.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        vodFloatingViewHolderDelegate.onActionDown(event);
                    case MotionEvent.ACTION_MOVE:
                        vodFloatingViewHolderDelegate.onActionMove(event);

                    case MotionEvent.ACTION_UP:
                        floatingJzvdStd.startButton.performClick();
                }
                return false;
            }
        });

        imageButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                removePlayer();
                vodFloatingViewHolderDelegate.onButtonCloseClicked();
            }
        });

        imageButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                removePlayer();
                vodFloatingViewHolderDelegate.onButtonBackClicked();

            }
        });

        floatingJzvdStd = view.findViewById(R.id.jzvdStd);
    }

    public void initVODPlayer() {

        floatingJzvdStd.setJzvdStdDelegate(new JzvdStd.JzvdStdDelegate() {

            @Override
            public void onCompletion() {
                Log.d("??", "onCompletion");

                long position = JZMediaManager.instance().getCurrentPosition();
                Log.d("??","current " + " ---- " + position + "  total = " + floatingJzvdStd.getDuration());

                Constants.current = position;
            }

            @Override
            public void onAutoCompletion() {
                Constants.current = 0;
            }
        });

        floatingJzvdStd.setUp(Constants.VOD_URL, "vod", JzvdStd.SCREEN_WINDOW_FLOATING);
        SHPicasso.getInstance().loadImage(view.getContext(), "http://jzvd-pic.nathen.cn/jzvd-pic/1bb2ebbe-140d-4e2e-abd2-9e7e564f71ac.png", floatingJzvdStd.thumbImageView);
        ThreadUtil.startUIThread(1000, new Runnable() {
            @Override
            public void run() {
                floatingJzvdStd.seekToInAdvance = Constants.current;
                Jzvd.goOnPlayOnResume();
                floatingJzvdStd.startButton.performClick();
            }
        });
    }

    private void removePlayer() {
        Jzvd.backPress();
        Jzvd.releaseAllVideos();
        Jzvd.clearSavedProgress(view.getContext(), Constants.VOD_URL);
    }

    public View getView() {
        return view;
    }

    @Override
    protected void finalize() throws Throwable {
        Log.d("????????", "??????????????????????????????????????????????????????? finalize");
        super.finalize();
    }
}
