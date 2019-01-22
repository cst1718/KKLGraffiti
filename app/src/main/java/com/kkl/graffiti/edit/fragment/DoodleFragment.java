package com.kkl.graffiti.edit.fragment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.kkl.graffiti.BaseFragment;
import com.kkl.graffiti.R;
import com.kkl.graffiti.edit.EditActivity;
import com.kkl.graffiti.edit.dialog.ColorPickerDialog;
import com.kkl.graffiti.edit.dialog.SelectSizeAndAlphaDialog;
import com.kkl.graffiti.edit.drawType.BaseDrawType;
import com.kkl.graffiti.edit.view.SimpleDoodleView;

/**
 * @author cst1718 on 2019/1/17 14:15
 * @explain 涂鸦
 */
public class DoodleFragment extends BaseFragment implements View.OnTouchListener, View.OnClickListener {

    private EditActivity     mActivity;
    private View             mLayoutEdit;
    private SimpleDoodleView mDoodleView;
    private ImageView        mIvLast;
    private ImageView        mIvNormal;
    private ImageView        mIvEraser;
    private ImageView        mIvPain;
    private ImageView        mIvColor;
    private ImageView        mIvSize;
    private Bitmap           mNoraml;
    private Bitmap           mChange;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_doodle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(final View view) {
        // 涂鸦
        mDoodleView = view.findViewById(R.id.dv_doodle_doodle);
        // 画栏
        mLayoutEdit = view.findViewById(R.id.layout_doodle_edit);

        mIvLast = view.findViewById(R.id.iv_doodle_last);
        mIvNormal = view.findViewById(R.id.iv_doodle_normal);
        mIvEraser = view.findViewById(R.id.iv_doodle_eraser);
        mIvPain = view.findViewById(R.id.iv_doodle_pain);
        mIvColor = view.findViewById(R.id.iv_doodle_color);
        mIvSize = view.findViewById(R.id.iv_doodle_size);
        mIvPain.setSelected(true);

        mIvNormal.setOnTouchListener(this);

        mIvLast.setOnClickListener(this);
        mIvEraser.setOnClickListener(this);
        mIvPain.setOnClickListener(this);
        mIvColor.setOnClickListener(this);
        mIvSize.setOnClickListener(this);

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (mNoraml == null) {// 空白画板
                    mNoraml = Bitmap.createBitmap(view.getWidth(), view.getHeight() - mLayoutEdit.getHeight(), Bitmap.Config.ARGB_8888);
                    mNoraml.eraseColor(Color.WHITE);
                }
                mDoodleView.setCanvasBackground(mNoraml, mChange);
                mDoodleView.setVisibility(View.VISIBLE);//原先的状态必须不为可见才,设置可见之后才会重新调用view的onMeasure才可以测量高度
            }
        });
    }

    public Bitmap getDoodleBitmap() {
        return mDoodleView.getEndMap();
    }

    /**
     * @param normal 裁剪后的图
     * @param change 滤镜转换过后的图,如果没有滤镜转换,可传null
     */
    public void setDoodleBitmap(Bitmap normal, Bitmap change) {
        mNoraml = normal;
        mChange = change;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.iv_doodle_normal) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mIvNormal.setPressed(true);
                    mDoodleView.showNormalBitmap(true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_OUTSIDE:
                    mIvNormal.setPressed(false);
                    mDoodleView.showNormalBitmap(false);
                    break;
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_doodle_last:// 上一步
                mDoodleView.back(1);
                break;
            case R.id.iv_doodle_eraser:// 橡皮擦
                mDoodleView.setDrawType(BaseDrawType.Type.Eraser);
                mIvEraser.setSelected(true);
                mIvPain.setSelected(false);
                break;
            case R.id.iv_doodle_pain:// 画笔
                mDoodleView.setDrawType(BaseDrawType.Type.Curve);
                mIvEraser.setSelected(false);
                mIvPain.setSelected(true);
                break;
            case R.id.iv_doodle_size:// 字号大小
                SelectSizeAndAlphaDialog dialog = SelectSizeAndAlphaDialog.getSizeSelectDialog(
                        mDoodleView.getPaintSize(), mDoodleView.getPaintAlpha(), mDoodleView.getPainColor(), mIvEraser.isSelected());
                dialog.setOnButtonClickCallback(new SelectSizeAndAlphaDialog.onProgressResult() {
                    @Override
                    public void onResult(int size, int alpha) {
                        mDoodleView.setSize(size);
                        mDoodleView.setLineAlpha(alpha);
                    }
                });
                dialog.show(getFragmentManager(), "SelectSizeAndAlphaDialog");
                break;
            case R.id.iv_doodle_color:// 颜色
                ColorPickerDialog dialog1 = ColorPickerDialog.getColorPickerDialog(mDoodleView.getPainColor());
                dialog1.setOnButtonClickCallback(new ColorPickerDialog.onColorProgressResult() {
                    @Override
                    public void onColorResult(int color) {
                        mDoodleView.setColor(color);
                    }
                });
                dialog1.show(getFragmentManager(), "ColorPickerDialog");
                break;
        }
    }

    @Override
    public void onDestroy() {
        if (mNoraml != null) {
            mNoraml.recycle();
            mNoraml = null;
        }
        if (mChange != null) {
            mChange.recycle();
            mChange = null;
        }
        super.onDestroy();
    }
}
