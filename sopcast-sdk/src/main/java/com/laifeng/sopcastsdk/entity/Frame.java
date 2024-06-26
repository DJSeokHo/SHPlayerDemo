package com.laifeng.sopcastsdk.entity;

public class Frame<T> {
    public static final int FRAME_TYPE_AUDIO = 1;
    public static final int FRAME_TYPE_KEY_FRAME = 2;
    public static final int FRAME_TYPE_INTER_FRAME = 3;
    public static final int FRAME_TYPE_CONFIGURATION = 4;

    public T data;
    public int packetType;
    public int frameType;

    public Frame(T data, int packetType, int frameType) {
        this.data = data;
        this.packetType = packetType;
        this.frameType = frameType;
    }
}
