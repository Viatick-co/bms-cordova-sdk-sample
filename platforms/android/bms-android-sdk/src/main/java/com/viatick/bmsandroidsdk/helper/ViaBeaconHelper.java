package com.viatick.bmsandroidsdk.helper;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.webkit.WebView;

import com.viatick.bmsandroidsdk.model.ViaBmsUtil;
import com.viatick.bmsandroidsdk.model.ViaMinisite;

import java.util.ArrayList;
import java.util.List;

public class ViaBeaconHelper {
    public static void readMofidyAndExecuteJs(WebView webView, Context context) {

//        String ret = ViaValues.MAIN_JS_CODE;
//
//        /*
//         * Get the device info again if user access by notification
//         * after they have closed the app
//         */
//        if (HTTPRequestHelper.xDeviceId == null) {
//            HTTPRequestHelper.xDeviceId = ViaBeaconHelper.getDeviceId(context);
//            HTTPRequestHelper.model = ViaBeaconHelper.getDeviceName().replaceAll("-", "");
//            HTTPRequestHelper.system = ViaConstants.OS_NAME;
//            HTTPRequestHelper.version = System.getProperty("os.version").replaceAll("-","");
//            HTTPRequestHelper.xSystem = HTTPRequestHelper.model + "-" + HTTPRequestHelper.system +
//                    "-" + HTTPRequestHelper.version;
//        }
//
//        /*
//         * Modify the String
//         */
//        ret = ret.replaceAll(ViaConstants.ANDROID_DEVICE_ID, HTTPRequestHelper.xDeviceId);
//        ret = ret.replaceAll(ViaConstants.ANDROID_MODEL, HTTPRequestHelper.model);
//        ret = ret.replaceAll(ViaConstants.ANDROID_SYSTEM, HTTPRequestHelper.system);
//        ret = ret.replaceAll(ViaConstants.ANDROID_VERSION, HTTPRequestHelper.version);

        /*
         * Execute the modified JS
         */
//        webView.loadUrl("javascript:" + ret);
    }

    public static void sendMinisiteMenuUpdate (LocalBroadcastManager broadcaster,
                                               ArrayList<ViaMinisite> viaMinisites) {
        Intent minisiteMenuIntent = new Intent(ViaBmsUtil.ViaConstants.MINISITE_MENU_UPDATE_RESULT);
        if(viaMinisites != null)
            minisiteMenuIntent.putParcelableArrayListExtra(ViaBmsUtil.ViaConstants.MINISITE_MENU_UPDATE_CONTENT,
                    viaMinisites);
        broadcaster.sendBroadcast(minisiteMenuIntent);
    }

    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }
}
