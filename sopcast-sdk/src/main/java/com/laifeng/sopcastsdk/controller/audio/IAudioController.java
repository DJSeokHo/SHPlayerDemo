package com.laifeng.sopcastsdk.controller.audio;

import com.laifeng.sopcastsdk.audio.OnAudioEncodeListener;
import com.laifeng.sopcastsdk.configuration.AudioConfiguration;

public interface IAudioController {
    void start();
    void stop();
    void pause();
    void resume();
    void mute(boolean mute);
    int getSessionId();
    void setAudioConfiguration(AudioConfiguration audioConfiguration);
    void setAudioEncodeListener(OnAudioEncodeListener listener);
}
