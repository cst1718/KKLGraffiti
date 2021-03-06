package com.kkl.graffiti.edit.dialog;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kkl.graffiti.BaseAlertDialogFragment;
import com.kkl.graffiti.R;
import com.kkl.graffiti.common.util.ResourceUtils;
import com.kkl.graffiti.edit.view.SimpleCurveView;

/**
 * @author cst1718 on 2018/11/27 21:07
 * @explain
 */
public class SelectSizeAndAlphaDialog extends BaseAlertDialogFragment
        implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    private final static String SIEZ   = "size";
    private final static String ALPHA  = "alpha";
    private final static String ERASER = "eraser";
    private final static String COLOR  = "color";

    private SeekBar          mSizeSeekbar;
    private SeekBar          mAlaphaSeekbar;
    private SimpleCurveView  mCurveView;
    private onProgressResult mCallback;
    private boolean          mIsEraser;//橡皮擦设置
    private TextView         mTvSize;
    private TextView         mTvAlpha;

    public static SelectSizeAndAlphaDialog getSizeSelectDialog(int size, int alpha, int color, boolean eraser) {
        SelectSizeAndAlphaDialog dialog = new SelectSizeAndAlphaDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(SIEZ, size);
        bundle.putInt(ALPHA, alpha);
        bundle.putInt(COLOR, color);
        bundle.putBoolean(ERASER, eraser);
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
        return 0.6f;
    }

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.dialog_select_size;
    }

    @Override
    protected void initView(View view) {
        setCancelable(true);
        mCurveView = view.findViewById(R.id.cv_select_view);
        mSizeSeekbar = view.findViewById(R.id.sk_select_size);
        mAlaphaSeekbar = view.findViewById(R.id.sk_select_alpha);
        TextView tvTitle = view.findViewById(R.id.dialog_select_title);
        mTvSize = view.findViewById(R.id.tv_select_size);
        mTvAlpha = view.findViewById(R.id.tv_select_alpha);

        mSizeSeekbar.setOnSeekBarChangeListener(this);
        mAlaphaSeekbar.setOnSeekBarChangeListener(this);
        view.findViewById(R.id.select_right_button_text).setOnClickListener(this);
        view.findViewById(R.id.select_left_button_text).setOnClickListener(this);

        mSizeSeekbar.setMax(100);
        mAlaphaSeekbar.setMax(255);
        int size = getArguments().getInt(SIEZ);
        int alpha = Math.abs(getArguments().getInt(ALPHA) - 255);
        mSizeSeekbar.setProgress(size);
        mAlaphaSeekbar.setProgress(alpha);
        mTvSize.setText(ResourceUtils.getResourcesString(R.string.dialog_select_size, size));
        mTvAlpha.setText(ResourceUtils.getResourcesString(R.string.dialog_select_alpha, (alpha * 100 / 255) + "%"));

        mIsEraser = getArguments().getBoolean(ERASER);
        if (mIsEraser) {
            tvTitle.setText(ResourceUtils.getResourcesString(R.string.dialog_select_title_eraser));
            view.findViewById(R.id.ll_select_alpha).setVisibility(View.GONE);
            mSizeSeekbar.setMax(150);
        } else {
            // 动态设置颜色
            GradientDrawable thumb = (GradientDrawable) mAlaphaSeekbar.getThumb();
            GradientDrawable thumb2 = (GradientDrawable) mSizeSeekbar.getThumb();
            thumb.setColor(getArguments().getInt(COLOR));
            thumb2.setColor(getArguments().getInt(COLOR));
            mSizeSeekbar.invalidate();
            mAlaphaSeekbar.invalidate();
            mCurveView.setPaintColor(getArguments().getInt(COLOR));
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar == mAlaphaSeekbar) {
            mCurveView.setLineAlpha(Math.abs(progress - 255));
            mTvAlpha.setText(ResourceUtils.getResourcesString(R.string.dialog_select_alpha, (progress * 100 / 255) + "%"));
        } else if (seekBar == mSizeSeekbar) {
            mCurveView.setLineWidth(progress);
            mTvSize.setText(ResourceUtils.getResourcesString(R.string.dialog_select_size, progress));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_left_button_text:
                break;
            case R.id.select_right_button_text:
                if (mCallback != null) {
                    mCallback.onResult(mSizeSeekbar.getProgress(), Math.abs(mAlaphaSeekbar.getProgress() - 255));
                }
                break;
        }
        dismiss();
    }

    public interface onProgressResult {
        void onResult(int size, int alpha);
    }

    public void setOnButtonClickCallback(onProgressResult callback) {
        this.mCallback = callback;
    }
}
