package com.qiku.broadcasts;

import android.content.Context;
import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MergePolicyAuto extends MergePolicyBase {

    static class Category {
        private String mName;
        private List<String> mCategoryActions;

        Category(String name, List<String> categoryActions) {
            mName = name;
            mCategoryActions = categoryActions;
        }

        String getName() {
            return mName;
        }

        List<String> getCategoryActions() {
            return mCategoryActions;
        }

        @Override
        public String toString() {
            return "Category{" +
                    "mName='" + mName + '\'' +
                    '}';
        }
    }

    private ArrayList<String> mKeyguardActions = new ArrayList<String>() {
        {
            add("android.intent.action.SCREEN_ON");
            add("android.intent.action.SCREEN_OFF");
            add("android.intent.action.USER_PRESENT");
        }
    };

    private ArrayList<String> mPackageActions = new ArrayList<String>() {
        {
            add("android.intent.action.PACKAGE_ADDED");
            add("android.intent.action.PACKAGE_REPLACED");
            add("android.intent.action.PACKAGE_REMOVED");
        }
    };


    private ArrayList<String> mNetActions = new ArrayList<String>() {
        {
            add("android.net.conn.CONNECTIVITY_CHANGE");
        }
    };

    private ArrayList<String> mBatteryActions = new ArrayList<String>() {
        {
            add("android.intent.action.BATTERY_CHANGED");
            add("android.intent.action.BATTERY_LOW");
            add("android.intent.action.BATTERY_OKAY");
        }
    };

    private ArrayList<String> mPowerActions = new ArrayList<String>() {
        {
            add("android.intent.action.ACTION_POWER_CONNECTED");
            add("android.intent.action.ACTION_POWER_DISCONNECTED");
        }
    };

    private ArrayList<String> mTimeActions = new ArrayList<String>() {
        {
            add("android.intent.action.TIME_TICK");
            add("android.intent.action.TIME_SET");
            add("android.intent.action.DATE_CHANGED");
            add("android.intent.action.TIMEZONE_CHANGED");
        }
    };

    private Category mKeyguardCategory = new Category("keyguard", mKeyguardActions);
    private Category mPackageCategory = new Category("package", mPackageActions);
    private Category mNetCategory = new Category("net", mNetActions);
    private Category mBatteryCategory = new Category("battery", mBatteryActions);
    private Category mPowerCategory = new Category("power", mPowerActions);
    private Category mTimeCategory = new Category("time", mTimeActions);


    private ArrayMap<String, Category> mActionsToCategory = new ArrayMap<>();

    {
        mActionsToCategory.put("android.intent.action.SCREEN_ON", mKeyguardCategory);
        mActionsToCategory.put("android.intent.action.SCREEN_OFF", mKeyguardCategory);
        mActionsToCategory.put("android.intent.action.USER_PRESENT", mKeyguardCategory);

        mActionsToCategory.put("android.intent.action.PACKAGE_ADDED", mPackageCategory);
        mActionsToCategory.put("android.intent.action.PACKAGE_REPLACED", mPackageCategory);
        mActionsToCategory.put("android.intent.action.PACKAGE_REMOVED", mPackageCategory);
        mActionsToCategory.put("android.intent.action.PACKAGE_CHANGED", mPackageCategory);

        mActionsToCategory.put("android.net.conn.CONNECTIVITY_CHANGE", mNetCategory);

        mActionsToCategory.put("android.intent.action.BATTERY_CHANGED", mBatteryCategory);
        mActionsToCategory.put("android.intent.action.BATTERY_LOW", mBatteryCategory);
        mActionsToCategory.put("android.intent.action.BATTERY_OKAY", mBatteryCategory);

        mActionsToCategory.put("android.intent.action.ACTION_POWER_CONNECTED", mPowerCategory);
        mActionsToCategory.put("android.intent.action.ACTION_POWER_DISCONNECTED", mPowerCategory);

        mActionsToCategory.put("android.intent.action.TIME_TICK", mTimeCategory);
        mActionsToCategory.put("android.intent.action.TIME_SET", mTimeCategory);
        mActionsToCategory.put("android.intent.action.DATE_CHANGED", mTimeCategory);
        mActionsToCategory.put("android.intent.action.TIMEZONE_CHANGED", mTimeCategory);
    }


    public MergePolicyAuto(Context context, String name) {
        super(context, name);
    }


    /**
     * 当前MergePolicy的分类方法
     *
     * @param listener
     * @return
     */
    @Override
    public String getActionCategory(BroadcastBridge.Listener listener) {

        HashSet<String> temp = new HashSet<>();
        for (String action : listener.getActionList()) {
            Category category = mActionsToCategory.get(action);
            if (category != null) {
                temp.add(category.getName());
            } else {
                // 如果action不在预设的category中，那么这个action作为一个category
                temp.add(action);
            }
        }

        if (temp.size() > 1) {
            // listener中的action属于不同的category
            throw new IllegalArgumentException("Actions " + listener.getActionList() +
                    " in listener " + listener);
        }

        return temp.iterator().next();
    }

    @Override
    public boolean isSchemeSupported() {
        return true;
    }

    @Override
    public boolean isPermissionSupported() {
        return false;
    }
}
