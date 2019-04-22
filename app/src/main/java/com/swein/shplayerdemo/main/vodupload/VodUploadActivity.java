package com.swein.shplayerdemo.main.vodupload;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.swein.shplayerdemo.R;
import com.swein.shplayerdemo.framework.util.bitmaps.BitmapUtil;
import com.swein.shplayerdemo.framework.util.debug.log.ILog;
import com.swein.shplayerdemo.framework.util.files.FilesUtil;
import com.swein.shplayerdemo.framework.util.thread.ThreadUtil;
import com.swein.shplayerdemo.main.vodupload.customview.SHVideoView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VodUploadActivity extends Activity {

    private final static String TAG = "VodUploadActivity";

    private final static int MAX_SEEK_BAR_PREVIEW_NUMBER = 10;
    private final static int MAX_SELECT_COVER_NUMBER = 3;

    private MediaMetadataRetriever mediaMetadataRetriever;

    private ImageView imageView;
    private ImageView imageViewCoverOne;
    private ImageView imageViewCoverTwo;
    private ImageView imageViewCoverThree;
    private Button buttonSelect;

    private FrameLayout frameLayoutProgress;

    private List<Bitmap> tempBitmapList;
    private List<Bitmap> coverBitmapList;

    private Bitmap result;
    private Bitmap preview;

    private SHVideoView shVideoView;
    private SeekBar seekBar;

    private ImageView imageViewPlay;
    private ImageView imageViewPreview;
    private View viewPlayerPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod_upload);

        initData();
        findView();
        initSeekBar();
        setListener();
        disableSeekBar();
    }

    private void initData() {
        mediaMetadataRetriever = new MediaMetadataRetriever();
    }

    private void findView() {
        shVideoView = findViewById(R.id.shVideoView);
        seekBar = findViewById(R.id.seekBar);
        seekBar.setEnabled(false);

        imageView = findViewById(R.id.imageView);
        buttonSelect = findViewById(R.id.buttonSelect);
        frameLayoutProgress = findViewById(R.id.frameLayoutProgress);

        imageViewCoverOne = findViewById(R.id.imageViewCoverOne);
        imageViewCoverTwo = findViewById(R.id.imageViewCoverTwo);
        imageViewCoverThree = findViewById(R.id.imageViewCoverThree);

        imageViewPlay = findViewById(R.id.imageViewPlay);
        viewPlayerPreview = findViewById(R.id.viewPlayerPreview);
        imageViewPreview = findViewById(R.id.imageViewPreview);
    }

    private void setListener() {
        viewPlayerPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleVideo();
            }
        });

        shVideoView.setOnVideoSizeChangedListener();

        shVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                ILog.iLogDebug(TAG, "onCompletion");
                showPreview();
                imageViewPlay.setVisibility(View.VISIBLE);
                resetSeekBar();
                disableSeekBar();
            }
        });

        shVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });

        shVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                imageViewPlay.setVisibility(View.VISIBLE);
                disableSeekBar();
                ILog.iLogDebug(TAG, "onPrepared");
            }
        });

        buttonSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFile();
            }
        });
    }

    private void initSeekBar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            boolean isTouch = false;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if(isTouch) {
                    shVideoView.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isTouch = false;
            }
        });
    }

    private void toggleVideo() {

        hidePreview();

        if(shVideoView == null) {
            return;
        }

        if(shVideoView.isPlaying()) {
            shVideoView.pause();
        }
        else {
            shVideoView.start();
        }

        if(shVideoView.isPlaying()) {
            imageViewPlay.setVisibility(View.GONE);
            enableSeekBar();
            syncSeekBar();
        }
        else {
            imageViewPlay.setVisibility(View.VISIBLE);
            disableSeekBar();
        }
    }

    private void enableSeekBar() {
        seekBar.setEnabled(true);
    }

    private void disableSeekBar() {
        seekBar.setEnabled(false);
    }

    private void resetSeekBar() {
        seekBar.setProgress(0);
    }

    private void syncSeekBar() {

        ThreadUtil.startThread(new Runnable() {
            @Override
            public void run() {

                while (shVideoView.isPlaying()) {
                    ThreadUtil.startUIThread(1000, new Runnable() {
                        @Override
                        public void run() {
                            seekBar.setProgress((int) (shVideoView.getCurrentPosition() * 0.001));
                        }
                    });
                }
            }
        });
    }

    private void initPreview() {
        preview = BitmapUtil.extractFrame(shVideoView.getCurrentPosition() * 1000, mediaMetadataRetriever);
        preview = BitmapUtil.getScaleBitmap(preview, (int) (preview.getWidth() * 0.1), (int) (preview.getHeight() * 0.1));
        imageViewPreview.setImageBitmap(preview);
    }

    private void showPreview() {
        imageViewPreview.setVisibility(View.VISIBLE);
    }

    private void hidePreview() {
        imageViewPreview.setVisibility(View.GONE);
    }

    private void initFile(File file) {

        if (file == null) {
            return;
        }

        clearImageViews();
        clearBitMapCoverList();
        clearBitmapPreview();

        mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
        String duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        ILog.iLogDebug(TAG, duration);

        long durationMs = Long.valueOf(duration);
        double durations = Math.floor(durationMs * 0.001);

        seekBar.setMax((int) durations);

        ILog.iLogDebug(TAG, durations);

        List<Float> previewList = new ArrayList<>();
        List<Float> coverList = new ArrayList<>();


        for (int i = 1; i <= MAX_SEEK_BAR_PREVIEW_NUMBER; i++) {
            previewList.add((float) (durations / MAX_SEEK_BAR_PREVIEW_NUMBER * i));
        }

        for (int i = 1; i <= MAX_SELECT_COVER_NUMBER; i++) {
            coverList.add((float) (durations / MAX_SELECT_COVER_NUMBER * i));
        }

        showProgressBar();

        ThreadUtil.startThread(new Runnable() {
            @Override
            public void run() {

                tempBitmapList = new ArrayList<>();

                for (int i = 0; i < previewList.size(); i++) {
                    ILog.iLogDebug(TAG, previewList.get(i));
                    Bitmap bitmap = BitmapUtil.extractFrame(previewList.get(i) * 1000, mediaMetadataRetriever);
                    bitmap = BitmapUtil.getScaleBitmap(bitmap, (int) (bitmap.getWidth() * 0.1), (int) (bitmap.getHeight() * 0.1));
                    tempBitmapList.add(bitmap);

                    result = BitmapUtil.concatBitmaps(0, tempBitmapList);
                    ILog.iLogDebug(TAG, result.getWidth() + " " + result.getHeight());
                }

                ThreadUtil.startUIThread(0, new Runnable() {
                    @Override
                    public void run() {
                        hideProgressBar();
                        imageView.setImageBitmap(result);

                        shVideoView.setVideoPath(file.getAbsolutePath());

                        initPreview();
                        showPreview();

                        ThreadUtil.startThread(new Runnable() {
                            @Override
                            public void run() {

                                int total = tempBitmapList.size();
                                for (int i = total - 1; i >= 0; i--) {
                                    Bitmap b = tempBitmapList.get(i);
                                    if (!b.isRecycled()) {
                                        b.recycle();
                                    }
                                }

                                tempBitmapList.clear();
                                tempBitmapList = null;
                            }
                        });
                    }
                });
            }
        });

        ThreadUtil.startThread(new Runnable() {
            @Override
            public void run() {

                coverBitmapList = new ArrayList<>();

                for (int i = 0; i < coverList.size(); i++) {
                    ILog.iLogDebug(TAG, coverList.get(i));
                    Bitmap bitmap = BitmapUtil.extractFrame(coverList.get(i) * 1000, mediaMetadataRetriever);
                    bitmap = BitmapUtil.getScaleBitmap(bitmap, (int) (bitmap.getWidth() * 0.1), (int) (bitmap.getHeight() * 0.1));
                    coverBitmapList.add(bitmap);
                }

                ThreadUtil.startUIThread(0, new Runnable() {
                    @Override
                    public void run() {

                        imageViewCoverOne.setImageBitmap(coverBitmapList.get(0));
                        imageViewCoverTwo.setImageBitmap(coverBitmapList.get(1));
                        imageViewCoverThree.setImageBitmap(coverBitmapList.get(2));

                        resetSeekBar();
                    }
                });
            }
        });
    }

    private void clearImageViews() {
        imageView.setImageBitmap(null);
        imageViewPreview.setImageBitmap(null);
        imageViewCoverOne.setImageBitmap(null);
        imageViewCoverTwo.setImageBitmap(null);
        imageViewCoverThree.setImageBitmap(null);
        imageViewPlay.setVisibility(View.VISIBLE);
    }

    private void selectFile() {
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 102);
                return;
            }
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == 101) {
            Uri uri = data.getData();
            String realPath = FilesUtil.getRealFilePath(this, uri);
            if (realPath != null) {
                ILog.iLogDebug(TAG, realPath);

                File file = new File(realPath);
                ILog.iLogDebug(TAG, file.exists() + " " + file.isFile() + " " + file.getAbsolutePath());

                try {
                    ILog.iLogDebug(TAG, FilesUtil.formatFileSize(FilesUtil.getFileSize(file)));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                initFile(file);
            }
        }
    }



    private void showProgressBar() {
        frameLayoutProgress.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        frameLayoutProgress.setVisibility(View.GONE);
    }

    private void clearBitMapCoverList() {
        if(coverBitmapList != null) {
            int total = coverBitmapList.size();
            for (int i = total - 1; i >= 0; i--) {
                Bitmap b = coverBitmapList.get(i);
                if (b != null && !b.isRecycled()) {
                    b.recycle();
                }
            }

            coverBitmapList.clear();
            coverBitmapList = null;
        }
    }

    private void clearBitmapPreview() {
        if(result != null && !result.isRecycled()) {
            result.recycle();
        }

        if(preview != null && !preview.isRecycled()) {
            preview.recycle();
        }
    }

    @Override
    protected void onDestroy() {

        if (mediaMetadataRetriever != null) {
            mediaMetadataRetriever.release();
            mediaMetadataRetriever = null;
        }

        clearBitMapCoverList();
        clearBitmapPreview();

        if(shVideoView != null) {
            shVideoView.release();
            shVideoView = null;
        }

        super.onDestroy();
    }
}
