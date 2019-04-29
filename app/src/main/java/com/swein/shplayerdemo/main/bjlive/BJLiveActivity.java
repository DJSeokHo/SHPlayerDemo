package com.swein.shplayerdemo.main.bjlive;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.laifeng.sopcastsdk.camera.CameraHolder;
import com.laifeng.sopcastsdk.camera.CameraListener;
import com.laifeng.sopcastsdk.configuration.AudioConfiguration;
import com.laifeng.sopcastsdk.configuration.CameraConfiguration;
import com.laifeng.sopcastsdk.configuration.VideoConfiguration;
import com.laifeng.sopcastsdk.entity.Watermark;
import com.laifeng.sopcastsdk.entity.WatermarkPosition;
import com.laifeng.sopcastsdk.stream.packer.rtmp.RtmpPacker;
import com.laifeng.sopcastsdk.stream.sender.rtmp.RtmpSender;
import com.laifeng.sopcastsdk.ui.CameraLivingView;
import com.laifeng.sopcastsdk.video.effect.GrayEffect;
import com.laifeng.sopcastsdk.video.effect.NullEffect;
import com.swein.shplayerdemo.R;
import com.swein.shplayerdemo.framework.util.debug.log.ILog;
import com.swein.shplayerdemo.framework.util.toast.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class BJLiveActivity extends Activity {

    private final static String TAG = "BJLiveActivity";

    private final static int REQUEST_PERMISSION_CODE = 101;

    private String[] permissions = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WAKE_LOCK};
    private List<String> permissionList = new ArrayList<>();

    private View viewCover;

    private Button buttonMic;
    private Button buttonFlash;
    private Button buttonSwitch;
    private Button buttonFocus;
    private Button buttonColor;
    private Button buttonRecord;

    private ProgressBar progressBar;

    private CameraLivingView cameraLivingView;
    private GestureDetector gestureDetector;

    private GrayEffect grayEffect;
    private NullEffect nullEffect;

    private boolean isGray;
    private boolean isRecording;
    private boolean isMute = false;
    private RtmpSender rtmpSender;
    private VideoConfiguration videoConfiguration;
    private int currentBps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bjlive);

        permissionList.clear();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }

        if (permissionList.size() > 0) {
            ActivityCompat.requestPermissions(this,permissions, REQUEST_PERMISSION_CODE);
        }
        else {
            init();
        }
    }

    private void init() {

        initEffects();
        findViews();

        initCameraView();
    }

    private void initEffects() {
        grayEffect = new GrayEffect(this);
        nullEffect = new NullEffect(this);
    }

    private void findViews() {

        cameraLivingView = findViewById(R.id.cameraLivingView);

        viewCover = findViewById(R.id.viewCover);
        viewCover.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    // 1920 1080 is max
                    ILog.iLogDebug(TAG, event.getX() + " " + event.getY());
                }

                return false;
            }
        });

        buttonMic = findViewById(R.id.buttonMic);
        buttonMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isMute) {
                    isMute = false;
                }
                else {
                    isMute = true;
                }

                cameraLivingView.mute(isMute);
            }
        });

        buttonFlash = findViewById(R.id.buttonFlash);
        buttonFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraLivingView.switchTorch();
            }
        });

        buttonSwitch = findViewById(R.id.buttonSwitch);
        buttonSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraLivingView.switchCamera();
            }
        });

        buttonFocus = findViewById(R.id.buttonFocus);
        buttonFocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraLivingView.switchFocusMode();
            }
        });

        buttonColor = findViewById(R.id.buttonColor);
        buttonColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isGray) {
                    cameraLivingView.setEffect(nullEffect);
                    isGray = false;
                } else {
                    cameraLivingView.setEffect(grayEffect);
                    isGray = true;
                }
            }
        });

        buttonRecord = findViewById(R.id.buttonRecord);
        buttonRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isRecording) {
                    progressBar.setVisibility(View.GONE);
                    ToastUtil.showShortToastNormal(BJLiveActivity.this, "stop living");
                    buttonRecord.setText("START");
                    cameraLivingView.stop();
                    isRecording = false;
                }
                else {
                    String uploadUrl = "rtmp://cdn.bestream.kr/dotv/reAd5f";
//                    String uploadUrl = "rtmp://mobilemedia1.xst.kinxcdn.com/dotv/abcdef";
//                    String uploadUrl = "rtmp://cdn.bestream.kr/onnoff";
                    rtmpSender.setAddress(uploadUrl);
                    progressBar.setVisibility(View.VISIBLE);
                    ToastUtil.showShortToastNormal(BJLiveActivity.this,"start connecting");
                    buttonRecord.setText("STOP");
                    rtmpSender.connect();
                    isRecording = true;

                }
            }
        });

        progressBar = findViewById(R.id.progressBar);
    }

    private void setWaterMark(Bitmap bitmap, int width, int height) {
        Watermark watermark = new Watermark(bitmap, width, height, WatermarkPosition.WATERMARK_ORIENTATION_BOTTOM_RIGHT, 100, 100);
        cameraLivingView.setWatermark(watermark);
    }

    /**
     * must check camera null
     * @param width
     * @param height
     */
    private void setCameraSize(int width, int height) {
        VideoConfiguration.Builder videoBuilder = new VideoConfiguration.Builder();
        videoBuilder.setSize(width, height);
        videoConfiguration = videoBuilder.build();
        cameraLivingView.setVideoConfiguration(videoConfiguration);
    }

    /**
     * must check camera null
     */
    private void setBestCameraSize() {

        Camera.Parameters params = CameraHolder.instance().getmCameraDevice().getParameters();
        List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
        for(int i = 0; i < previewSizes.size(); i++) {
            ILog.iLogDebug(TAG, String.valueOf(previewSizes.get(i).width) + " " + String.valueOf(previewSizes.get(i).height));
        }

        setCameraSize(previewSizes.get(0).width, previewSizes.get(0).height);
    }

    private void initCameraView() {

        cameraLivingView.init();

        CameraConfiguration.Builder cameraBuilder = new CameraConfiguration.Builder();
        cameraBuilder.setOrientation(CameraConfiguration.Orientation.LANDSCAPE)
                .setFacing(CameraConfiguration.Facing.BACK);
        CameraConfiguration cameraConfiguration = cameraBuilder.build();
        cameraLivingView.setCameraConfiguration(cameraConfiguration);



        // set camera open listener
        cameraLivingView.setCameraOpenListener(new CameraListener() {
            @Override
            public void onOpenSuccess() {
                ToastUtil.showShortToastNormal(BJLiveActivity.this, "camera open success");

                setBestCameraSize();

                Bitmap watermarkImg = BitmapFactory.decodeResource(getResources(), R.drawable.water_mark);
                setWaterMark(watermarkImg, 200, 150);

            }

            @Override
            public void onOpenFail(int error) {
                ToastUtil.showShortToastNormal(BJLiveActivity.this, "camera open fail");
            }

            @Override
            public void onCameraChange() {
                ToastUtil.showShortToastNormal(BJLiveActivity.this, "camera switch");
            }
        });

        gestureDetector = new GestureDetector(this, new GestureListener());
        cameraLivingView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return false;
            }
        });


        RtmpPacker packer = new RtmpPacker();
        packer.initAudioParams(AudioConfiguration.DEFAULT_FREQUENCY, 16, false);
        cameraLivingView.setPacker(packer);

        rtmpSender = new RtmpSender();

        rtmpSender.setVideoParams(1280, 720);
        rtmpSender.setAudioParams(AudioConfiguration.DEFAULT_FREQUENCY, 16, false);
        rtmpSender.setSenderListener(senderListener);
        cameraLivingView.setSender(rtmpSender);
        cameraLivingView.setLivingStartListener(new CameraLivingView.LivingStartListener() {
            @Override
            public void startError(int error) {

                ToastUtil.showShortToastNormal(BJLiveActivity.this, "start living fail");
                cameraLivingView.stop();
            }

            @Override
            public void startSuccess() {

                ToastUtil.showShortToastNormal(BJLiveActivity.this, "start living");
            }
        });

        rtmpSender.start();
    }

    private RtmpSender.OnSenderListener senderListener = new RtmpSender.OnSenderListener() {

        @Override
        public void onConnecting() {

        }

        @Override
        public void onConnected() {
            progressBar.setVisibility(View.GONE);
            cameraLivingView.start();
            currentBps = videoConfiguration.maxBps;
        }

        @Override
        public void onDisConnected() {
            progressBar.setVisibility(View.GONE);
            ToastUtil.showShortToastNormal(BJLiveActivity.this, "fail to live");
            buttonRecord.setText("START");
            cameraLivingView.stop();
            isRecording = false;
        }

        @Override
        public void onPublishFail() {
            progressBar.setVisibility(View.GONE);
            ToastUtil.showShortToastNormal(BJLiveActivity.this, "fail to publish stream");
            buttonRecord.setText("START");
            isRecording = false;
        }

        @Override
        public void onNetGood() {
            if (currentBps + 50 <= videoConfiguration.maxBps) {
                ILog.iLogDebug(TAG, "BPS_CHANGE good up 50");
                int bps = currentBps + 50;
                if (cameraLivingView != null) {
                    boolean result = cameraLivingView.setVideoBps(bps);
                    if (result) {
                        currentBps = bps;
                    }
                }
            }
            else {
                ILog.iLogDebug(TAG, "BPS_CHANGE good good good");
            }
            ILog.iLogDebug(TAG, "Current Bps: " + currentBps);
        }

        @Override
        public void onNetBad() {
            if (currentBps - 100 >= videoConfiguration.minBps) {
                ILog.iLogDebug(TAG, "BPS_CHANGE bad down 100");
                int bps = currentBps - 100;
                if (cameraLivingView != null) {
                    boolean result = cameraLivingView.setVideoBps(bps);
                    if (result) {
                        currentBps = bps;
                    }
                }
            } else {
                ILog.iLogDebug(TAG, "BPS_CHANGE bad down 100");
            }
            ILog.iLogDebug(TAG, "Current Bps: " + currentBps);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean hasPermissionDismiss = false;
        if (REQUEST_PERMISSION_CODE == requestCode){
            for (int grantResult : grantResults) {
                if (grantResult == -1) {
                    hasPermissionDismiss = true;
                    break;
                }
            }
        }

        if (hasPermissionDismiss) {
            finish();
        }
        else {
            // all ok
            init();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        cameraLivingView.pause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        cameraLivingView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraLivingView.stop();
        cameraLivingView.release();
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1.getX() - e2.getX() > 100 && Math.abs(velocityX) > 200) {
                // Fling left
                ToastUtil.showShortToastNormal(BJLiveActivity.this, "Fling Left");
            } else if (e2.getX() - e1.getX() > 100 && Math.abs(velocityX) > 200) {
                // Fling right
                ToastUtil.showShortToastNormal(BJLiveActivity.this, "Fling Right");
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }
}
