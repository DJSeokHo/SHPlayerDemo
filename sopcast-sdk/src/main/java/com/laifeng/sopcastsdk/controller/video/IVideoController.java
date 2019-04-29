package com.laifeng.sopcastsdk.controller.video;

import com.laifeng.sopcastsdk.configuration.VideoConfiguration;
import com.laifeng.sopcastsdk.video.OnVideoEncodeListener;

public interface IVideoController {
    void start();
    void stop();
    void pause();
    void resume();
    boolean setVideoBps(int bps);
    void setVideoEncoderListener(OnVideoEncodeListener listener);
    void setVideoConfiguration(VideoConfiguration configuration);
}
