package com.qiku.broadcasts;

import android.content.IntentFilter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

final class BroadcastBridgeUtil {
    private BroadcastBridgeUtil() {}

    static void removeAction(IntentFilter filter, String action) {
        Iterator<String> it = filter.actionsIterator();
        while (it.hasNext()) {
            String filterAction = it.next();
            if (action.equals(filterAction)) {
                it.remove();
                break;
            }
        }
    }

    static List<String> actionsOf(IntentFilter filter) {
        Iterator<String> it = filter.actionsIterator();
        List<String> list = new ArrayList<>();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }
}
