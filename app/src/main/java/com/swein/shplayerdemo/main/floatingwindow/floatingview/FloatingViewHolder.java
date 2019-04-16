package com.swein.shplayerdemo.main.floatingwindow.floatingview;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.swein.shplayerdemo.R;
import com.swein.shplayerdemo.framework.util.activity.ActivityUtil;
import com.swein.shplayerdemo.main.floatingwindow.FloatingWindowActivity;

public class FloatingViewHolder {

    private final static String TAG = "FloatingViewHolder";


    public interface FloatingViewHolderDelegate {
        void onButtonCloseClicked();
    }

    private View view;

    private Button buttonClose;
    private Button buttonBack;

    private FloatingViewHolderDelegate floatingViewHolderDelegate;

    public FloatingViewHolder(Context context, FloatingViewHolderDelegate floatingViewHolderDelegate) {
        this.floatingViewHolderDelegate = floatingViewHolderDelegate;
        view = LayoutInflater.from(context).inflate(R.layout.view_holder_floating, null);

        buttonBack = view.findViewById(R.id.buttonBack);
        buttonClose = view.findViewById(R.id.buttonClose);

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(view.getContext(), FloatingWindowActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                view.getContext().startActivity(intent);
            }
        });

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingViewHolderDelegate.onButtonCloseClicked();
            }
        });
    }

    public View getView() {
        return view;
    }
}
