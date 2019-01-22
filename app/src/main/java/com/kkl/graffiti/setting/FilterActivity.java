package com.kkl.graffiti.setting;

import com.kkl.graffiti.BaseActivity;

/**
 * @author cst1718 on 2019/1/16 20:42
 * @explain 滤镜设置
 */
public class FilterActivity extends BaseActivity {
    @Override
    public int getContentViewLayoutId() {
        return 0;
    }

    @Override
    public void initViewsAndListeners() {
      /*  GPUImage gpuImage = new GPUImage(this);
        gpuImage.deleteImage();
        GPUImageToneCurveFilter curveFilter = new GPUImageToneCurveFilter();
         curveFilter.setFromCurveFileInputStream(getResources().openRawResource(R.raw.aimei));
        gpuImage.setFilter(curveFilter);
        gpuImage.setImage(bitmap);
        filterBitmap.add(gpuImage.getBitmapWithFilterApplied());*/

    }
}
