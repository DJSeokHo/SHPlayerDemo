package com.laifeng.sopcastsdk.entity;

import android.graphics.Bitmap;
import android.util.Log;

public class Watermark {

    public Bitmap bitmap;
    public int width;
    public int height;

    // top left
    public float topLeftX;
    public float topLeftY;

    // bottom left
    public float bottomLeftX;
    public float bottomLeftY;

    // top right
    public float topRightX;
    public float topRightY;

    // bottom right
    public float bottomRightX;
    public float bottomRightY;


    public Watermark(Bitmap bitmap, int width, int height, float centX, float centY, int screenWidth, int screenHeight, float rotation) {
        this.bitmap = bitmap;
        this.width = width;
        this.height = height;

        float oX = centX / (screenWidth * 0.5f);
        float oY = centY / (screenHeight * 0.5f);
        Log.d("???", width + " " + height + " " + oX + " " + oY + " " + screenWidth + " " + screenHeight);

        float offsetX = (float) width / (float) screenWidth;
        float offsetY = (float) height / (float) screenHeight;

        // top left
        topLeftX = oX - offsetX;
        topLeftY = oY + offsetY;

        // bottom left
        bottomLeftX = oX - offsetX;
        bottomLeftY = oY - offsetY;

        // top right
        topRightX = oX + offsetX;
        topRightY = oY + offsetY;

        // bottom right
        bottomRightX = oX + offsetX;
        bottomRightY = oY - offsetY;

        Log.d("???", topLeftX + " - " + topLeftY);
        Log.d("???", bottomLeftX + " - " + bottomLeftY);
        Log.d("???", topRightX + " - " + topRightY);
        Log.d("???", bottomRightX + " - " + bottomRightY);

        if(rotation != 0) {

            // top left
            topLeftX = getRotationX(oX, oY, topLeftX, topLeftY, rotation);
            topLeftY = getRotationY(oX, oY, topLeftX, topLeftY, rotation);

            // bottom left
            bottomLeftX = getRotationX(oX, oY, bottomLeftX, bottomLeftY, rotation);
            bottomLeftY = getRotationY(oX, oY, bottomLeftX, bottomLeftY, rotation);

            // top right
            topRightX = getRotationX(oX, oY, topRightX, topRightY, rotation);
            topRightY = getRotationY(oX, oY, topRightX, topRightY, rotation);

            // bottom right
            bottomRightX = getRotationX(oX, oY, bottomRightX, bottomRightY, rotation);
            bottomRightY = getRotationY(oX, oY, bottomRightX, bottomRightY, rotation);

            Log.d("???", topLeftX + " - " + topLeftY);
            Log.d("???", bottomLeftX + " - " + bottomLeftY);
            Log.d("???", topRightX + " - " + topRightY);
            Log.d("???", bottomRightX + " - " + bottomRightY);
        }

    }

    /**
     *  x′ = (x0 － xcenter) cosθ － (y0 － ycenter) sinθ ＋ xcenter;
     *  y′ = (x0 － xcenter) sinθ ＋ (y0 － ycenter) cosθ ＋ ycenter;
     */
    private float getRotationX(float oX, float oY, float x, float y, float rotation) {

        float l = (float) ((rotation * Math.PI) / 180);
        float cosv = (float) Math.cos(l);
        float sinv = (float) Math.sin(l);

        return ((x - oX) * cosv - (y - oY) * sinv + oX);
    }

    /**
     *  x′ = (x0 － xcenter) cosθ － (y0 － ycenter) sinθ ＋ xcenter;
     *  y′ = (x0 － xcenter) sinθ ＋ (y0 － ycenter) cosθ ＋ ycenter;
     */
    private float getRotationY(float oX, float oY, float x, float y, float rotation) {

        float l = (float) ((rotation * Math.PI) / 180);
        float cosv = (float) Math.cos(l);
        float sinv = (float) Math.sin(l);

        return ((x - oX) * sinv + (y - oY) * cosv + oY);
    }

}
