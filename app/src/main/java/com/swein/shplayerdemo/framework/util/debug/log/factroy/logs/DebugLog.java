package com.swein.shplayerdemo.framework.util.debug.log.factroy.logs;


import android.util.Log;

import com.swein.shplayerdemo.framework.util.debug.log.factroy.basiclog.BasicLog;


/**
 * Created by seokho on 19/04/2017.
 */

public class DebugLog implements BasicLog {

    @Override
    public void iLog(String tag, Object content) {
        Log.d( tag, String.valueOf( content ) );
    }
}
