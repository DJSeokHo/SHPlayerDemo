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
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.laifeng.sopcastsdk.camera.CameraHolder;
import com.laifeng.sopcastsdk.camera.CameraListener;
import com.laifeng.sopcastsdk.configuration.AudioConfiguration;
import com.laifeng.sopcastsdk.configuration.CameraConfiguration;
import com.laifeng.sopcastsdk.configuration.VideoConfiguration;
import com.laifeng.sopcastsdk.entity.Watermark;
import com.laifeng.sopcastsdk.stream.packer.rtmp.RtmpPacker;
import com.laifeng.sopcastsdk.stream.sender.rtmp.RtmpSender;
import com.laifeng.sopcastsdk.ui.CameraLivingView;
import com.laifeng.sopcastsdk.video.effect.GrayEffect;
import com.laifeng.sopcastsdk.video.effect.NullEffect;
import com.swein.shplayerdemo.R;
import com.swein.shplayerdemo.framework.util.bitmaps.BitmapUtil;
import com.swein.shplayerdemo.framework.util.debug.log.ILog;
import com.swein.shplayerdemo.framework.util.thread.ThreadUtil;
import com.swein.shplayerdemo.framework.util.toast.ToastUtil;
import com.swein.shplayerdemo.main.bjlive.imagelayer.ImageLayerViewHolder;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

public class BJLiveActivity extends Activity {

    private final static String TAG = "BJLiveActivity";

    private final static int REQUEST_PERMISSION_CODE = 101;

    private String[] permissions = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WAKE_LOCK};
    private List<String> permissionList = new ArrayList<>();

    private Button buttonMic;
    private Button buttonFlash;
    private Button buttonSwitch;
    private Button buttonFocus;
    private Button buttonColor;
    private Button buttonRecord;

    private Button buttonImage;
    private Button buttonText;

    private FrameLayout frameLayoutContainer;

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


    private float mCurrentScale = 1;

    private SoftReference<Bitmap> bitmapSoftReference;

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

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.water_mark);
        bitmapSoftReference  = new SoftReference<>(BitmapUtil.getScaleBitmap(bitmap, (int) (bitmap.getWidth() * 0.6), (int) (bitmap.getHeight() * 0.6)));
    }

    private void initEffects() {
        grayEffect = new GrayEffect(this);
        nullEffect = new NullEffect(this);
    }

    private void setWaterMark(Bitmap bitmap, int width, int height, float touchX, float touchY, int screenWidth, int screenHeight, float rotation) {
        Watermark watermark = new Watermark(bitmap, width, height, touchX, touchY, screenWidth, screenHeight, rotation);
        cameraLivingView.setWatermark(watermark);
    }

    private float screenXToOriginalX(float x, int screenWidth) {
        return x - (float)screenWidth * 0.5f;
    }

    private float screenYToOriginalY(float y, int screenHeight) {
       return -(y - (float)screenHeight * 0.5f);
    }

    private void findViews() {

        frameLayoutContainer = findViewById(R.id.frameLayoutContainer);

        cameraLivingView = findViewById(R.id.cameraLivingView);


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

        buttonImage = findViewById(R.id.buttonImage);
        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImageLayerViewHolder imageLayerViewHolder = new ImageLayerViewHolder(BJLiveActivity.this, new ImageLayerViewHolder.ImageLayerViewHolderDelegate() {
                    @Override
                    public void onActionUp(View view, int width, int height, float translationX, float translationY, float scale, float rotation) {
                        ILog.iLogDebug(TAG, view.getWidth() + " " + view.getHeight() + " " +
                                width + " " + height + " " + translationX + " " + translationY + " " + scale + " " + rotation);

                        setWaterMark(bitmapSoftReference.get(), (int)(width * scale), (int)(height * scale),
                                screenXToOriginalX(translationX, view.getWidth()), screenYToOriginalY(translationY, view.getHeight()),
                                view.getWidth(), view.getHeight(), rotation);

                    }
                });

                frameLayoutContainer.addView(imageLayerViewHolder.getView());

                ThreadUtil.startThread(new Runnable() {
                    @Override
                    public void run() {

                        ThreadUtil.startUIThread(0, new Runnable() {
                            @Override
                            public void run() {

                                imageLayerViewHolder.setImageView(bitmapSoftReference.get());
                            }
                        });

                    }
                });

            }
        });

        buttonText = findViewById(R.id.buttonText);
        buttonText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

//    private int getDeviceScreenWidth(Context context) {
//
//        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            return 0;
//        }
//
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//
//        ((Activity)context).getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
//        return displayMetrics.widthPixels;
//    }
//
//    private int getDeviceScreenHeight(Context context) {
//
//        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            return 0;
//        }
//
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//
//        ((Activity)context).getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
//        return displayMetrics.heightPixels;
//    }

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

    private void setRtmpSize(int width, int height) {
        rtmpSender.setVideoParams(width, height);
    }

    private void setBestRtmpSize() {
        Camera.Parameters params = CameraHolder.instance().getmCameraDevice().getParameters();
        List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
        for(int i = 0; i < previewSizes.size(); i++) {
            ILog.iLogDebug(TAG, String.valueOf(previewSizes.get(i).width) + " " + String.valueOf(previewSizes.get(i).height));
        }

        setRtmpSize(previewSizes.get(0).width, previewSizes.get(0).height);
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
                setBestRtmpSize();

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
