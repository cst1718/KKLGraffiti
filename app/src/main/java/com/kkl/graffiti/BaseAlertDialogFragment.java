package com.kkl.graffiti;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/**
 * @author cst1718 on 2018/4/18 20:53
 * @explain
 */
public abstract class BaseAlertDialogFragment extends DialogFragment {

    private static final String TAG = "BaseAlertDialogFragment";
    private Window mDialogWindow;
    private float  mDimAmount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.MyDialog);
        setCancelable(false);
        initBundleDate();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDialogWindow = getDialog().getWindow();
        mDialogWindow.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams params = mDialogWindow.getAttributes();
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = 0;
        int height = 0;
        if (getWidthRate() > 0) {
            width = (int) (dm.widthPixels * getWidthRate() + .5f);
        } else if (getWidthRate() > -2) {// 不要用match,全面屏会把后面的页面顶上去,用dm的高度和宽度
            width = dm.widthPixels;
        } else {
            width = WindowManager.LayoutParams.WRAP_CONTENT;
        }
        if (getHeightRate() > 0) {
            height = (int) (dm.heightPixels * getHeightRate() + .5f);
        } else if (getHeightRate() > -2) {
            height = dm.heightPixels;
        } else {
            height = WindowManager.LayoutParams.WRAP_CONTENT;
        }
        params.width = width;
        params.height = height;
        params.gravity = getGravity();
        mDimAmount = getDiaAmount();
        params.dimAmount = mDimAmount;// 越大越暗
        mDialogWindow.setAttributes(params);
    }

    protected float getDiaAmount() {
        return 0.6f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(getContentViewLayoutId(), container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initView(view);
    }

    /**
     * 位置
     *
     * @see android.view.Gravity#BOTTOM
     */
    protected abstract int getGravity();

    /**
     * 传入百分比,-1全屏match -2自适应wrap
     *
     * @see WindowManager.LayoutParams#MATCH_PARENT
     * @see WindowManager.LayoutParams#WRAP_CONTENT
     */
    protected abstract double getWidthRate();

    /**
     * 传入百分比,-1全屏match -2自适应wrap
     *
     * @see WindowManager.LayoutParams#MATCH_PARENT
     * @see WindowManager.LayoutParams#WRAP_CONTENT
     */
    protected abstract double getHeightRate();

    protected void initBundleDate() {

    }

    protected abstract int getContentViewLayoutId();

    protected abstract void initView(View view);

    @Override
    public void show(FragmentManager manager, String tag) {
        setDim(mDimAmount);
        super.show(manager, tag);
    }

    @Override
    public int show(FragmentTransaction transaction, String tag) {
        setDim(mDimAmount);
        return super.show(transaction, tag);
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
//        setDim(0);
        super.onDismiss(dialog);
    }

    @Override
    public void dismiss() {
        dismissAllowingStateLoss();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //        super.onSaveInstanceState(outState);
    }

    private void setDim(float alpha) {
        if (mDialogWindow == null) {
            return;
        }
        if (alpha == 0) {
            mDialogWindow.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        } else {
            mDialogWindow.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
        WindowManager.LayoutParams params = mDialogWindow.getAttributes();
        params.dimAmount = alpha;// 越大越暗
        mDialogWindow.setAttributes(params);
    }
}
