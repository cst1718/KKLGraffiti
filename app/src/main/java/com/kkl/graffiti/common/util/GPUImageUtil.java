package com.kkl.graffiti.common.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageAddBlendFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageAlphaBlendFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageBilateralFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageBoxBlurFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageBulgeDistortionFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageCGAColorspaceFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageChromaKeyBlendFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageColorBalanceFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;

/**
 * @author cst1718 on 2018/12/3 20:54
 * @explain
 */
public class GPUImageUtil {
    private static GPUImageFilter filter;

    /**
     * 获取过滤器
     *
     * @param GPUFlag
     * @return 滤镜类型
     */
    public static GPUImageFilter getFilter(int GPUFlag) {
        switch (GPUFlag) {
            case 1:
                filter = new GPUImageGrayscaleFilter();
                break;
            case 2:
                filter = new GPUImageAddBlendFilter();
                break;
            case 3:
                filter = new GPUImageAlphaBlendFilter();
                break;
            case 4:
                filter = new GPUImageBilateralFilter();
                break;
            case 5:
                filter = new GPUImageBoxBlurFilter();
                break;
            case 6:
                filter = new GPUImageBrightnessFilter();
                break;
            case 7:
                filter = new GPUImageBulgeDistortionFilter();
                break;
            case 8:
                filter = new GPUImageCGAColorspaceFilter();
                break;
            case 9:
                filter = new GPUImageChromaKeyBlendFilter();
                break;
            case 10:
                filter = new GPUImageColorBalanceFilter();
                break;
        }
        return filter;
    }

    public static Bitmap getGpuImage(Context context, GPUImage gpuImage, int FilterFlag) {
        AssetManager as = context.getAssets();
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = as.open("link.jpg");
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            Log.e("GPUImage", "Error");
        }

        // 使用GPUImage处理图像
        gpuImage = new GPUImage(context);
        gpuImage.setImage(bitmap);
        gpuImage.setFilter(getFilter(FilterFlag));
        bitmap = gpuImage.getBitmapWithFilterApplied();
        return bitmap;
    }

    public static Bitmap getGPUImageFromURL(String url) {
        Bitmap bitmap = null;
        try {
            URL imageUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
            // 获得图像的字符流
            InputStream is = connection.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bitmap = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();// 关闭流
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
