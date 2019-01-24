package com.kkl.graffiti;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

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

        if(immersionStatusBar()){
            changeStatusBarColor();
        }
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

    /** 是否沉浸式状态栏 */
    protected boolean immersionStatusBar() {
        return false;
    }

    private void changeStatusBarColor() {
        getWindow().getDecorView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            private View statusBarView;

            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                getWindow().getDecorView().removeOnLayoutChangeListener(this);
                if (statusBarView == null) {
                    //利用反射机制修改状态栏背景
                    int identifier = getResources().getIdentifier("statusBarBackground", "id", "android");
                    statusBarView = getWindow().findViewById(identifier);
                }
                if (statusBarView != null) {
                    statusBarView.setBackgroundResource(R.drawable.statusbar_bg);
                }
            }
        });
    }
}
