package com.story.demo.qlivepusher.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by qiuyayong on 2019/2/22.
 * Email:qiuyayong@bigo.sg
 */

public class DisplayUtil {

    public static int getScreenWidth(Context context)
    {
        DisplayMetrics metric = context.getResources().getDisplayMetrics();
        return metric.widthPixels;
    }

    public static int getScreenHeight(Context context)
    {
        DisplayMetrics metric = context.getResources().getDisplayMetrics();
        return metric.heightPixels;
    }
}
