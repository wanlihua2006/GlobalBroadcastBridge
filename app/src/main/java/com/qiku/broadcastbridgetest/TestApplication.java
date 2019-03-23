package com.qiku.broadcastbridgetest;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.qiku.broadcasts.BroadcastBridge;
import com.qiku.broadcasts.MergePolicyBase;

import java.util.Collections;
import java.util.List;

public class TestApplication extends Application {

    private BroadcastBridge mBridge;

    @Override
    public void onCreate() {
        super.onCreate();

        mBridge = BroadcastBridge.getInstance();

        //测试MERGE_GLOBAL
        //mBridge.init(this, BroadcastBridge.MERGE_GLOBAL);

        //测试MERGE_AUTO
        //mBridge.init(this, BroadcastBridge.MERGE_AUTO);

        //测试MERGE_SCHEME
        //mBridge.init(this, BroadcastBridge.MERGE_SCHEME);

        //测试MERGE_PERMISSION
        //mBridge.init(this, BroadcastBridge.MERGE_PERMISSION);

        //测试自定义Policy
        mBridge.init(this, new MergePolicyCustom(this,"MergeCustom"));

    }


    /**
     * 应用在使用BroadcastBridge模块时， 建议优先选择BroadcastBridge模块中预置的策略，
     * 但是BroadcastBridge模块中预置的策略考虑到合并广播会有冲突，可能不能完全满足App的需求。
     * 比如 : MergePolicyGlobal不支持scheme和permission,
     *       MergePolicyPermission支持permission但不支持scheme
     *       MergePolicyScheme支持scheme但不支持permission
     *
     * 比如我们在做App开发的时候，有一些广播需要scheme,另一些广播需要permission。
     * 现在自定义一个策略，即支持scheme,又支持permission，并且支持同时有permission和scheme的情况。
     * 只需要在isSchemeSupported方法中指明支持scheme,在isPermissionSupported中指明支持permission,
     * 并在getActionCategory方法中，为不同的Listener返回不同的category即可
     */
    private class MergePolicyCustom extends MergePolicyBase {

        public MergePolicyCustom(Context context, String name) {
            super(context, name);
        }

        @Override
        public boolean isSchemeSupported() {
            return true;
        }

        @Override
        public boolean isPermissionSupported() {
            return true;
        }

        @Override
        public String getActionCategory(BroadcastBridge.Listener listener) {
            List<String> schemes = listener.getDataSchemes();
            String permission = listener.getBroadcastPermission();

            StringBuilder category = new StringBuilder("custom");
            if (schemes != null && schemes.size() > 0) {
                Collections.sort(schemes);
                category.append(":");
                for (String scheme : schemes) {
                    category.append(scheme).append("_");
                }
                category.deleteCharAt(category.length() - 1);
            }

            if (!TextUtils.isEmpty(permission)) {
                category.append("_").append(permission);
            }

            return category.toString();
        }
    }
}
