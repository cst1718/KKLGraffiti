package com.kkl.graffiti.edit.detection;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.kkl.graffiti.common.interfaces.IFilterResult;

/**
 * 边缘检测方法,算法类继承
 * Created by smile on 2016/9/19.
 */
@Deprecated
public abstract class Detection {

    protected IFilterResult mCallback;

    Detection(@NonNull IFilterResult callback) {
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
