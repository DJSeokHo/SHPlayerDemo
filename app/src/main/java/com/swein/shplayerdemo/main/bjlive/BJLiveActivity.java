package com.swein.shplayerdemo.main.bjlive;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.faucamp.simplertmp.RtmpHandler;
import com.seu.magicfilter.utils.MagicFilterType;
import com.swein.shplayerdemo.R;
import com.swein.shplayerdemo.framework.util.debug.log.ILog;

import net.ossrs.yasea.SrsCameraView;
import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsPublisher;
import net.ossrs.yasea.SrsRecordHandler;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;

public class BJLiveActivity extends Activity  implements SrsRecordHandler.SrsRecordListener, SrsEncodeHandler.SrsEncodeListener, RtmpHandler.RtmpListener {

    private final static String TAG = "BJLiveActivity";

    private Button buttonStart;
    private Button buttonStop;
    private Button buttonFilter;
    private Button buttonImage;
    private Button buttonText;

    private SrsCameraView srsCameraView;
    private SrsPublisher srsPublisher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bjlive);

        findViews();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){

           ILog.iLogDebug(TAG, "good");

        }else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},100);

        }

        initCameraSize();
        initSrsPublisher();
    }

    private void findViews() {

        srsCameraView = findViewById(R.id.srsCameraView);

        buttonStart = findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });

        buttonStop = findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });

        buttonFilter = findViewById(R.id.buttonFilter);
        buttonFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                srsCameraView.setFilter(MagicFilterType.EARLYBIRD);
            }
        });

        buttonImage = findViewById(R.id.buttonImage);
        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        buttonText = findViewById(R.id.buttonText);
        buttonText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void initCameraSize() {

        Camera camera = Camera.open();

        List<Camera.Size> pictureSizes = camera.getParameters().getSupportedPictureSizes();
        List<Camera.Size> previewSizes = camera.getParameters().getSupportedPreviewSizes();

        for (int i=0; i<pictureSizes.size(); i++) {
            Camera.Size pSize = pictureSizes.get(i);
            Log.i(TAG, "---------------------PictureSize.width = " + pSize.width + "-----------------PictureSize.height = " + pSize.height);
        }

        for (int i=0; i<previewSizes.size(); i++) {
            Camera.Size pSize = previewSizes.get(i);
            Log.i(TAG, "--------------------previewSize.width = " + pSize.width + "-----------------previewSize.height = " + pSize.height);
        }

        camera.release();
    }

    private void initSrsPublisher() {

        srsPublisher = new SrsPublisher(srsCameraView);

        srsPublisher.setEncodeHandler(new SrsEncodeHandler(this));
        srsPublisher.setRecordHandler(new SrsRecordHandler(this));

        srsPublisher.setRtmpHandler(new RtmpHandler(this));

        srsPublisher.setPreviewResolution(1280, 720);

        srsPublisher.setOutputResolution(720, 1280);

        srsPublisher.setVideoHDMode();
//        srsPublisher.setVideoSmoothMode();
//        srsPublisher.switchCameraFilter(MagicFilterType.BEAUTY);

        srsPublisher.setScreenOrientation(getResources().getConfiguration().orientation);
//        srsPublisher.setScreenOrientation(Configuration.ORIENTATION_PORTRAIT);
//        srsPublisher.setScreenOrientation(Configuration.ORIENTATION_LANDSCAPE);
        srsPublisher.startCamera();

        srsPublisher.switchToHardEncoder();

    }

    private void start() {

//        srsPublisher.startPublish("rtmp://mobilemedia1.xst.kinxcdn.com/dotv/abcdef");
        srsPublisher.startPublish("rtmp://cdn.bestream.kr/dotv/reAd5f");
//        srsPublisher.startPublish("rtmp://cdn.bestream.kr/onnoff");
        srsPublisher.startCamera();
    }

    private void stop() {
        srsPublisher.stopPublish();
        srsPublisher.stopCamera();
    }

    @Override
    public void onNetworkWeak() {
        Toast.makeText(getApplicationContext(), "Network weak", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNetworkResume() {
        Toast.makeText(getApplicationContext(), "Network resume", Toast.LENGTH_SHORT).show();
    }

    private void handleException(Exception e) {
        try {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            srsPublisher.stopPublish();
            srsPublisher.stopRecord();
            //btnRecord.setText("record");
//            btnSwitchEncoder.setEnabled(true);
        }
        catch (Exception e1) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEncodeIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    @Override
    public void onRecordPause() {
        Toast.makeText(getApplicationContext(), "Record paused", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordResume() {
        Toast.makeText(getApplicationContext(), "Record resumed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordStarted(String msg) {
        Toast.makeText(getApplicationContext(), "Recording file: " + msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordFinished(String msg) {
        Toast.makeText(getApplicationContext(), "MP4 file saved: " + msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    @Override
    public void onRecordIOException(IOException e) {
        handleException(e);
    }

    @Override
    public void onRtmpConnecting(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpConnected(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpVideoStreaming() {

    }

    @Override
    public void onRtmpAudioStreaming() {

    }

    @Override
    public void onRtmpStopped() {
        Toast.makeText(getApplicationContext(), "Stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpDisconnected() {
        Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpVideoFpsChanged(double fps) {
        Log.i(TAG, String.format("Output Fps: %f", fps));
    }

    @Override
    public void onRtmpVideoBitrateChanged(double bitrate) {
        int rate = (int) bitrate;
        if (rate / 1000 > 0) {
            Log.i(TAG, String.format("Video bitrate: %f kbps", bitrate / 1000));
        } else {
            Log.i(TAG, String.format("Video bitrate: %d bps", rate));
        }
    }

    @Override
    public void onRtmpAudioBitrateChanged(double bitrate) {
        int rate = (int) bitrate;
        if (rate / 1000 > 0) {
            Log.i(TAG, String.format("Audio bitrate: %f kbps", bitrate / 1000));
        } else {
            Log.i(TAG, String.format("Audio bitrate: %d bps", rate));
        }
    }

    @Override
    public void onRtmpSocketException(SocketException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIOException(IOException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIllegalStateException(IllegalStateException e) {
        handleException(e);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case 100:

                if (permissions[0].equals(Manifest.permission.CAMERA)){

                    if (grantResults[0]  == PackageManager.PERMISSION_GRANTED){

                    }
                    else{

//                        startAlertDiaLog();
                    }

                }

                break;

        }

    }

    @Override
    protected void onDestroy() {
        stop();
        super.onDestroy();
    }
}
