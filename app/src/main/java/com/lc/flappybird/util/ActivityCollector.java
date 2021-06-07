package com.lc.flappybird.util;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cnrobin on 17-10-17.
 * Just Enjoy It!!!
 */

public class ActivityCollector {
    public static List<Activity> activities = new ArrayList<>();

    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public static void removeAll() {
        for (Activity activity : activities) {
            activity.finish();
        }
    }
}
