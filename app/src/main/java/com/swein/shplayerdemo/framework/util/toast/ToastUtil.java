package com.swein.shplayerdemo.framework.util.toast;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.swein.shplayerdemo.R;


public class ToastUtil {

    public static void showShortToastNormal(Context context, String string) {

        Toast.makeText(context, string, Toast.LENGTH_SHORT).show();

    }

    public static void showShortToastNormal(Context context, CharSequence string) {

        Toast.makeText(context, string, Toast.LENGTH_SHORT).show();

    }


    public static void showLongToastNormal(Context context, String string) {

        Toast.makeText(context, string, Toast.LENGTH_LONG).show();

    }

    public static void showCustomShortToastNormal(Context context, String string) {

        Toast toast = new Toast(context);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setBackgroundResource(R.drawable.toast_custom_background);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setPadding(5, 5, 5, 5);

        TextView textView = new TextView(context);
        textView.setText(string);
        textView.setPadding(5, 5, 5, 5);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(16);
        textView.setTextColor(Color.WHITE);
        linearLayout.addView(textView);

        toast.setView(linearLayout);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();

    }

    public static void showCustomLongToastNormal(Context context, String string) {

        Toast toast = new Toast(context);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setBackgroundResource(R.drawable.toast_custom_background);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setPadding(5, 5, 5, 5);

        TextView textView = new TextView(context);
        textView.setText(string);
        textView.setPadding(5, 5, 5, 5);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(16);
        textView.setTextColor(Color.WHITE);
        linearLayout.addView(textView);

        toast.setView(linearLayout);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void showCustomShortToastWithImageResourceId(Context context, String string, int resourceId) {

        Toast toast = new Toast(context);

        ImageView imageView = new ImageView(context);
        imageView.setImageResource(resourceId);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(5, 5, 5, 5);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setBackgroundResource(R.drawable.toast_custom_background);
        linearLayout.addView(imageView);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setPadding(5, 5, 5, 5);

        TextView textView = new TextView(context);
        textView.setText(string);
        textView.setPadding(5, 5, 5, 5);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(16);
        textView.setTextColor(Color.WHITE);
        linearLayout.addView(textView);

        toast.setView(linearLayout);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void showCustomLongToastWithImageResourceId(Context context, String string, int resourceId) {

        Toast toast = new Toast(context);

        ImageView imageView = new ImageView(context);
        imageView.setImageResource(resourceId);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(5, 5, 5, 5);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setBackgroundResource(R.drawable.toast_custom_background);
        linearLayout.addView(imageView);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setPadding(5, 5, 5, 5);

        TextView textView = new TextView(context);
        textView.setText(string);
        textView.setPadding(5, 5, 5, 5);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(16);
        textView.setTextColor(Color.WHITE);
        linearLayout.addView(textView);

        toast.setView(linearLayout);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }

}
