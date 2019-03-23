package com.qiku.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.List;

/**
 * 各个MergePolicy的基类，所有Policy都是从该类继承。
 * Broadcast中提供几种预置的Policy，如果不满足App的需求，App可以继承该类实现自定义的Policy。
 */
public abstract class MergePolicyBase implements IMergePolicy {

    /**
     * 每个CategoryReceiver会监听某个category中的所有的action
     * 至于每个action划分到哪个category中，则由具体的MergePolicy决定
     */
    public class CategoryReceiver extends BroadcastReceiver {

        private IntentFilter mFilter = new IntentFilter();

        private String mCategory;

        private String mPermission;

        private ArrayList<BroadcastBridge.Listener> mListeners = new ArrayList<>();

        CategoryReceiver(String category) {
            mCategory = category;
        }

        /**
         * 添加新的Listener到当前Receiver中
         * @param listener 要添加的Listener
         * @return 是否成功添加了Listener
         */
        boolean addListener(BroadcastBridge.Listener listener) {

            BroadcastBridgeLog.i(mCategory, "*** add listener start : " + listener + " <<<");

            // listener不能注册多次,否则会进行多余的回调
            if (mListeners.contains(listener)) {
                BroadcastBridgeLog.e(mCategory, "addListener : " + listener +
                        " can not register twice");
                return false;
            }

            //判断当前MergePolicy是否支持scheme
            List<String> schemes = listener.getDataSchemes();
            if (schemes != null && schemes.size() > 0) {
                if (isSchemeSupported()) {
                    for (String scheme : schemes) {
                        if (!mFilter.hasDataScheme(scheme)) {
                            mFilter.addDataScheme(scheme);
                        }
                    }
                } else {
                    String msgErr = "current merge policy " + getMergePolicyName() +
                            " do not support scheme";

                    // TODO : 为了调试方便，只打印日志并返回false，在正式代码中最好抛出异常
                    BroadcastBridgeLog.e(mCategory,  msgErr + ", " + listener + " not added !!!");
                    return false;

                    /*throw new IllegalArgumentException(msgErr + " for listener " + listener
                            + ", please select other merge policy or implement by yourself !" );*/
                }
            }

            //判断当前MergePolicy是否支持permission
            String permission = listener.getBroadcastPermission();
            if (!TextUtils.isEmpty(permission)) {
                if (isPermissionSupported()) {
                    mPermission = permission;
                } else {
                    // TODO : 为了调试方便，只打印日志并返回false，在正式代码中最好抛出异常
                    String msgErr = "current merge policy " + getMergePolicyName() +
                            " do not support permission";
                    BroadcastBridgeLog.e(mCategory,  msgErr + ", " + listener + " not added !!!");
                    return false;

                    /*throw new IllegalArgumentException(msgErr + " for listener " + listener
                            + ", please select other merge policy or implement by yourself !" );*/
                }
            }

            mListeners.add(listener);

            boolean registerAgain = false;

            // 监听的action已经变化，需要重新注册
            for (String action : listener.getActionList()) {
                if (!mFilter.hasAction(action)) {
                    BroadcastBridgeLog.i(mCategory, "addListener : " +
                            "receiver will add new action " + action);
                    mFilter.addAction(action);
                    registerAgain = true;
                }
            }

            // 广播优先级已经改变，需要重新注册
            // Receiver的广播优先级是所有Listener中指定的优先级的最大值
            if (mFilter.getPriority() < listener.getPriority()) {
                BroadcastBridgeLog.i(mCategory, "addListener : receiver " +
                        "priority changed from " + mFilter.getPriority() +
                        " to " + listener.getPriority());
                mFilter.setPriority(listener.getPriority());
                registerAgain = true;
            }

            if (registerAgain) {
                BroadcastBridgeLog.i(mCategory, "addListener : receiver " +
                        "should be registered again");
                registerAgain();
            }
            BroadcastBridgeLog.i(mCategory, "*** add listener end  : " + listener + " <<<");
           return true;
        }

        /**
         * 从当前Receiver中删除一个Listener
         * 如果删除Listener会影响当前Receiver监听的action和广播优先级，则会重新注册
         *
         * @param listener 要删除的Listener
         * @return 是否由于删除Listener使当前Receiver不再使用
         */
        boolean removeListener(BroadcastBridge.Listener listener) {

            BroadcastBridgeLog.i(mCategory, "*** remove listener start : " + listener + " <<<");

            boolean removed = mListeners.remove(listener);
            if (!removed) {
                BroadcastBridgeLog.w(mCategory, "removeListener : " + listener +
                        " has not registered, no need to remove");
                return false;
            } else {
                BroadcastBridgeLog.i(mCategory, "removeListener : " + listener + " removed");
            }

            if (mListeners.size() == 0) {
                BroadcastBridgeLog.w(mCategory, "removeListener : all listeners removed");
                return true;
            }

            // 判断是否重新注册
            boolean shouldRegisterAgain = false;

            // 如果其他listener没有监听被删除的listener的action，则将action从IntentFilter中删除,
            // 然后重新注册receiver,这样可以避免系统投递不必要的广播
            for (String actionOfRemovedListener : listener.getActionList()) {
                boolean shouldRemoveFromIntentFilter = true;
                for (BroadcastBridge.Listener remainListener : mListeners) {
                    if (remainListener.getActionList().contains(actionOfRemovedListener)) {
                        shouldRemoveFromIntentFilter = false;
                        break;
                    }
                }
                if (shouldRemoveFromIntentFilter) {
                    BroadcastBridgeUtil.removeAction(mFilter, actionOfRemovedListener);
                    shouldRegisterAgain = true;
                    BroadcastBridgeLog.w(mCategory, "removeListener : remove action " +
                            actionOfRemovedListener);
                }
            }

            //判断是否需要改变优先级
            //如果移除的listener的优先级和IntentFilter中相同，那么从所有剩余listener中找到最大的优先级,
            //如果这个优先级小于当前的IntentFilter的有限级，那么IntentFilter要降低优先级
            int maxRemainPriority = IntentFilter.SYSTEM_LOW_PRIORITY;
            if (mFilter.getPriority() == listener.getPriority()) {
                for (BroadcastBridge.Listener l : mListeners) {
                    if (l.getPriority() > maxRemainPriority) {
                        maxRemainPriority = l.getPriority();
                    }
                }
            }

            if (maxRemainPriority < mFilter.getPriority()) {
                BroadcastBridgeLog.i(mCategory, "removeListener : receiver " +
                        "priority changed from " + mFilter.getPriority() +
                        " to " + maxRemainPriority);
                mFilter.setPriority(maxRemainPriority);
                shouldRegisterAgain = true;
            }

            if (shouldRegisterAgain) {
                BroadcastBridgeLog.i(mCategory, "removeListener : receiver " +
                        "should be registered again");
                registerAgain();
            }

            BroadcastBridgeLog.i(mCategory, "*** remove listener end  : " + listener + " <<<");

            return false;
        }

        private void registerSelf() {
            try {
                if (TextUtils.isEmpty(mPermission)) {
                    mApplicationContext.registerReceiver(this, mFilter);
                } else {
                    mApplicationContext.registerReceiver(this, mFilter, mPermission, null);
                }
            } catch (Exception e) {
            }
            BroadcastBridgeLog.i(mCategory, "registerSelf : current actions : " +
                    BroadcastBridgeUtil.actionsOf(mFilter));
        }

        private void unregisterSelf() {
            try {
                mApplicationContext.unregisterReceiver(this);
            } catch (Exception e) {
            }
        }

        private void registerAgain() {
            unregisterSelf();
            registerSelf();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            for (BroadcastBridge.Listener listener : mListeners) {
                if (listener.getActionList().contains(intent.getAction())) {
                    listener.onReceiveBroadcast(intent);
                }
            }
        }
    }


    protected Context mApplicationContext;
    protected String mName;

    private ArrayMap<String, CategoryReceiver> mCategoryToReceivers =
            new ArrayMap<>();

    public MergePolicyBase(Context context, String name) {
        mApplicationContext = context;
        mName = name;
    }

    @Override
    public String getMergePolicyName() {
        return mName;
    }

    @Override
    public boolean registerListener(BroadcastBridge.Listener listener) {

        if (listener.getActionList() == null || listener.getActionList().size() == 0) {
            throw new IllegalArgumentException("Listener " + listener + " has no actions");
        }

        String category = getActionCategory(listener);
        if (!mCategoryToReceivers.containsKey(category)) {
            //需要新注册一个receiver
            CategoryReceiver receiver = new CategoryReceiver(category);
            receiver.addListener(listener); // add listener will register the receiver
            mCategoryToReceivers.put(category, receiver);
            return true;
        }

        CategoryReceiver receiver = mCategoryToReceivers.get(category);
        return receiver.addListener(listener);
    }

    @Override
    public boolean unregisterListener(BroadcastBridge.Listener listener) {
        String category = getActionCategory(listener);
        if (!mCategoryToReceivers.containsKey(category)) {
            return false;
        }

        CategoryReceiver receiver = mCategoryToReceivers.get(category);
        if (receiver.removeListener(listener)) {
            //由于在当前CategoryReceiver中删除了一个Listener,导致当前CategoryReceiver不再使用
            //这种情况通常发生在删除的Listener是CategoryReceiver中最后一个Listener
            //这时要unregister当前CategoryReceiver,并从mCategoryToReceivers中删除
            receiver.unregisterSelf();
            mCategoryToReceivers.remove(category);
        }
        return true;
    }
}
