package com.swein.shplayerdemo.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class TranslateScaleRotationContainerView extends RelativeLayout {

    private float translationX;
    private float translationY;
    private float scale = 1;
    private float rotation;

    private float actionX;
    private float actionY;
    private float spacing;
    private float degree;
    private int moveType;

    public interface ImageLayerViewDelegate {
        void onActionUp(float translationX, float translationY, float scale, float rotation);
    }

    private ImageLayerViewDelegate imageLayerViewDelegate;

    public void setImageLayerViewDelegate(ImageLayerViewDelegate imageLayerViewDelegate) {
        this.imageLayerViewDelegate = imageLayerViewDelegate;
    }

    public TranslateScaleRotationContainerView(Context context) {
        this(context, null);
    }

    public TranslateScaleRotationContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TranslateScaleRotationContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                moveType = 1;
                actionX = event.getRawX();
                actionY = event.getRawY();
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                moveType = 2;
                spacing = getSpacing(event);
                degree = getDegree(event);
                break;

            case MotionEvent.ACTION_MOVE:

                if (moveType == 1) {
                    /*
                        move
                     */
                    translationX = translationX + event.getRawX() - actionX;
                    translationY = translationY + event.getRawY() - actionY;
                    setTranslationX(translationX);
                    setTranslationY(translationY);
                    actionX = event.getRawX();
                    actionY = event.getRawY();
                }
                else if (moveType == 2) {
                    /*
                        scale
                     */
                    scale = scale * getSpacing(event) / spacing;
                    setScaleX(scale);
                    setScaleY(scale);
                    rotation = rotation + getDegree(event) - degree;
                    if (rotation > 360) {
                        rotation = rotation - 360;
                    }
                    if (rotation < -360) {
                        rotation = rotation + 360;
                    }
                    setRotation(rotation);
                }

                break;

            case MotionEvent.ACTION_UP:
                imageLayerViewDelegate.onActionUp(translationX, translationY, scale, rotation);
                break;
            case MotionEvent.ACTION_POINTER_UP:

                moveType = 0;

                imageLayerViewDelegate.onActionUp(translationX, translationY, scale, rotation);
                break;
        }
        return super.onTouchEvent(event);
    }

    private float getSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }


    private float getDegree(MotionEvent event) {
        double delta_x = event.getX(0) - event.getX(1);
        double delta_y = event.getY(0) - event.getY(1);
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }
}