package com.kkl.graffiti;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.kkl.graffiti.common.util.ResourceUtils;

/**
 * @author cst1718 on 2018/11/28 18:04
 * @explain
 */
public abstract class BaseActivity extends FragmentActivity {
    private String CLASS_TAG = getClass().getSimpleName();

    protected Activity mActivity;

    /**
     * 获取Layout ID。
     *
     * @return
     */
    public abstract int getContentViewLayoutId();

    /**
     * 初始化View和Listener。
     */
    public abstract void initViewsAndListeners();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        if (!beforeSetContentView()) {
            return;
        }

        initConfig();

        int resId = getContentViewLayoutId();
        setContentView(resId);

        afterSetContentView();
        initViewsAndListeners();
    }

    private void initConfig() {
        ResourceUtils.init(this);
    }

    /** 通知当前activity的其他fragment */
    public void notifyOtherFragment(Object o) {

    }

    protected boolean beforeSetContentView() {
        return true;
    }

    protected void afterSetContentView() {

    }
}
