package com.swein.shplayerdemo.main.biglistitem.item.viewholder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.swein.shplayerdemo.R;
import com.swein.shplayerdemo.framework.util.debug.log.ILog;
import com.swein.shplayerdemo.framework.util.picasso.SHPicasso;
import com.swein.shplayerdemo.main.biglistitem.item.model.ListAutoTinyWindowItemModel;

import java.lang.ref.WeakReference;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class ListAutoTinyWindowItemViewHolder extends RecyclerView.ViewHolder {

    private final static String TAG = "ListAutoTinyWindowItemViewHolder";

    private WeakReference<View> view;

    private JzvdStd jzvdStd;

    private TextView textViewSubTitle;
    private TextView textViewTitle;
    private View viewCover;

    private float touchDownX;

    private ListAutoTinyWindowItemModel listAutoTinyWindowItemModel;

//    private long durationTimeBetweenTouchDownAndTouchUp;


    public ListAutoTinyWindowItemViewHolder(@NonNull View itemView) {
        super(itemView);
        view = new WeakReference<>(itemView);
        findView();
    }

    public void setPlayer(ListAutoTinyWindowItemModel listAutoTinyWindowItemModel) {
        this.listAutoTinyWindowItemModel = listAutoTinyWindowItemModel;
        jzvdStd.setUp(listAutoTinyWindowItemModel.url, "title", Jzvd.SCREEN_WINDOW_LIST);
        SHPicasso.getInstance().loadImage(jzvdStd.getContext(), listAutoTinyWindowItemModel.imageUrl, jzvdStd.thumbImageView);

        textViewTitle.setText(listAutoTinyWindowItemModel.title);
        textViewSubTitle.setText(listAutoTinyWindowItemModel.subTitle);
    }

    private void findView() {
        jzvdStd = view.get().findViewById(R.id.jzvdStd);
        jzvdStd.pipButton.setVisibility(View.GONE);
        jzvdStd.fullscreenButton.setVisibility(View.GONE);

        textViewTitle = view.get().findViewById(R.id.textViewTitle);
        textViewSubTitle = view.get().findViewById(R.id.textViewSubTitle);
        viewCover = view.get().findViewById(R.id.viewCover);

        jzvdStd.setSoundEffectsEnabled(false);
        jzvdStd.startButton.setSoundEffectsEnabled(false);

        viewCover.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touchDownX = event.getX();
//                        durationTimeBetweenTouchDownAndTouchUp = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_MOVE:

                        break;

                    case MotionEvent.ACTION_UP:

                        float distance = Math.abs(event.getX() - touchDownX);

                        ILog.iLogDebug(TAG, distance);

                        if(distance > 20) {
                            autoPlay();
                        }
                        else {
                            ILog.iLogDebug(TAG, listAutoTinyWindowItemModel.title);
                        }
//                        long duration = System.currentTimeMillis() - durationTimeBetweenTouchDownAndTouchUp;

//                        ILog.iLogDebug(TAG, duration);
//
//                        if(duration > 100) {
//                            autoPlay();
//                        }
//                        else {
//                            ILog.iLogDebug(TAG, listAutoTinyWindowItemModel.title);
//                        }

                        break;
                }

                return true;
            }
        });
    }

    private void autoPlay() {
        if(jzvdStd.currentState != Jzvd.CURRENT_STATE_PLAYING) {
            jzvdStd.startButton.performClick();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        ILog.iLogDebug(TAG, "finalize");
        super.finalize();
    }
}
