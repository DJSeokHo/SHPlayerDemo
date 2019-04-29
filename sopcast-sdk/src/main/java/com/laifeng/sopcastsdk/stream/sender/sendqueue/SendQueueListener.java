package com.laifeng.sopcastsdk.stream.sender.sendqueue;

public interface SendQueueListener {
    void good();
    void bad();
}
