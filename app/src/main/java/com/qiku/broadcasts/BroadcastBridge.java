package com.qiku.broadcasts;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * BroadcastBridge是一个管理动态广播的通用模块，目的是合并App中的广播，防止在App中过度注册重复广播。
 * 目前支持两种合并策略: MERGE_GLOBAL和MERGE_AUTO，默认是MERGE_AUTO。
 *
 * 合并后的广播优先级，是该Receiver对应的所有Listener中指定的优先级最高值。
 *
 * BroadcastBridge中注册receiver，使用的都是App的ApplicationContext。
 *
 * @author zhangjigang(zhangjigang-os@360os.com)
 * @version v1.0
 */
public class BroadcastBridge {

    private static final String TAG = "BroadcastBridge";

    private static final BroadcastBridge sInstance = new BroadcastBridge();

    public static BroadcastBridge getInstance() {
        return sInstance;
    }

    /**
     * 用于监听某个或某些action的Listener，由App业务层实现，并注册到BroadcastBridge中。
     * 每个Listener回调的顺序和注册的先后顺序一致。
     */
    public static abstract class Listener {

        private static final int PRIORITY_DEFAULT = 0;

        private String mName;
        private int mPriority;
        private List<String> mActionList;

        /**
         * 创建Listener对象
         *
         * @param name 在同一个进程中，每个Listener都要有一个唯一的名字
         * @param priority 广播优先级
         * @param actions 该listener关注的action
         */
        public Listener(String name, int priority, String ... actions) {
            if (TextUtils.isEmpty(name)) {
                throw new IllegalArgumentException("Listener must has a name");
            }

            if (actions == null || actions.length == 0) {
                throw new IllegalArgumentException("Listener must has actions");
            }
            mName = name;
            mPriority = priority;
            mActionList = Arrays.asList(actions);
        }

        /**
         * 创建Listener对象
         *
         * @param name 在同一个进程中，每个Listener都要有一个唯一的名字
         * @param actions 该listener关注的action
         */
        public Listener(String name, String ... actions) {
            this(name, PRIORITY_DEFAULT, actions);
        }

        public String getName() {
            return mName;
        }

        public int getPriority() {
            return mPriority;
        }

        public List<String> getActionList() {
            return mActionList;
        }

        /**
         * 像PACKAGE_ADD,PACKAGE_REMOVED,PACKAGE_REPLACED等广播
         * 必须要加package scheme
         * 如果一个Listener指定它需要scheme等uri信息，那么这种类型的action无法和其他类型的action合并
         *
         * 如果需要添加scheme信息，App实现Listener的时候，覆写该方法，返回需要添加的scheme
         * @return
         */
        public List<String> getDataSchemes() {
            return null;
        }

        public String getBroadcastPermission() {
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof Listener)) return false;
            Listener that = (Listener) o;
            return Objects.equals(mName, that.mName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mName);
        }

        @Override
        public String toString() {
            return "BroadcastListener " + mName;
        }

        /**
         * 该方法在主线程中回调，APP可以根据自己的业务逻辑，将回调的intent放到子线程中处理,
         * 切勿在主线程中执行耗时操作
         * @param intent
         */
        public abstract void onReceiveBroadcast(Intent intent);
    }

    /**
     * 将所有的action合并到一个全局的receiver中，相当于使用一个receiver监听所有的广播
     *
     * 注意:
     * 如果一个进程中监听的广播不是同一个类别的，该策略不能很好的工作。
     * 比如，SCREEN_ON和PACKAGE_ADD合并到一起来监听，但是监听PACKAGE_ADD需要在IntentFilter中添加
     * package scheme，但是添加了package scheme后，就无法接收SCREEN_ON广播
     * 所以，除非应用中所有的广播都不需要scheme, type等uri信息，才考虑使用该策略，否则不要使用该策略
     */
    public static final String MERGE_GLOBAL = "MergeGlobal";

    /**
     * 自动将同类别的action合并到一个receiver中(比如SCREEN_ON, SCREEN_OFF和USER_PRESENT合并到keyguard
     * category中；PACKAGE_ADDED,PACKAGE_REPLACED和PACKAGE_REMOVED合并到package category中)。
     * 每个category对应一个receiver对象。
     *
     * 在MergePolicyAuto中预定义了这些category,每个category对应一个或多个action, 如果业务层传入的action
     * 不在预定义的category中，那个这个action作为一个独立的category
     */
    public static final String MERGE_AUTO = "MergeAuto";


    /**
     * 按Listener指定的scheme来为Listener分类，相同scheme的Listener分到同一个category中,
     * 使用一个Receiver来监听。
     *
     * 该策略是为了解决不同scheme的IntentFilter无法合并的问题。比如PACKAGE_XXX广播必须指定scheme
     * 为package,如果把这个广播和其他不需要scheme的广播合并到一个IntentFilter中，会导致其他广播
     * 无法接收
     */
    public static final String MERGE_SCHEME = "MergeScheme";

    /**
     * 按Listener指定的permission来为Listener分类，相同permission的Listener分到同一个category中,
     * 使用一个Receiver来监听。
     *
     * 该策略是为了解决不同permission的IntentFilter无法合并的问题。如果一个广播必须限定发送者必须有一个特定的权限，
     * 把这个广播和其他不需要permission的广播合并到一个IntentFilter中，会导致其他广播无法接收
     */
    public static final String MERGE_PERMISSION = "MergePermission";

    /**
     * 不进行合并，每个Listener对应一个receiver
     * 这种方式可能不能很好的达到合并重复广播的效果,不推荐使用
     */
    //public static final String MERGE_NONE = "MergeNone";


    private Context mApplicationContext;

    private String mMergePolicyName;

    private IMergePolicy mPolicy;

    private boolean mInitialized;


    /**
     * 初始化BroadcastBridge,使用默认的合并策略，默认为MERGE_AUTO
     *
     * @param context
     */
    public void init(Context context) {
        init(context, MERGE_AUTO /* default category */);
    }

    /**
     * 初始化BroadcastBridge
     *
     * @param context
     * @param mergePolicy 需要使用的合并策略(MERGE_GLOBAL | MERGE_AUTO | MERGE_SCHEME | MERGE_PERMISSION)
     */
    public void init(Context context, String mergePolicy) {
        if (mInitialized) {
            BroadcastBridgeLog.w(TAG, "No need to init twice");
            return;
        }

        if (context == null) {
            throw new IllegalStateException("context is null !!!");
        }
        mApplicationContext = context.getApplicationContext();
        mMergePolicyName = mergePolicy;
        BroadcastBridgeLog.i(TAG, "init : merge policy is " + mMergePolicyName);

        switch (mMergePolicyName) {
            case MERGE_GLOBAL: {
                mPolicy = new MergePolicyGlobal(mApplicationContext, MERGE_GLOBAL);
                break;
            }
            case MERGE_AUTO: {
                mPolicy = new MergePolicyAuto(mApplicationContext, MERGE_AUTO);
                break;
            }
            case MERGE_SCHEME: {
                mPolicy = new MergePolicyScheme(mApplicationContext, MERGE_SCHEME);
                break;
            }
            case MERGE_PERMISSION: {
                mPolicy = new MergePolicyPermission(mApplicationContext, MERGE_PERMISSION);
                break;
            }
            /*case MERGE_NONE: {
                //TODO : not supported now, zhangjigang 20190322
                mPolicy = new MergePolicyNone(mApplicationContext);
                break;
            }*/
            default: {
                throw new IllegalArgumentException("Unsupported policy " + mergePolicy);
            }
        }

        mInitialized = true;
    }

    /**
     * 初始化BroadcastBridge
     *
     * @param context
     * @param policy 自定义策略，可以继承MergePolicyBase实现自定义策略
     */
    public void init(Context context, IMergePolicy policy) {
        if (mInitialized) {
            BroadcastBridgeLog.w(TAG, "No need to init twice");
            return;
        }
        if (context == null) {
            throw new IllegalStateException("context is null !!!");
        }
        mApplicationContext = context.getApplicationContext();
        mPolicy = policy;
        mMergePolicyName = policy.getMergePolicyName();

        BroadcastBridgeLog.i(TAG, "init : merge policy is " + mMergePolicyName);

        mInitialized = true;

    }


    public synchronized boolean registerBroadcastListener(Listener listener) {

        if (listener == null) {
            return false;
        }
        return mPolicy.registerListener(listener);
    }

    public synchronized boolean unregisterBroadcastListener(Listener listener) {
        if (listener == null) {
            return false;
        }
        return mPolicy.unregisterListener(listener);
    }
}
