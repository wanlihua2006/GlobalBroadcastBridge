package com.qiku.broadcasts;

import android.content.Context;

/**
 * 将所有Listener合并到一个全局的category中，使用一个全局的Receiver监听所有action
 *
 * 该策略并不能支持一些特殊需求，比如需要指定scheme和permission的情况
 *
 * 如果该策略不能满足需求，建议选用其他策略，或者App自己实现自定义策略
 */
public class MergePolicyGlobal extends MergePolicyBase {

    MergePolicyGlobal(Context context, String name) {
        super(context, name);
    }

    /**
     * 所有Listener公用一个category, 所有Listener中的所有action合并到一个全局的receiver中监听
     * @param listener
     * @return
     */
    @Override
    public String getActionCategory(BroadcastBridge.Listener listener) {
        return "global";
    }

    /**
     * 如果Listener指定了scheme,那么当前Policy无法支持该Listener
     * 因为在注册广播的时候指定permission,会导致合并的其他Listener无法接收广播
     * @return
     */
    @Override
    public boolean isSchemeSupported() {
        return false;
    }

    /**
     * 如果Listener指定了permission,那么当前Policy无法支持该Listener
     * 因为在注册广播的时候指定permission,会导致合并的其他Listener无法接收广播
     * @return
     */
    @Override
    public boolean isPermissionSupported() {
        return false;
    }
}
