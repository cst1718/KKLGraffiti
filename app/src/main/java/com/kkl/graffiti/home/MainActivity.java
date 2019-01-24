package com.kkl.graffiti.home;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import com.kkl.graffiti.BaseActivity;
import com.kkl.graffiti.BaseFragment;
import com.kkl.graffiti.R;
import com.kkl.graffiti.common.util.ResourceUtils;
import com.kkl.graffiti.home.adapter.TabViewpagerAdapter;
import com.kkl.graffiti.home.fragment.MyPhotoFragment;
import com.kkl.graffiti.home.fragment.PiazzaFragment;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    public static final int REQ_DOODLE = 1116;

    private MainActivity    mActivity = MainActivity.this;
    private MyPhotoFragment mPhotoFragment;
    private ImageView       mIvMenu;
    private PiazzaFragment  mPiazzaFragment;
    private DrawerLayout    mDrawerLayout;

    @Override
    public int getContentViewLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initViewsAndListeners() {
        initView();
    }

    @Override
    protected boolean immersionStatusBar() {
        return true;
    }

    private void initView() {
        mPhotoFragment = new MyPhotoFragment();
        mPiazzaFragment = new PiazzaFragment();
        mDrawerLayout = findViewById(R.id.dl_main_drawerlayout);
        TabLayout tabLayout = findViewById(R.id.tab_main_layout);
        ViewPager viewPager = findViewById(R.id.vp_main_content);
        ArrayList<String> title = new ArrayList<>();
        title.add(ResourceUtils.getResourcesString(R.string.home_tab_photo_title));
        title.add(ResourceUtils.getResourcesString(R.string.home_tab_piazza_title));
        ArrayList<BaseFragment> fragmentList = new ArrayList<>();
        fragmentList.add(mPhotoFragment);
        fragmentList.add(mPiazzaFragment);
        TabViewpagerAdapter myViewPageAdapter = new TabViewpagerAdapter(getSupportFragmentManager(), title, fragmentList);
        viewPager.setAdapter(myViewPageAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabsFromPagerAdapter(myViewPageAdapter);
        mIvMenu = findViewById(R.id.iv_main_menu);
        mIvMenu.setOnClickListener(this);
    }

    private void showLeftMenu() {
        mDrawerLayout.openDrawer(Gravity.START);
    }


    @Override
    public void notifyOtherFragment(Object o) {
        super.notifyOtherFragment(o);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_main_menu) {
            showLeftMenu();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQ_DOODLE:// 刷新
                    if (mPhotoFragment != null) {
                        mPhotoFragment.refreshPhoto();
                    }
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(Gravity.START)) {
            mDrawerLayout.closeDrawer(Gravity.START);
        } else {
            super.onBackPressed();
        }
    }

    // 此方法是为了防止三星手机调用相机导致activity销毁重启,同时
    // android:launchMode="singleTask" android:configChanges="orientation|keyboardHidden|screenSize"
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
