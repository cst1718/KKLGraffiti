package com.kkl.graffiti.edit.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.kkl.graffiti.BaseFragment;
import com.kkl.graffiti.R;
import com.kkl.graffiti.edit.EditActivity;
import com.kkl.graffiti.edit.view.MyCropView;

/**
 * @author cst1718 on 2019/1/17 14:14
 * @explain 裁剪
 */
public class CropFragment extends BaseFragment {

    private static final String PATH = "path";// 出入图片的地址

    private MyCropView   mCropView;
    private EditActivity mActivity;
    private String       mSelectPath;

    public static CropFragment getInstance(String path) {
        CropFragment fragment = new CropFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PATH, path);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_crop, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity = (EditActivity) getActivity();
        if (getArguments() == null) {
            Toast.makeText(mActivity, "图片路径不能为空", Toast.LENGTH_SHORT).show();
            mActivity.finish();
            return;
        }
        mSelectPath = getArguments().getString(PATH);
        initView(view);
    }

    private void initView(View view) {
        // 图片裁剪
        mCropView = view.findViewById(R.id.cv_edit_crop);
        // 初始化视图完成之后再设置,不然设置图片并没有重新绘制测量高度不对
        mCropView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mCropView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mCropView.setBmpPath(mSelectPath, mActivity);
            }
        });
    }

    public Bitmap getCroppedImage() {
        return mCropView.getCroppedImage();
    }
}
