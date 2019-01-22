package com.kkl.graffiti.common.interfaces;

import android.graphics.Bitmap;

/**
 * @author cst1718 on 2019/1/19 11:37
 * @explain 滤镜生成图片回调
 */
public interface IFilterResult {

    /** 注意线程 */
    public void photoResult(Bitmap bitmap);
}
