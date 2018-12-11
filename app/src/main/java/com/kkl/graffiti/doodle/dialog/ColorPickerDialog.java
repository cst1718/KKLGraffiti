package com.kkl.graffiti.doodle.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import com.kkl.graffiti.BaseAlertDialogFragment;
import com.kkl.graffiti.R;
import com.kkl.graffiti.doodle.view.ColorPicker.ColorPicker;
import com.kkl.graffiti.doodle.view.ColorPicker.SVBar;

/**
 * @author cst1718 on 2018/12/6 20:14
 * @explain
 */
public class ColorPickerDialog extends BaseAlertDialogFragment implements View.OnClickListener {

    private final static String COLOR = "color";

    private ColorPicker           mColorView;
    private SVBar                 mSvBar;
    private int                   mColor;
    private onColorProgressResult mCallback;

    public static ColorPickerDialog getColorPickerDialog(int color) {
        ColorPickerDialog dialog = new ColorPickerDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(COLOR, color);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    protected int getGravity() {
        return Gravity.CENTER;
    }

    @Override
    protected double getWidthRate() {
        return 0.8f;
    }

    @Override
    protected double getHeightRate() {
        return -2;
    }

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.dialog_color_picker;
    }

    @Override
    protected void initBundleDate() {
        super.initBundleDate();
        if (getArguments() != null) {
            mColor = getArguments().getInt(COLOR);
        }
    }

    @Override
    protected void initView(View view) {
        setCancelable(true);
        mColorView = view.findViewById(R.id.dialog_color_picker_colorPicker);
        mSvBar = view.findViewById(R.id.dialog_color_picker_svbar);
        mColorView.addSVBar(mSvBar);
        mColorView.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                mColor = color;
            }
        });

        view.findViewById(R.id.color_left_button_text).setOnClickListener(this);
        view.findViewById(R.id.color_right_button_text).setOnClickListener(this);
        mColorView.setColor(mColor);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.color_left_button_text:
                break;
            case R.id.color_right_button_text:
                if (mCallback != null) {
                    mCallback.onColorResult(mColor);
                }
                break;
        }
        dismiss();
    }

    public interface onColorProgressResult {
        void onColorResult(int color);
    }

    public void setOnButtonClickCallback(onColorProgressResult callback) {
        this.mCallback = callback;
    }
}
