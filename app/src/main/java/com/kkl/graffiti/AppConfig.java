package com.kkl.graffiti;

import android.app.Activity;
import android.os.Environment;

import java.io.File;

/**
 * @author cst1718 on 2018/12/4 15:02
 * @explain
 */
public class AppConfig {

    public static String getTempDirPath(Activity activity) {
        return activity.getCacheDir().getAbsolutePath();
    }

    public static String getSaveDirPath() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + Constants.SAVE_DIR_NAME;
    }
}
