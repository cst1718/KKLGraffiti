package com.kkl.graffiti.doodle.detection;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

/**
 * 边缘检测方法,算法类继承
 * Created by smile on 2016/9/19.
 */
public abstract class Detection {

    protected IDetectionResult mCallback;

    Detection(@NonNull IDetectionResult callback) {
        this.mCallback = callback;
    }

    /**
     * 进行边缘检测
     *
     * @param originalBitmap 原始图片
     * @return
     */
    public abstract void detection(Bitmap originalBitmap);
}
