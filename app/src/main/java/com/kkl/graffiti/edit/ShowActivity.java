package com.kkl.graffiti.edit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.kkl.graffiti.AppConfig;
import com.kkl.graffiti.BaseActivity;
import com.kkl.graffiti.R;
import com.kkl.graffiti.common.util.ImageCache;
import com.kkl.graffiti.common.util.ImageUtils;
import com.kkl.graffiti.common.util.UriUtils;
import com.kkl.graffiti.edit.adapter.ShowAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author cst1718 on 2018/12/19 14:37
 * @explain 展示图
 */
public class ShowActivity extends BaseActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {

    private static final int    MAX_CACHE  = 1024 * 1024 * 50;// 20M3
    private static final int    REQ_DOODLE = 110;
    private static final String INDEX      = "index";

    private ShowActivity         mActivity;
    private ViewPager            mViewPager;
    private ArrayList<String>    mPhotoPathList;
    private ArrayList<ImageView> mImageList;
    private ImageCache           mCache;
    private ShowAdapter          mAdapter;
    private int                  mLastPostion;
    private int                  mStartIndex;// 进来的index
    private TextView             mTitle;
    private View                 mLayoutTop;
    private View                 mLayoutBottom;
    private TranslateAnimation   mShowTopAction;
    private TranslateAnimation   mShowBottomAction;
    private TranslateAnimation   mGoneTopAction;
    private TranslateAnimation   mGoneBottomAction;

    public static Intent getActivityIntent(Activity activity, int position) {
        Intent intent = new Intent();
        intent.setClass(activity, ShowActivity.class);
        intent.putExtra(INDEX, position);
        return intent;
    }

    @Override
    public int getContentViewLayoutId() {
        return R.layout.activity_show;
    }

    @Override
    public void initViewsAndListeners() {
        if (!initIntent()) {
            finish();
            return;
        }
        initView();
    }

    private boolean initIntent() {
        if (getIntent() == null) {
            return false;
        }
        mStartIndex = getIntent().getIntExtra(INDEX, 0);
        mPhotoPathList = getDirList();
        if (mPhotoPathList == null || mPhotoPathList.isEmpty()) {
            return false;
        }
        return true;
    }

    private void initView() {
        mActivity = this;
        mViewPager = findViewById(R.id.show_viewpager);
        findViewById(R.id.tv_show_edit).setOnClickListener(this);
        findViewById(R.id.tv_show_shape).setOnClickListener(this);
        findViewById(R.id.iv_show_back).setOnClickListener(this);
        mTitle = findViewById(R.id.tv_show_title);
        mLayoutTop = findViewById(R.id.rl_show_title);
        mLayoutBottom = findViewById(R.id.ll_show_edit);
        initViewPager();
        initAnimation();
        updateTitle();
    }

    private void initViewPager() {
        // 只加3个ImageView,填充的具体图片随轮播改变,注意加载防止oom
        mCache = new ImageCache(mActivity, MAX_CACHE, 0, null);
        mImageList = new ArrayList<>(3);
        ImageView iv;
        for (int i = 0; i < 3; i++) {
            iv = new ImageView(this);
            iv.setOnClickListener(this);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            mImageList.add(iv);
        }
        mAdapter = new ShowAdapter(mImageList, mViewPager);
        mViewPager.setAdapter(mAdapter);

        // 把ViewPager设置为默认选中Integer.MAX_VALUE / t2，从十几亿次开始轮播图片，达到无限循环目的;
        if (mImageList.size() == 0) {
            finish();
            return;
        }
        int mid = Integer.MAX_VALUE / 2;
        int x = mid % mPhotoPathList.size();// 中间索引的第一张图
        int y = (mid - x) % mImageList.size();// 第一张图片对应的ImageView索引

        // 只有三个视图,显示第一张图,第二个视图是第二张,上一个视图为最后一张
        mImageList.get(y).setImageBitmap(getBitmap(mPhotoPathList.get(mStartIndex)));
        mImageList.get((y + 1) % mImageList.size()).setImageBitmap(getBitmap(mPhotoPathList.get(mStartIndex == mPhotoPathList.size() - 1 ? 0 : mStartIndex + 1)));
        mImageList.get((y + 2) % mImageList.size()).setImageBitmap(getBitmap(mPhotoPathList.get(mStartIndex == 0 ? mPhotoPathList.size() - 1 : mStartIndex - 1)));

        mViewPager.setCurrentItem(mid - x);// 显示第一张图片
        mLastPostion = mid - x;
        mViewPager.setOnPageChangeListener(this);
    }

    private void initAnimation() {
        mShowTopAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                                                Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mGoneTopAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                                                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f);
        mShowBottomAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                                                   Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mGoneBottomAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                                                   Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
        mShowTopAction.setDuration(500);
        mGoneTopAction.setDuration(500);
        mShowBottomAction.setDuration(500);
        mGoneBottomAction.setDuration(500);
    }

    private void updateTitle() {
        mTitle.setText((mLastPostion + mStartIndex) % mPhotoPathList.size() + 1 + "/" + mPhotoPathList.size());
    }

    private void showLayout(boolean show) {
        if (show && (mLayoutTop.getVisibility() == View.VISIBLE || mLayoutBottom.getVisibility() == View.VISIBLE)) {
            return;
        }
        if (!show && (mLayoutTop.getVisibility() != View.VISIBLE || mLayoutBottom.getVisibility() != View.VISIBLE)) {
            return;
        }
        mLayoutTop.startAnimation(show ? mShowTopAction : mGoneTopAction);
        mLayoutBottom.startAnimation(show ? mShowBottomAction : mGoneBottomAction);
        mLayoutTop.setVisibility(show ? View.VISIBLE : View.GONE);
        mLayoutBottom.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private ArrayList<String> getDirList() {
        File[] files = new File(AppConfig.getSaveDirPath()).listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        //每张图片的id值，原地址值，缓存地址值为一个map
        ArrayList<String> list = new ArrayList<>(files.length);
        Arrays.sort(files);// 数组只有升序,此时是按照文件名从小到大
        for (File file : files) {
            if (file.isDirectory() || !file.getName().endsWith("jpg") || file.length() < 100) {
                continue;
            }
            list.add(0, file.getAbsolutePath());
        }
        return list;
    }

    private Bitmap getBitmap(String path) {
        Bitmap bitmapFromPath = mCache.getBitmapMemoryCache(path);
        if (bitmapFromPath == null) {
            bitmapFromPath = ImageUtils.createBitmapFromPath(path, mActivity);
            mCache.saveBitmapMemoryCache(bitmapFromPath, path);
        }
        return bitmapFromPath;
    }

    private void go2DoodleActivity(String path) {
        Intent activityIntent = EditActivity.getActivityIntent(mActivity, path, EditActivity.Type.NOEDGE);
        startActivityForResult(activityIntent, REQ_DOODLE);
    }

    private void go2Shape() {
        Intent share_intent = new Intent();
        share_intent.setAction(Intent.ACTION_SEND);//设置分享行为
        share_intent.setType("image/*");
        Uri fileUri = UriUtils.getImageContentUri(mActivity, mPhotoPathList.get((mLastPostion + mStartIndex) % mPhotoPathList.size()));
        share_intent.putExtra(Intent.EXTRA_STREAM, fileUri);//添加分享内容
        // 系统分享弹窗的标题
        share_intent = Intent.createChooser(share_intent, "分享给我的朋友");
        share_intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // 不加new_task,系统分享没有回调,回来后会显示还是继续分享
        startActivity(share_intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_show_back:
                finish();
                break;
            case R.id.tv_show_shape:
                go2Shape();
                break;
            case R.id.tv_show_edit:
                go2DoodleActivity(mPhotoPathList.get((mLastPostion + mStartIndex) % mPhotoPathList.size()));
                break;
            default:
                showLayout(true);
                break;
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int position) {
        int imageViewIndex;
        int BitmapIndex;
        if (position < mLastPostion) {// 往右滑,需要替换上一页的图片
            imageViewIndex = (position - 1) % mImageList.size();
            BitmapIndex = (position + mStartIndex - 1) % mPhotoPathList.size();
        } else {// 往左滑,需要替换下一页的图片
            imageViewIndex = (position + 1) % mImageList.size();
            BitmapIndex = (position + mStartIndex + 1) % mPhotoPathList.size();
        }
        String path = mPhotoPathList.get(BitmapIndex);
        mImageList.get(imageViewIndex).setImageBitmap(getBitmap(path));
        mLastPostion = position;
        updateTitle();
    }

    @Override
    public void onPageScrollStateChanged(int i) {
        if (i == 1) {// 开始滑动
            showLayout(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_DOODLE && resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
        }
        finish();
    }
}
