package com.kkl.graffiti.doodle.dialog;

import android.view.Gravity;
import android.view.View;

import com.kkl.graffiti.BaseAlertDialogFragment;

/**
 * @author cst1718 on 2018/11/27 21:07
 * @explain
 */
public class SizeSelectDialog extends BaseAlertDialogFragment {

    public static SizeSelectDialog getSizeSelectDialog() {
        SizeSelectDialog dialog = new SizeSelectDialog();
        return dialog;
    }

    @Override
    protected int getGravity() {
        return Gravity.CENTER;
    }

    @Override
    protected double getWidthRate() {
        return 0;
    }

    @Override
    protected double getHeightRate() {
        return 0;
    }

    @Override
    protected int getContentViewLayoutId() {
        return 0;
    }

    @Override
    protected void initView(View view) {

    }


}
