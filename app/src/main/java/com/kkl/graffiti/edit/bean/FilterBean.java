package com.kkl.graffiti.edit.bean;

import android.graphics.Bitmap;

/**
 * @author cst1718 on 2019/1/18 11:35
 * @explain
 */
public class FilterBean {
    public boolean mIsSelect;// 是否选中
    public boolean mHasAdjuster;// 是否可以调节
    public String  mName;
    public Bitmap  mBitmap;
    public int     mRaw;// 滤镜颜色曲线资源id,在res/raw中,目前先设置0为默认,-1为素描
}
