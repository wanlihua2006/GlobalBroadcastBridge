package com.qiku.broadcasts;

public interface IMergePolicy {

    /**
     * 获取当前MergePolicy的名字
     *
     * @return
     */
    String getMergePolicyName();

    /**
     * 注册Listener
     *
     * @param listener
     * @return
     */
    boolean registerListener(BroadcastBridge.Listener listener);

    /**
     * 反注册Listener
     *
     * @param listener
     * @return
     */
    boolean unregisterListener(BroadcastBridge.Listener listener);

    /**
     * 当前MergePolicy是否支持在Listener中指定scheme
     *
     * @return
     */
    boolean isSchemeSupported();

    /**
     * 当前MergePolicy是否支持在Listener中指定permission
     * @return
     */
    boolean isPermissionSupported();

    /**
     * 为每个Listener指定分类，相同分类的Listener会合并到同一个Receiver中
     *
     * @param listener
     * @return
     */
    String getActionCategory(BroadcastBridge.Listener listener);
}
