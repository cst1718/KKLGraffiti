package com.kkl.graffiti.setting.adapter;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * @author cst1718 on 2018/12/18 10:58
 * @explain 默认空白效果, 即只是平滑进出
 */
public class PageTransformerNormal implements ViewPager.PageTransformer {

    @Override
    public void transformPage(View page, float position) {

        // 按照左边看不见,左边画面,右边画面,右边看不见的顺序

        if(position < -1){ // [负无穷，-1）:当前页面已经滑出左边屏幕，我们已经看不到了
            page.setAlpha(0F);
        } else if (position <= 0){ // [-1, 0]：当前页面向左画出，已远离中心位置，但还未滑出左屏幕
            page.setAlpha(1F);
            page.setTranslationX(0F);
            page.setScaleX(1F);
            page.setScaleY(1F);
        } else if (position <= 1){ // (0,1]:下一页面已经进入屏幕，但还在进入并未到达中间位置
            page.setAlpha(1F);
            page.setTranslationX(0F);
            page.setScaleX(1F);
            page.setScaleY(1F);
        } else { // (1, 正无穷]：下一页面还未进入屏幕
            page.setAlpha(0F);
        }
    }
}
