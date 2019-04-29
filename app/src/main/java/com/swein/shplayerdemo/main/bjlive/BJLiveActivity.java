package com.swein.shplayerdemo.main.bjlive;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.swein.shplayerdemo.R;
import com.swein.shplayerdemo.framework.util.debug.log.ILog;

public class BJLiveActivity extends Activity {

    private final static String TAG = "BJLiveActivity";

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

    }

    private void findViews() {

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
        super.onDestroy();
    }
}
