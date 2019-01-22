package com.kkl.graffiti.edit.fragment;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kkl.graffiti.BaseFragment;
import com.kkl.graffiti.R;
import com.kkl.graffiti.common.interfaces.IFilterResult;
import com.kkl.graffiti.common.interfaces.OnRecycleViewItemClickListener;
import com.kkl.graffiti.common.util.ResourceUtils;
import com.kkl.graffiti.edit.EditActivity;
import com.kkl.graffiti.edit.adapter.FilterAdapter;
import com.kkl.graffiti.edit.bean.FilterBean;
import com.kkl.graffiti.edit.filter.FilterHelper;
import com.kkl.graffiti.edit.filter.GPUImageAdjusterFilter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageThresholdEdgeDetection;
import jp.co.cyberagent.android.gpuimage.GPUImageToneCurveFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;

/**
 * @author cst1718 on 2019/1/17 14:13
 * @explain 滤镜
 */
public class FilterFragment extends BaseFragment
        implements RadioGroup.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {

    private EditActivity   mActivity;
    private Bitmap         mNormal;
    private Bitmap         mSamllIcon;
    private GPUImageView   mGPUImageView;
    private RecyclerView   mRecyclerView;
    private FilterAdapter  mAdapter;
    private ProgressDialog mProgressDlg;
    private RadioGroup     mLayoutAdjuster;

    private GPUImage                       mSmallGpuImage;//滤镜效果小图
    private GPUImageToneCurveFilter        mRawFilter;// 颜色曲线的filter
    private GPUImageFilter                 mNormalFilter;
    private GPUImageThresholdEdgeDetection mEdgeFilter;
    private GPUImageAdjusterFilter         mAdjusterFilter;
    private View                           mLayoutSeekbar;
    private TextView                       mTvSelect;
    private SeekBar                        mSeekBar;
    private int[]                          mAdjustArr;
    private int                            mFilterIndex;// 滤镜选择索引
    private int                            mAdjustIndex;// 调节器选择索引

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity = (EditActivity) getActivity();
        mProgressDlg = new ProgressDialog(mActivity);
        mProgressDlg.setCancelable(false);
        mProgressDlg.setMessage("正在生成中...");
        initGPUImage();
        initView(view);
        initProgress(view);
        initData();
    }

    private void initGPUImage() {
        mSmallGpuImage = new GPUImage(mActivity);
        mNormalFilter = new GPUImageFilter();// 默认
        mRawFilter = new GPUImageToneCurveFilter();// raw文件的滤镜
        mEdgeFilter = new GPUImageThresholdEdgeDetection();// 素描的滤镜,滤镜组
        mAdjusterFilter = new GPUImageAdjusterFilter();// 调节器的滤镜,滤镜组
        mEdgeFilter.setThreshold(0.8f);
        mEdgeFilter.setLineSize(1.0f);
    }

    private void initView(View view) {
        mLayoutAdjuster = view.findViewById(R.id.rg_filter_adjuster);
        mLayoutAdjuster.setOnCheckedChangeListener(this);
        RadioGroup layoutSelect = view.findViewById(R.id.rg_filter_select);
        layoutSelect.setOnCheckedChangeListener(this);

        mGPUImageView = view.findViewById(R.id.iv_filter_icon);
        mGPUImageView.setScaleType(GPUImage.ScaleType.CENTER_CROP);
        mGPUImageView.getLayoutParams().height = mNormal.getHeight();
        mGPUImageView.getLayoutParams().width = mNormal.getWidth();
        mGPUImageView.setImage(mNormal);

        mRecyclerView = view.findViewById(R.id.rv_filter_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new FilterAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClick(new OnRecycleViewItemClickListener<FilterBean>() {
            @Override
            public void onResultCallback(int index, FilterBean data, View view) {
                resetSeekBar();
                mFilterIndex = index;
                refreshFilter(getFilter(data));
            }
        });
    }

    private void initProgress(View view) {
        mLayoutSeekbar = view.findViewById(R.id.ll_filter_select);
        mTvSelect = view.findViewById(R.id.tv_filter_size);
        mSeekBar = view.findViewById(R.id.sk_filter_adjuster);
        mSeekBar.setProgress(50);
        mSeekBar.setOnSeekBarChangeListener(this);
        mAdjustArr = new int[]{50, 50, 50, 50};
    }

    private void initData() {
        SmallPicTask task = new SmallPicTask();
        task.execute();
    }


    /** 滤镜小图标初始化大小 */
    private void initSmallBitmap() {
        int smallWidth = ResourceUtils.getResourcesDimensionforInt(R.dimen.filter_icon_width);
        if (mNormal.getWidth() < smallWidth && mNormal.getHeight() < smallWidth) {
            mSamllIcon = mNormal;
        } else {
            if (mNormal.getWidth() > mNormal.getHeight()) {
                double i = mNormal.getWidth() * 1.0 / smallWidth;
                int height = (int) Math.floor(mNormal.getHeight() / i);
                mSamllIcon = Bitmap.createScaledBitmap(mNormal, smallWidth, height, false);
            } else {
                double i = mNormal.getHeight() * 1.0 / smallWidth;
                int width = (int) Math.floor(mNormal.getWidth() / i);
                mSamllIcon = Bitmap.createScaledBitmap(mNormal, width, smallWidth, false);
            }
        }
    }

    private GPUImageFilter getFilter(FilterBean bean) {
        if (bean.mRaw == 0) {// 原图无效果
            return mNormalFilter;
        } else if (bean.mRaw == -1) {// 素描画
            return mEdgeFilter;
        } else {
            mRawFilter.setFromCurveFileInputStream(getResources().openRawResource(bean.mRaw));
            return mRawFilter;
        }
    }

    /** 刷新大图 */
    private void refreshFilter(GPUImageFilter filter) {
        mGPUImageView.setFilter(filter);
    }

    public void setNormalBitmap(Bitmap normalBitmap) {
        mNormal = normalBitmap;
    }

    private void updateSeekBar(int adjustIndex) {
        mAdjusterFilter.setSelectIndex(adjustIndex);
        mSeekBar.setProgress(mAdjustArr[adjustIndex]);
        mTvSelect.setText(String.valueOf(mAdjustArr[adjustIndex] - 50));
    }

    /** 还原调节器 */
    private void resetSeekBar() {
        mAdjusterFilter.resetFilter();
        mSeekBar.setProgress(50);
        mAdjustArr[0] = 50;
        mAdjustArr[1] = 50;
        mAdjustArr[2] = 50;
        mAdjustArr[3] = 50;
    }

    /** 点击调节把当前的滤镜filter加上去显示 */
    private void changeAdjusterFilter() {
        mAdjusterFilter.setNewFilter(getFilter(mAdapter.getData().get(mFilterIndex)));
        mGPUImageView.setFilter(mAdjusterFilter);
    }

    /** 获取最终滤镜图片 */
    public void getFilterBitmap(final IFilterResult callback) {
        mProgressDlg.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 有时候会出现黑边,这个是GPUImage的问题,所以生成图片的时候剪掉2px
                    // 效率问题后续直接重写capture方法
                    Bitmap tempBitmap = mGPUImageView.capture();
                    final Bitmap bitmap = Bitmap.createBitmap(tempBitmap, 2, 2, tempBitmap.getWidth() - 4, tempBitmap.getHeight() - 4);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDlg.dismiss();
                            callback.photoResult(bitmap);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.tv_edit_filter:
                mRecyclerView.setVisibility(View.VISIBLE);
                mLayoutAdjuster.setVisibility(View.GONE);
                mLayoutSeekbar.setVisibility(View.GONE);
                mLayoutAdjuster.check(R.id.tv_filter_1);
                break;
            case R.id.tv_edit_adjust:
                mRecyclerView.setVisibility(View.GONE);
                mLayoutAdjuster.setVisibility(View.VISIBLE);
                mLayoutSeekbar.setVisibility(View.VISIBLE);
                changeAdjusterFilter();
                break;
            case R.id.tv_filter_1:
                updateSeekBar(0);
                break;
            case R.id.tv_filter_2:
                updateSeekBar(1);
                break;
            case R.id.tv_filter_3:
                updateSeekBar(2);
                break;
            case R.id.tv_filter_4:
                updateSeekBar(3);
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mTvSelect.setText(String.valueOf(progress - 50));
        Log.e("1718", "fromUser == " + fromUser);
        if (fromUser) {
            mAdjustArr[mAdjusterFilter.getSelectIndex()] = progress;
            mAdjusterFilter.setProgress(progress);
            mGPUImageView.requestRender();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private class SmallPicTask extends AsyncTask<Void, Void, ArrayList<FilterBean>> {

        @Override
        protected ArrayList<FilterBean> doInBackground(Void... voids) {
            initSmallBitmap();
            ArrayList<FilterBean> list = new ArrayList<>();
            LinkedHashMap<String, Integer> rawList = FilterHelper.getRawList();
            InputStream inputStream;
            mSmallGpuImage.setImage(mSamllIcon);
            mSmallGpuImage.setFilter(mRawFilter);
            for (String name : rawList.keySet()) {
                FilterBean bean = new FilterBean();
                bean.mName = name;
                bean.mRaw = rawList.get(name);
                inputStream = mActivity.getResources().openRawResource(bean.mRaw);
                mRawFilter.setFromCurveFileInputStream(inputStream);
                bean.mBitmap = mSmallGpuImage.getBitmapWithFilterApplied();
                list.add(bean);
            }

            FilterBean bean1 = new FilterBean();
            bean1.mName = "无效果";
            bean1.mRaw = 0;
            bean1.mIsSelect = true;
            bean1.mBitmap = mSamllIcon;
            list.add(0, bean1);

            FilterBean bean2 = new FilterBean();
            bean2.mName = "素描";
            bean2.mRaw = -1;
            mSmallGpuImage.setFilter(mEdgeFilter);
            bean2.mBitmap = mSmallGpuImage.getBitmapWithFilterApplied();
            list.add(1, bean2);

            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<FilterBean> list) {
            mAdapter.updateList(list);
        }
    }

    @Override
    public void onDestroyView() {
        if (mAdapter != null) {
            mAdapter.onDestroy();
        }
        super.onDestroyView();
    }
}
