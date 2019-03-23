package com.qiku.broadcasts;

import android.util.Log;

final class BroadcastBridgeLog {
    private BroadcastBridgeLog() {}


    private static final String TAG = "BroadcastBridge";

    public static void v(String subTag, String msg) {
        if (!isLogOn()) {
            return;
        }
        Log.v(TAG, "[" + subTag + "] => " + msg);
    }

    public static void w(String subTag, String msg) {
        if (!isLogOn()) {
            return;
        }
        Log.w(TAG, "[" + subTag + "] => " + msg);
    }

    public static void d(String subTag, String msg) {
        if (!isLogOn()) {
            return;
        }
        Log.d(TAG, "[" + subTag + "] => " + msg);
    }

    public static void i(String subTag, String msg) {
        if (!isLogOn()) {
            return;
        }
        Log.i(TAG, "[" + subTag + "] => " + msg);
    }

    public static void e(String subTag, String msg) {
        if (!isLogOn()) {
            return;
        }
        Log.e(TAG, "[" + subTag + "] => " + msg);
    }

    private static boolean isLogOn() {
        //TODO : 可改为用属性动态控制log开关
        return true;
    }

}
