package com.kkl.graffiti.common.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

/**
 * @author cst1718 on 2018/12/4 10:20
 * @explain
 */
public class PermissionUtils {

    public static  ArrayList<String> checkPermission(Activity activity, @NonNull String...permissions) {
        ArrayList<String> permissionList = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            if (!checkPermission(activity, permissions[i]))
                permissionList.add(permissions[i]);
        }
        if (permissionList.isEmpty()) {
            return null;
        }
        return permissionList;
    }

    private static boolean checkPermission(Activity context, @NonNull String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static void reqPermissions(int reqCode, Activity activity,@NonNull ArrayList<String> list){
        ActivityCompat.requestPermissions(activity, list.toArray(new String[list.size()]), reqCode);
    }
}
