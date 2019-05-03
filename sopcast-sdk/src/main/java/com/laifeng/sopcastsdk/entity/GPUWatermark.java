package com.laifeng.sopcastsdk.entity;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GPUWatermark {

    private final static String TAG = "GPUWatermark";

    private static final String FIELD_POSITION = "vPosition";
    private static final String FIELD_MATRIX_MVP = "uMVPMatrix";

    private static final String FIELD_A_TEX_COORD = "a_texCoord";
    private static final String FIELD_V_TEX_COORD = "v_texCoord";
    private static final String FIELD_S_TEXTURE = "s_texture";

    private static final String SHADER_CODE_BITMAP_VERTEX_MVP =
            "uniform mat4 " + FIELD_MATRIX_MVP + ";" +
                    "attribute vec4 " + FIELD_POSITION + ";" +
                    "attribute vec2 " + FIELD_A_TEX_COORD + ";" +
                    "varying vec2 " + FIELD_V_TEX_COORD + ";" +
                    "void main() {" +
                    "  gl_Position = " + FIELD_MATRIX_MVP + " * " + FIELD_POSITION + ";" +
                    "  " + FIELD_V_TEX_COORD + " = " + FIELD_A_TEX_COORD + ";" +
                    "}";

    private static final String SHADER_CODE_BITMAP_FRAGMENT =
            "precision mediump float;" +
                    "varying vec2 " + FIELD_V_TEX_COORD + ";" +
                    "uniform sampler2D " + FIELD_S_TEXTURE + ";" +
                    "void main(){" +
                    "   gl_FragColor = texture2D(" + FIELD_S_TEXTURE + "," + FIELD_V_TEX_COORD + ");" +
                    "}";

    private FloatBuffer watermarkVertexBuffer;

    private float[] MVPMatrix;

    private Bitmap bitmap;

    private static final int COORDS_PER_VERTEX = 3;
    private static final int BYTES_PER_FLOAT = 4;

    private float watermarkCoords[];

    private int program;

    private int positionHandle;
    private int MVPMatrixHandle;
    private int aTexCoordHandle;
    private int sTexSamplerHandle;

    private float normalTexCoord[] = {
            // UV
            0f, 1f,
            0f, 0f,
            1f, 1f,
            1f, 0f,
    };

    private FloatBuffer normalTexCoordBuf;

    private int watermarkTextureId = -1;

    public GPUWatermark() {

        program = createShaderProgram(SHADER_CODE_BITMAP_VERTEX_MVP, SHADER_CODE_BITMAP_FRAGMENT);

    }

    private int createShaderProgram(String vertexShaderCode, String fragmentShaderCode) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        int shaderProgram = GLES20.glCreateProgram();

        GLES20.glAttachShader(shaderProgram, vertexShader);
        GLES20.glAttachShader(shaderProgram, fragmentShader);

        GLES20.glLinkProgram(shaderProgram);

        return shaderProgram;
    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public void setWatermarkCoords(float watermarkCoords[]) {

        ByteBuffer bb = ByteBuffer.allocateDirect(4 * normalTexCoord.length);
        bb.order(ByteOrder.nativeOrder());
        normalTexCoordBuf = bb.asFloatBuffer();
        normalTexCoordBuf.put(normalTexCoord);
        normalTexCoordBuf.position(0);

        this.watermarkCoords = watermarkCoords;

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(watermarkCoords.length * BYTES_PER_FLOAT);
        byteBuffer.order(ByteOrder.nativeOrder());
        watermarkVertexBuffer = byteBuffer.asFloatBuffer();
        watermarkVertexBuffer.put(watermarkCoords);
        watermarkVertexBuffer.position(0);

    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setMVPMatrix(float[] matrix){
        MVPMatrix = matrix;
    }

    public void draw() {

        if(bitmap == null) {
            return;
        }

        GLES20.glUseProgram(program);

        positionHandle = GLES20.glGetAttribLocation(program, FIELD_POSITION);
        MVPMatrixHandle = GLES20.glGetUniformLocation(program, FIELD_MATRIX_MVP);
        aTexCoordHandle = GLES20.glGetAttribLocation(program, FIELD_A_TEX_COORD);
        sTexSamplerHandle = GLES20.glGetUniformLocation(program, FIELD_S_TEXTURE);

        GLES20.glUniformMatrix4fv(MVPMatrixHandle, 1, false, MVPMatrix, 0);
        GLES20.glUniform1i(sTexSamplerHandle, 0);

        watermarkVertexBuffer.position(0);
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, COORDS_PER_VERTEX * BYTES_PER_FLOAT, watermarkVertexBuffer);

        normalTexCoordBuf.position(0);
        GLES20.glVertexAttribPointer(aTexCoordHandle, 2, GLES20.GL_FLOAT, false, 4 * 2, normalTexCoordBuf);
        GLES20.glEnableVertexAttribArray(aTexCoordHandle);

        if (watermarkTextureId == -1) {

            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,  GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            // TODO need recycle this

//            bitmap.recycle();

            watermarkTextureId = textures[0];
        }

        // important !!!
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, watermarkTextureId);
        // important !!!

        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, watermarkCoords.length / COORDS_PER_VERTEX);

//        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisable(GLES20.GL_BLEND);
    }


}
