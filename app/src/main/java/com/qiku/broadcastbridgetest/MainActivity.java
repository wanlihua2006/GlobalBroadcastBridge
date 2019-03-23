package com.qiku.broadcastbridgetest;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.qiku.broadcasts.BroadcastBridge;
import com.qiku.broadcasts.R;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    BroadcastBridge.Listener mScreenListener =
            new BroadcastBridge.Listener("screen-listener",
                    Intent.ACTION_SCREEN_OFF, Intent.ACTION_SCREEN_ON) {
        @Override
        public void onReceiveBroadcast(Intent intent) {
            Log.i(Constants.TAG, this + " received " + intent);
        }
    };

    BroadcastBridge.Listener mScreenListener1 =
            new BroadcastBridge.Listener("screen-listener1",
                    Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT) {
                @Override
                public void onReceiveBroadcast(Intent intent) {
                    Log.i(Constants.TAG, this + " received " + intent);
                }
            };

    BroadcastBridge.Listener mScreenListener2 =
            new BroadcastBridge.Listener("screen-listener2",
                    Intent.ACTION_PACKAGE_ADDED, Intent.ACTION_PACKAGE_CHANGED, Intent.ACTION_PACKAGE_REMOVED) {
                @Override
                public void onReceiveBroadcast(Intent intent) {
                    Log.i(Constants.TAG, this + " received " + intent);
                }

                @Override
                public List<String> getDataSchemes() {
                    return Arrays.asList("package");
                }
            };

    BroadcastBridge.Listener mScreenListener3 =
            new BroadcastBridge.Listener("screen-listener3",
                    ConnectivityManager.CONNECTIVITY_ACTION) {
                @Override
                public void onReceiveBroadcast(Intent intent) {
                    Log.i(Constants.TAG, this + " received " + intent);
                }
            };

    //自定义广播
    BroadcastBridge.Listener mScreenListener4 =
            new BroadcastBridge.Listener("screen-listener4",
                    "com.qiku.ACTION_BROADCAST_BRIDGE_TEST") {
                @Override
                public void onReceiveBroadcast(Intent intent) {
                    Log.i(Constants.TAG, this + " received " + intent);
                }
            };

    //自定义广播
    BroadcastBridge.Listener mScreenListener5 =
            new BroadcastBridge.Listener("screen-listener5",
                    "com.qiku.ACTION_BROADCAST_BRIDGE_TEST1") {
                @Override
                public void onReceiveBroadcast(Intent intent) {
                    Log.i(Constants.TAG, this + " received " + intent);
                }

                @Override
                public String getBroadcastPermission() {
                    return "com.qiku.permission.BROADCAST_BRIDGE";
                }
            };

    //测试优先级
    BroadcastBridge.Listener mScreenListener6 =
            new BroadcastBridge.Listener("screen-listener6", 100,
                    Intent.ACTION_SCREEN_OFF) {
                @Override
                public void onReceiveBroadcast(Intent intent) {
                    Log.i(Constants.TAG, this + " received " + intent);
                }
            };


    //自定义广播，即需要scheme,又需要权限
    BroadcastBridge.Listener mScreenListener7 =
            new BroadcastBridge.Listener("screen-listener5",
                    "com.qiku.ACTION_BROADCAST_BRIDGE_TEST2") {
                @Override
                public void onReceiveBroadcast(Intent intent) {
                    Log.i(Constants.TAG, this + " received " + intent);
                }

                @Override
                public List<String> getDataSchemes() {
                    return Arrays.asList("content");
                }

                @Override
                public String getBroadcastPermission() {
                    return "com.qiku.permission.BROADCAST_BRIDGE";
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.send_custom_broadcast).setOnClickListener( v -> {
            Intent intent = new Intent("com.qiku.ACTION_BROADCAST_BRIDGE_TEST");
            sendBroadcast(intent);
        });

        findViewById(R.id.send_custom_broadcast1).setOnClickListener( v -> {
            Intent intent = new Intent("com.qiku.ACTION_BROADCAST_BRIDGE_TEST1");
            sendBroadcast(intent);
        });

        findViewById(R.id.send_custom_broadcast2).setOnClickListener( v -> {
            Intent intent = new Intent("com.qiku.ACTION_BROADCAST_BRIDGE_TEST2");
            intent.setData(Uri.parse("content://a.b.c/d"));
            sendBroadcast(intent);
        });

        BroadcastBridge.getInstance().registerBroadcastListener(mScreenListener);
        sleep(100);
        BroadcastBridge.getInstance().registerBroadcastListener(mScreenListener1);
        sleep(100);
        BroadcastBridge.getInstance().registerBroadcastListener(mScreenListener2);
        sleep(100);
        BroadcastBridge.getInstance().registerBroadcastListener(mScreenListener3);
        sleep(100);
        BroadcastBridge.getInstance().registerBroadcastListener(mScreenListener4);
        sleep(100);
        BroadcastBridge.getInstance().registerBroadcastListener(mScreenListener5);
        sleep(100);
        BroadcastBridge.getInstance().registerBroadcastListener(mScreenListener6);
        sleep(100);
        BroadcastBridge.getInstance().registerBroadcastListener(mScreenListener7);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BroadcastBridge.getInstance().unregisterBroadcastListener(mScreenListener);
        sleep(100);
        BroadcastBridge.getInstance().unregisterBroadcastListener(mScreenListener1);
        sleep(100);
        BroadcastBridge.getInstance().unregisterBroadcastListener(mScreenListener2);
        sleep(100);
        BroadcastBridge.getInstance().unregisterBroadcastListener(mScreenListener3);
        sleep(100);
        BroadcastBridge.getInstance().unregisterBroadcastListener(mScreenListener4);
        sleep(100);
        BroadcastBridge.getInstance().unregisterBroadcastListener(mScreenListener5);
        sleep(100);
        BroadcastBridge.getInstance().unregisterBroadcastListener(mScreenListener6);
        sleep(100);
        BroadcastBridge.getInstance().unregisterBroadcastListener(mScreenListener7);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
