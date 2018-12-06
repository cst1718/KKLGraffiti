package com.kkl.graffiti.home.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kkl.graffiti.BaseFragment;
import com.kkl.graffiti.R;

/**
 * @author cst1718 on 2018/12/4 14:02
 * @explain
 */
public class AboutFragment extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_about, container, false);
    }
}
