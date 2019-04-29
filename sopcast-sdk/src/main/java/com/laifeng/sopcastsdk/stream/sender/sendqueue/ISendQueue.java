package com.laifeng.sopcastsdk.stream.sender.sendqueue;

import com.laifeng.sopcastsdk.entity.Frame;

public interface ISendQueue {
    void start();
    void stop();
    void setBufferSize(int size);
    void putFrame(Frame frame);
    Frame takeFrame();
    void setSendQueueListener(SendQueueListener listener);
}
