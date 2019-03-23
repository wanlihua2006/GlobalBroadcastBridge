package com.qiku.broadcasts;

import android.content.Context;

/**
 * 暂不支持该策略
 */
public class MergePolicyNone extends MergePolicyBase {

    public MergePolicyNone(Context context, String name) {
        super(context, name);
    }

    @Override
    public String getActionCategory(BroadcastBridge.Listener listener) {
        return null;
    }

    @Override
    public boolean isSchemeSupported() {
        return true;
    }

    @Override
    public boolean isPermissionSupported() {
        return true;
    }
}
