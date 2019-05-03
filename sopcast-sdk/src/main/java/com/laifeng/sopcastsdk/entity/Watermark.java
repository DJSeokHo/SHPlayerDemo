package com.laifeng.sopcastsdk.entity;

import android.graphics.Bitmap;

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

    public float rotation;

    public Watermark(Bitmap bitmap, int width, int height, float centX, float centY, int screenWidth, int screenHeight, float rotation) {
        this.bitmap = bitmap;
        this.width = width;
        this.height = height;
        this.rotation = rotation;

        float oX = centX / (screenWidth * 0.5f);
        float oY = centY / (screenHeight * 0.5f);

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

//        Log.d("???", "[ " + topLeftX + " " + topLeftY + " ]" + " " + "[ " + oX + " " + oY + " ]");
//        Log.d("???", bottomLeftX + " - " + bottomLeftY);
//        Log.d("???", topRightX + " - " + topRightY);
//        Log.d("???", bottomRightX + " - " + bottomRightY);

    }

}
