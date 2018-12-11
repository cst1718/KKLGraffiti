package com.kkl.graffiti;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.kkl.graffiti.common.util.ResourceUtils;

/**
 * @author cst1718 on 2018/11/28 18:04
 * @explain
 */
public class BaseActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ResourceUtils.init(this);
    }

    /** 通知当前activity的其他fragment */
    public void notifyOtherFragment(Object o) {

    }
}
