package com.kkl.graffiti.setting;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
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
import com.kkl.graffiti.setting.adapter.PPTAdapter;
import com.kkl.graffiti.setting.adapter.PageTransformer1;
import com.kkl.graffiti.setting.adapter.PageTransformerNormal;
import com.kkl.graffiti.view.CustomScrollerView;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author cst1718 on 2018/12/17 14:25
 * @explain
 */
public class PPTActivity extends BaseActivity
        implements View.OnClickListener, ViewPager.OnPageChangeListener {

    private static final int MAX_CACHE = 1024 * 1024 * 50;// 20M

    private PPTActivity          mActivity;
    private ViewPager            mViewPager;
    private ArrayList<ImageView> mImageList;
    private ImageCache           mCache;
    private ArrayList<String>    mPhotoPathList;
    private int                  mLastPostion;// 上一个索引
    private PPTAdapter           mAdapter;
    private boolean              mIsPlay;//是否自动播放

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg == null) {
                return true;
            }
            mViewPager.setCurrentItem(msg.arg1, true);
            return false;
        }
    });

    private CustomScrollerView mCustomScrollerView;
    private TextView           mGoOn;
    private TranslateAnimation mShowTopAction;
    private TranslateAnimation mGoneTopAction;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ppt);
        mActivity = this;
        mPhotoPathList = getDirList();
        if (mPhotoPathList == null || mPhotoPathList.isEmpty()) {
            finish();
            return;
        }
        initView();
        initViewPager();
        startPlay();
    }

    private void initView() {
        mCache = new ImageCache(mActivity, MAX_CACHE, 0, null);

        // 只加3个ImageView,填充的具体图片随轮播改变,注意加载防止oom
        mViewPager = findViewById(R.id.ppt_viewpager);
        mGoOn = findViewById(R.id.tv_ppt_go_on);
        mGoOn.setOnClickListener(this);
        mImageList = new ArrayList<>(3);
        ImageView iv;
        for (int i = 0; i < 3; i++) {
            iv = new ImageView(this);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            mImageList.add(iv);
        }
        mAdapter = new PPTAdapter(mImageList, mViewPager);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(this);
        try {

            Field field = ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
           /* Field interpolator = ViewPager.class.getDeclaredField("sInterpolator");
            interpolator.setAccessible(true);*/

            // 可以设置过渡时间
            mCustomScrollerView = new CustomScrollerView(mActivity/*,(Interpolator) interpolator.get(null)*/);
            mCustomScrollerView.setDuration(5000);

            field.set(mViewPager, mCustomScrollerView);// 利用反射设置mScroller域为自己定义的Scroller
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 初始化刚开始视图 */
    private void initViewPager() {
        // 把ViewPager设置为默认选中Integer.MAX_VALUE / t2，从十几亿次开始轮播图片，达到无限循环目的;
        if (mImageList.size() == 0) {
            finish();
            return;
        }
        int mid = Integer.MAX_VALUE / 2;
        int x = mid % mPhotoPathList.size();// 第一张图
        int y = (mid - x) % mImageList.size();// 第一张图片对应的ImageView索引

        // 只有三个视图,显示第一张图,第二个视图是第二张,上一个视图为最后一张
        mImageList.get(y).setImageBitmap(getBitmap(mPhotoPathList.get(0)));
        mImageList.get((y + 1) % mImageList.size()).setImageBitmap(getBitmap(mPhotoPathList.get(mPhotoPathList.size() == 1 ? 0 : 1)));
        mImageList.get((y + 2) % mImageList.size()).setImageBitmap(getBitmap(mPhotoPathList.get(mPhotoPathList.size() - 1)));

        mViewPager.setCurrentItem(mid - x);// 以第一张图为准
        mLastPostion = mid - x;
    }

    private Bitmap getBitmap(String path) {
        Bitmap bitmapFromPath = mCache.getBitmapMemoryCache(path);
        if (bitmapFromPath == null) {
            bitmapFromPath = ImageUtils.createBitmapFromPath(path, mActivity);
            mCache.saveBitmapMemoryCache(bitmapFromPath, path);
        }
        return bitmapFromPath;
    }

    // 开始自动播放
    private void startPlay() {
        mIsPlay = true;
        showTitleLayout(false);
        mCustomScrollerView.setDuration(7000);
        Message obtain = Message.obtain();
        obtain.arg1 = mLastPostion + 1;
        mHandler.sendMessageDelayed(obtain, 3000);
        mViewPager.setPageTransformer(true, new PageTransformer1());
    }

    private void stopPlay() {
        mHandler.removeCallbacksAndMessages(null);
        mIsPlay = false;
        showTitleLayout(true);
        mCustomScrollerView.setDuration(250);
        mViewPager.setPageTransformer(true, new PageTransformerNormal());
    }

    private void showTitleLayout(boolean show) {
        if (mShowTopAction == null) {
            mShowTopAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                                                    Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
            mShowTopAction.setDuration(500);
        }
        if (mGoneTopAction == null) {
            mGoneTopAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                                                    Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f);
            mGoneTopAction.setDuration(500);
        }
        mGoOn.startAnimation(show ? mShowTopAction : mGoneTopAction);
        mGoOn.setVisibility(show ? View.VISIBLE : View.GONE);
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_ppt_go_on) {
            startPlay();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {// 动态替换imageView控件里面的bitmap
        int imageViewIndex;
        int BitmapIndex;
        if (position < mLastPostion) {// 往右滑,需要替换上一页的图片
            imageViewIndex = (position - 1) % mImageList.size();
            BitmapIndex = (position - 1) % mPhotoPathList.size();
        } else {// 往左滑,需要替换下一页的图片
            imageViewIndex = (position + 1) % mImageList.size();
            BitmapIndex = (position + 1) % mPhotoPathList.size();
        }
        String path = mPhotoPathList.get(BitmapIndex);
        mImageList.get(imageViewIndex).setImageBitmap(getBitmap(path));

        mLastPostion = position;
        // 自动播放
        if (mIsPlay) {
            Message obtain = Message.obtain();
            obtain.arg1 = mLastPostion + 1;
            mHandler.sendMessageDelayed(obtain, 4500);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // 触摸了屏幕停止自动播放
        if (ev.getAction() == MotionEvent.ACTION_DOWN && mIsPlay) {
            stopPlay();
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onDestroy() {
        mCache.clearAllMemoryCache();
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
