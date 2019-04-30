package com.swein.shplayerdemo.main.bjlive.imagelayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.swein.shplayerdemo.R;
import com.swein.shplayerdemo.custom.TranslateScaleRotationContainerView;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

public class ImageLayerViewHolder {

    private final static String TAG = "ImageLayerViewHolder";

    private SoftReference<View> viewSoftReference;
    private ImageView imageView;
    private WeakReference<Bitmap> bitmapWeakReference;

    private TranslateScaleRotationContainerView translateScaleRotationContainerView;

    public interface ImageLayerViewHolderDelegate {
        void onActionUp(View view, int width, int height, float translationX, float translationY, float scale, float rotation);
    }

    private ImageLayerViewHolderDelegate imageLayerViewHolderDelegate;

    public ImageLayerViewHolder(Context context, ImageLayerViewHolderDelegate imageLayerViewHolderDelegate) {

        this.imageLayerViewHolderDelegate = imageLayerViewHolderDelegate;
        viewSoftReference = new SoftReference<>(LayoutInflater.from(context).inflate(R.layout.view_holder_image_layer, null));

        findView();
    }

    private void findView() {
        imageView = viewSoftReference.get().findViewById(R.id.imageView);
        translateScaleRotationContainerView = viewSoftReference.get().findViewById(R.id.translateScaleRotationContainerView);

        translateScaleRotationContainerView.setImageLayerViewDelegate(new TranslateScaleRotationContainerView.ImageLayerViewDelegate() {
            @Override
            public void onActionUp(float translationX, float translationY, float scale, float rotation) {
                translationX = translationX + imageView.getWidth() * 0.5f;
                translationY = translationY + imageView.getHeight() * 0.5f;

                imageLayerViewHolderDelegate.onActionUp(viewSoftReference.get(), imageView.getWidth(), imageView.getHeight(), translationX, translationY, scale, rotation);
            }
        });
    }

    public void setImageView(Bitmap bitmap) {
        bitmapWeakReference = new WeakReference<>(bitmap);
        imageView.setImageBitmap(bitmapWeakReference.get());
    }


    public View getView() {
        return viewSoftReference.get();
    }
}
