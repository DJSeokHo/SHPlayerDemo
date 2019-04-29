package com.laifeng.sopcastsdk.stream.sender.rtmp.io;

import com.laifeng.sopcastsdk.stream.sender.rtmp.packets.Chunk;

public interface OnReadListener {
    void onChunkRead(Chunk chunk);
    void onDisconnect();
    void onStreamEnd();
}
