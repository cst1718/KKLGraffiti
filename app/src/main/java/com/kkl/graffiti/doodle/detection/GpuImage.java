package com.kkl.graffiti.doodle.detection;

import android.app.Activity;
import android.graphics.Bitmap;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageThresholdEdgeDetection;

/**
 * @author cst1718 on 2018/12/4 11:19
 * @explain
 */
public class GpuImage extends Detection {

    private final GPUImage mGpuImage;

    public GpuImage(IDetectionResult callback, Activity activity) {
        super(callback);
        mGpuImage = new GPUImage(activity);
    }

    @Override
    public void detection(Bitmap originalBitmap) {
        mGpuImage.setImage(originalBitmap);
    }

    /** 边缘检测,1到0 */
    public void createBitmap(float threshold) {
        GPUImageThresholdEdgeDetection detection = new GPUImageThresholdEdgeDetection();
        detection.setThreshold(threshold);
        mGpuImage.setFilter(detection);
        mCallback.photoResult(mGpuImage.getBitmapWithFilterApplied());
    }
}
