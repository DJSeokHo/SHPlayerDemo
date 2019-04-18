package com.swein.shplayerdemo.framework.util.debug.log;


import com.swein.shplayerdemo.BuildConfig;
import com.swein.shplayerdemo.framework.util.debug.log.factroy.basiclog.BasicLog;
import com.swein.shplayerdemo.framework.util.debug.log.logclass.DebugILog;
import com.swein.shplayerdemo.framework.util.debug.log.logclass.ErrorILog;
import com.swein.shplayerdemo.framework.util.debug.log.logclass.InfoILog;
import com.swein.shplayerdemo.framework.util.debug.log.logclass.WarnILog;

/**
 *
 * Created by seokho on 13/12/2016.
 */

public class ILog {

    private static String HEAD = "[- ILog Print -] ";
    private static String TAG  = " ||===>> ";

    public static void iLogDebug( String tag, Object content ) {
        if( BuildConfig.DEBUG) {
            BasicLog basicLog    = new DebugILog().getBasicLog();
            basicLog.iLog( HEAD + TAG + tag, content );
        }
    }

    public static void iLogInfo(String tag, Object content) {
        if( BuildConfig.DEBUG) {
            BasicLog    basicLog    = new InfoILog().getBasicLog();
            basicLog.iLog( HEAD + TAG + tag, content );
        }
    }

    public static void iLogError(String tag, Object content) {
        if( BuildConfig.DEBUG) {
            BasicLog    basicLog    = new ErrorILog().getBasicLog();
            basicLog.iLog( HEAD + TAG + tag, content );
        }
    }

    public static void iLogWarn(String tag, Object content) {
        if( BuildConfig.DEBUG) {
            BasicLog    basicLog    = new WarnILog().getBasicLog();
            basicLog.iLog( HEAD + TAG + tag, content );
        }
    }
}
