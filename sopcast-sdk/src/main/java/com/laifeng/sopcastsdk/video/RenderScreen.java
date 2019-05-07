package com.laifeng.sopcastsdk.video;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.laifeng.sopcastsdk.camera.CameraData;
import com.laifeng.sopcastsdk.camera.CameraHolder;
import com.laifeng.sopcastsdk.entity.GPUWatermark;
import com.laifeng.sopcastsdk.entity.Watermark;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class RenderScreen {

    private final static String TAG = "RenderScreen";

    private final FloatBuffer mNormalVtxBuf = GlUtil.createVertexBuffer();
    private final float[] mPosMtx = GlUtil.createIdentityMtx();

    private int mFboTexId;

    private int mProgram = -1;
    private int maPositionHandle = -1;
    private int maTexCoordHandle = -1;
    private int muPosMtxHandle = -1;
    private int muSamplerHandle = -1;

    private int mScreenW = -1;
    private int mScreenH = -1;

    /* camera buffer */
    private FloatBuffer mCameraTexCoordBuffer;
    private Bitmap mWatermarkImg;
    private float watermarkRotation;

    private float watermarkCoords[];
    private float textCoords[];

    private GPUWatermark gpuWatermark;
    private GPUWatermark textWatermark;

    private final float[] projectionMatrix = new float[16];

    public RenderScreen(int id) {
        mFboTexId = id;
        initGL();

        gpuWatermark = new GPUWatermark();
        textWatermark = new GPUWatermark();
    }

    public void setSreenSize(int width, int height) {
        mScreenW = width;
        mScreenH = height;

        initCameraTexCoordBuffer();

        GLES20.glClearColor(0f, 0f, 0f, 1f);
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        Log.d(TAG, String.valueOf(ratio));

        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    public void setTextureId(int textureId) {
        mFboTexId = textureId;
    }

    public void setVideoSize(int width, int height) {
        mScreenW = width;
        mScreenH = height;
    }

    private void initCameraTexCoordBuffer() {
        int cameraWidth, cameraHeight;
        CameraData cameraData = CameraHolder.instance().getCameraData();
        int width = cameraData.cameraWidth;
        int height = cameraData.cameraHeight;

        if (CameraHolder.instance().isLandscape()) {
            cameraWidth = Math.max(width, height);
            cameraHeight = Math.min(width, height);
        }
        else {
            cameraWidth = Math.min(width, height);
            cameraHeight = Math.max(width, height);
        }

        float hRatio = mScreenW / ((float) cameraWidth);
        float vRatio = mScreenH / ((float) cameraHeight);

        float ratio;
        if (hRatio > vRatio) {
            ratio = mScreenH / (cameraHeight * hRatio);
            final float vtx[] = {
                    //UV
                    0f, 0.5f + ratio / 2,
                    0f, 0.5f - ratio / 2,
                    1f, 0.5f + ratio / 2,
                    1f, 0.5f - ratio / 2,
            };
            ByteBuffer bb = ByteBuffer.allocateDirect(4 * vtx.length);
            bb.order(ByteOrder.nativeOrder());
            mCameraTexCoordBuffer = bb.asFloatBuffer();
            mCameraTexCoordBuffer.put(vtx);
            mCameraTexCoordBuffer.position(0);
        }
        else {
            ratio = mScreenW / (cameraWidth * vRatio);
            final float vtx[] = {
                    //UV
                    0.5f - ratio / 2, 1f,
                    0.5f - ratio / 2, 0f,
                    0.5f + ratio / 2, 1f,
                    0.5f + ratio / 2, 0f,
            };
            ByteBuffer bb = ByteBuffer.allocateDirect(4 * vtx.length);
            bb.order(ByteOrder.nativeOrder());
            mCameraTexCoordBuffer = bb.asFloatBuffer();
            mCameraTexCoordBuffer.put(vtx);
            mCameraTexCoordBuffer.position(0);
        }
    }

    public void setWatermark(Watermark watermark) {
        if (watermark != null) {

            if (mWatermarkImg != null && !mWatermarkImg.isRecycled()) {
                mWatermarkImg.recycle();
                mWatermarkImg = null;
            }

            mWatermarkImg = watermark.bitmap;
            watermarkRotation = watermark.rotation;

            initWatermarkVertexBuffer(
                    watermark.bottomLeftX, watermark.bottomLeftY,
                    watermark.topLeftX, watermark.topLeftY,
                    watermark.topRightX, watermark.topRightY,
                    watermark.bottomRightX, watermark.bottomRightY);

        }
        else {
            mWatermarkImg = null;
        }
    }

    private void initWatermarkVertexBuffer(
            float leftBottomX, float leftBottomY,
            float leftTopX, float leftTopY,
            float rightTopX, float rightTopY,
            float rightBottomX, float rightBottomY) {

        if (mScreenW <= 0 || mScreenH <= 0) {
            return;
        }

//        Log.d(TAG, String.valueOf(CameraHolder.instance().isLandscape()));
//        Log.d(TAG, rightBottomX + " " + rightBottomY);
//        Log.d(TAG, rightTopX + " " + rightTopY);
//        Log.d(TAG, leftBottomX + " " + leftBottomY);
//        Log.d(TAG, leftTopX + " " + leftTopY);


        if (CameraHolder.instance().isLandscape()) {
            watermarkCoords = new float[]{
                    -rightBottomY, -rightBottomX, 0.0f,
                    rightTopY, rightTopX, 0.0f,
                    leftBottomY, leftBottomX, 0.0f,
                    -leftTopY, -leftTopX, 0.0f,
            };
        } else {
            watermarkCoords = new float[]{
                    rightBottomX, rightBottomY, 0.0f,
                    rightTopX, rightTopY, 0.0f,
                    leftBottomX, leftBottomY, 0.0f,
                    leftTopX, leftTopY, 0.0f,
            };
        }

        gpuWatermark.setWatermarkCoords(watermarkCoords);
        gpuWatermark.setBitmap(mWatermarkImg);

        if (CameraHolder.instance().isLandscape()) {
            textCoords = new float[]{
                    -rightBottomY, -rightBottomX - 0.5f, 0.0f,
                    rightTopY, rightTopX - 0.5f, 0.0f,
                    leftBottomY, leftBottomX - 0.5f, 0.0f,
                    -leftTopY, -leftTopX - 0.5f, 0.0f,
            };
        } else {
            textCoords = new float[]{
                    rightBottomX, rightBottomY, 0.0f,
                    rightTopX, rightTopY, 0.0f,
                    leftBottomX, leftBottomY, 0.0f,
                    leftTopX, leftTopY, 0.0f,
            };
        }

        textWatermark.setWatermarkCoords(textCoords);
        textWatermark.setBitmap(mWatermarkImg);
    }

    public void draw() {

        if (mScreenW <= 0 || mScreenH <= 0) {
            return;
        }

        GlUtil.checkGlError("draw_S");

        GLES20.glViewport(0, 0, mScreenW, mScreenH);

        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);

        mNormalVtxBuf.position(0);
        GLES20.glVertexAttribPointer(maPositionHandle,
                3, GLES20.GL_FLOAT, false, 4 * 3, mNormalVtxBuf);
        GLES20.glEnableVertexAttribArray(maPositionHandle);

        mCameraTexCoordBuffer.position(0);
        GLES20.glVertexAttribPointer(maTexCoordHandle,
                2, GLES20.GL_FLOAT, false, 4 * 2, mCameraTexCoordBuffer);
        GLES20.glEnableVertexAttribArray(maTexCoordHandle);

        GLES20.glUniformMatrix4fv(muPosMtxHandle, 1, false, mPosMtx, 0);
        GLES20.glUniform1i(muSamplerHandle, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboTexId);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);


        float[] mMVPMatrix = getMVPMatrix(watermarkRotation);
        gpuWatermark.setMVPMatrix(mMVPMatrix);
        gpuWatermark.draw();

        float[] textMVPMatrix = getTestMVPMatrix(-watermarkRotation);
        textWatermark.setMVPMatrix(textMVPMatrix);
        textWatermark.draw();

        GlUtil.checkGlError("draw_E");
    }

    private float[] getMVPMatrix(float angle) {
        float[] mMVPMatrix = new float[16];
        float[] mRotationMatrix = new float[16];
        float[] mViewMatrix = getDefaultViewMatrix();


        Matrix.setRotateM(mRotationMatrix, 0, angle, 0, 0, -1.0f);

        Matrix.multiplyMM(mViewMatrix, 0, mRotationMatrix, 0, mViewMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, projectionMatrix, 0, mViewMatrix, 0);


        return mMVPMatrix;
    }

    private float[] getTestMVPMatrix(float angle) {
        float[] mMVPMatrix = new float[16];
        float[] mRotationMatrix = new float[16];
        float[] mViewMatrix = getDefaultViewMatrix();

        Matrix.setRotateM(mRotationMatrix, 0, angle, 0, 0, -1.0f);

        Matrix.multiplyMM(mViewMatrix, 0, mRotationMatrix, 0, mViewMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, projectionMatrix, 0, mViewMatrix, 0);


        return mMVPMatrix;
    }

    private float[] getDefaultViewMatrix() {
        float eyeX = 0f;
        float eyeY = 0f;
        float eyeZ = -3f;
        float centerX = 0f;
        float centerY = 0f;
        float centerZ = 0f;
        float upX = 0f;
        float upY = 1f;
        float upZ = 0f;

        float[] mViewMatrix = new float[16];
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
        return mViewMatrix;
    }

    private void initGL() {
        GlUtil.checkGlError("initGL_S");

        final String vertexShader =
                "attribute vec4 position;\n" +
                "attribute vec4 inputTextureCoordinate;\n" +
                "uniform   mat4 uPosMtx;\n" +
                "varying   vec2 textureCoordinate;\n" +
                "void main() {\n" +
                "  gl_Position = uPosMtx * position;\n" +
                "  textureCoordinate   = inputTextureCoordinate.xy;\n" +
                "}\n";

        final String fragmentShader =
                "precision mediump float;\n" +
                "uniform sampler2D uSampler;\n" +
                "varying vec2 textureCoordinate;\n" +
                "void main() {\n" +
                "  gl_FragColor = texture2D(uSampler, textureCoordinate);\n" +
                "}\n";

        mProgram = GlUtil.createProgram(vertexShader, fragmentShader);
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "position");
        maTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        muPosMtxHandle = GLES20.glGetUniformLocation(mProgram, "uPosMtx");
        muSamplerHandle = GLES20.glGetUniformLocation(mProgram, "uSampler");

        GlUtil.checkGlError("initGL_E");
    }


}
