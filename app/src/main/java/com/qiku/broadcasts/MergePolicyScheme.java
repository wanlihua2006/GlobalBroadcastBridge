package com.qiku.broadcasts;

import android.content.Context;

import java.util.List;

/**
 * 如果App中注册的广播需要指定scheme,可以使用该MergePolicy
 *
 * 该MergePolicy会将相同scheme的Listener合并到一个category中
 * 没有指定scheme的Listener合并到一个叫做scheme:non的category中
 *
 * 如果该类不能满足App的要求，建议App参考该类实现自己的MergePolicy
 */
public class MergePolicyScheme extends MergePolicyBase {

    MergePolicyScheme(Context context, String name) {
        super(context, name);
    }

    /**
     * 在该方法中按照scheme为Listener分类
     *
     * @param listener
     * @return
     */
    @Override
    public String getActionCategory(BroadcastBridge.Listener listener) {
        List<String> schemes = listener.getDataSchemes();
        if (schemes == null || schemes.size() == 0) {
            return "scheme:non";
        } else {
            //将listener指定的所有scheme拼接，作为Listener的category
            StringBuilder sb = new StringBuilder("scheme:");
            for (String scheme : schemes) {
                sb.append(scheme).append("_");
            }
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
    }

    /**
     * 因为该策略就是根据scheme为Listener进行分类的，所以必须支持scheme
     * @return
     */
    @Override
    public boolean isSchemeSupported() {
        return true;
    }

    @Override
    public boolean isPermissionSupported() {
        return false;
    }
}
