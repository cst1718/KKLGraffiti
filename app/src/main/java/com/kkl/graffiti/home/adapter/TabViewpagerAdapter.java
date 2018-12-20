package com.kkl.graffiti.home.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.kkl.graffiti.BaseFragment;

import java.util.ArrayList;

/**
 * @author cst1718 on 2018/12/15 14:13
 * @explain
 */
public class TabViewpagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<String>       mTitleList;
    private ArrayList<BaseFragment> mFragmentList;

    public TabViewpagerAdapter(FragmentManager fm, ArrayList<String> titleList, ArrayList<BaseFragment> fragmentList) {
        super(fm);
        mTitleList = titleList;
        mFragmentList = fragmentList;
    }

    @Override
    public Fragment getItem(int i) {
        return mFragmentList.get(i);
    }

    @Override
    public int getCount() {
        return mTitleList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mTitleList.get(position);
    }
}
