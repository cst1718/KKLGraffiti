package com.kkl.graffiti.edit.filter;

import jp.co.cyberagent.android.gpuimage.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageHueFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSaturationFilter;

/**
 * @author cst1718 on 2019/1/19 15:40
 * @explain 调节器的组合, 包括亮度, 对比度, 饱和度, 色调
 */
public class GPUImageAdjusterFilter extends GPUImageFilterGroup {

    private int mIndex;

    public GPUImageAdjusterFilter() {
        super();
        addFilter(new GPUImageBrightnessFilter(0.0f));// 亮度 -1.0f~1.0f
        addFilter(new GPUImageContrastFilter(1.0f));//对比度 0~2
        addFilter(new GPUImageSaturationFilter(1.0f));//饱和度 0~2
        addFilter(new GPUImageHueFilter(0.0f));//色度 0~360,0与360为原本的颜色
    }

    public void setProgress(int progress) {
        switch (mIndex) {
            case 0:
                ((GPUImageBrightnessFilter) getFilters().get(1)).setBrightness(range(progress, -1.0f, 1.0f));
                break;
            case 1:
                ((GPUImageContrastFilter) getFilters().get(2)).setContrast(range(progress, 0f, 2.0f));
                break;
            case 2:
                ((GPUImageSaturationFilter) getFilters().get(3)).setSaturation(range(progress, 0f, 2.0f));
                break;
            case 3:
                if (progress <= 50) {
                    progress = progress + 50;// 实际效果以360~180方向变化
                } else {
                    progress = progress - 50;// 实际效果以0~180方向变化
                }
                ((GPUImageHueFilter) getFilters().get(4)).setHue(range(progress, 0f, 360f));
                break;
        }
    }

    public void setSelectIndex(int index) {
        mIndex = index;
    }

    public int getSelectIndex() {
        return mIndex;
    }

    public void setNewFilter(GPUImageFilter filter) {
        if (filter == null) {
            return;
        }
        mIndex = 0;
        if (!(getFilters().get(0) instanceof GPUImageBrightnessFilter)) {
            getFilters().remove(0);
        }
        mFilters.add(0, filter);
        updateMergedFilters();
    }

    public void resetFilter() {
        if (mIndex < 0) {
            return;
        }
        mIndex = -1;
        if (!(getFilters().get(0) instanceof GPUImageBrightnessFilter)) {
            getFilters().remove(0);
        }
        ((GPUImageBrightnessFilter) getFilters().get(0)).setBrightness(0.0f);
        ((GPUImageContrastFilter) getFilters().get(1)).setContrast(1.0f);
        ((GPUImageSaturationFilter) getFilters().get(2)).setSaturation(1.0f);
        ((GPUImageHueFilter) getFilters().get(3)).setHue(0.0f);
    }

    protected float range(final int percentage, final float start, final float end) {
        return (end - start) * percentage / 100.0f + start;
    }

    protected int range(final int percentage, final int start, final int end) {
        return (end - start) * percentage / 100 + start;
    }
}
