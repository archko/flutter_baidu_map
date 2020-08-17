package com.archko.map.baidu_map;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;

/**
 * @author: archko 2020/8/14 :15:48
 */
public class SDKInit {

    public static boolean hasInit = false;

    public static void init(Context context) {
        if (!hasInit) {
            SDKInitializer.initialize(context);
            hasInit = true;
        }
    }
}
