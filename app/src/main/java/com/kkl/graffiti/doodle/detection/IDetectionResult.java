package com.kkl.graffiti.doodle.detection;

import android.graphics.Bitmap;

/**
 * @author cst1718 on 2018/12/3 14:24
 * @explain
 */
public interface IDetectionResult {
    /** 注意线程 */
    public void photoResult(Bitmap bitmap);
}
