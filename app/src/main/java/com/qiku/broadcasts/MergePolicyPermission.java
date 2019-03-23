package com.qiku.broadcasts;

import android.content.Context;
import android.text.TextUtils;

/**
 * 如果App中注册的广播需要指定permission,可以使用该Policy
 *
 * 该MergePolicy会将相同permission的Listener合并到一个category中
 * 没有指定scheme的Listener合并到一个叫做permission:non的category中
 *
 * 如果该类不能满足App的要求，建议App参考该类实现自己的Policy
 */
public class MergePolicyPermission extends MergePolicyBase {
    public MergePolicyPermission(Context context, String name) {
        super(context, name);
    }

    @Override
    public boolean isSchemeSupported() {
        return false;
    }

    /**
     * 因为该Policy就是根据permission为Listener进行分类的，所以必须支持permission
     * @return
     */
    @Override
    public boolean isPermissionSupported() {
        return true;
    }

    @Override
    public String getActionCategory(BroadcastBridge.Listener listener) {

        String permission = listener.getBroadcastPermission();
        if (TextUtils.isEmpty(permission)) {
            return "permission:non";
        } else {
            return "permission:" + permission;
        }
    }
}
