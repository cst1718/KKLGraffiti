package com.kkl.graffiti.home.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kkl.graffiti.AppConfig;
import com.kkl.graffiti.BaseActivity;
import com.kkl.graffiti.BaseAlertDialogFragment;
import com.kkl.graffiti.BaseFragment;
import com.kkl.graffiti.R;
import com.kkl.graffiti.common.interfaces.IDialogsCallBack;
import com.kkl.graffiti.common.util.AppUtils;
import com.kkl.graffiti.common.util.FileUtils;
import com.kkl.graffiti.common.util.ResourceUtils;
import com.kkl.graffiti.doodle.dialog.NormalAlertDialog;
import com.kkl.graffiti.home.view.AboutView;

import java.io.File;

/**
 * @author cst1718 on 2018/12/4 14:02
 * @explain
 */
public class AboutFragment extends BaseFragment implements View.OnClickListener {

    private BaseActivity mActivity;
    private AboutView    mClean;
    private AboutView    mVersion;
    private boolean      mCanClean;
    private boolean      mCanUpdate;

    private static final float NUM = 1024f;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity = (BaseActivity) getActivity();
        mClean = view.findViewById(R.id.av_about_clean);
        mVersion = view.findViewById(R.id.av_about_version);
        mClean.setOnClickListener(this);
        mVersion.setOnClickListener(this);
    }

    private String getCacheSize() {
        File[] files = new File(AppConfig.getSaveDirPath()).listFiles();
        float total = 0;
        if (files != null) {
            for (File child : files) {
                if (child.isFile()) {
                    total += child.length();
                }
            }
            mCanClean = total != 0;
            return (float) (Math.round(total / NUM / NUM * 100)) / 100 + "M";
        }
        return "0M";
    }

    private void updateVersion() {
        mVersion.showArrow(mCanUpdate);
        mVersion.showAlert(mCanUpdate);
    }

    private void showCleanDialog() {
        NormalAlertDialog dialog = NormalAlertDialog.getNormalAlertDialog(ResourceUtils.getResourcesString(R.string.about_dialog_clean));
        dialog.setOnButtonClickCallback(new IDialogsCallBack() {
            @Override
            public void DialogsCallBack(ButtonType buttonType, BaseAlertDialogFragment thisDialogs) {
                thisDialogs.dismiss();
                switch (buttonType) {
                    case rightButton:
                        FileUtils.delete(AppConfig.getSaveDirPath());
                        mClean.setRightText(getCacheSize());
                        mActivity.notifyOtherFragment(null);
                        break;
                }
            }
        });
        dialog.show(getFragmentManager(), "showCleanDialog");
    }

    @Override
    public void onStart() {
        super.onStart();
        mClean.setRightText(getCacheSize());
        mVersion.setRightText(AppUtils.getApkVersion(mActivity));
        updateVersion();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.av_about_clean:
                if (mCanClean) {
                    showCleanDialog();
                }
                break;
            case R.id.av_about_version:
                if (!mCanUpdate) {
                    return;
                }

                break;
        }
    }
}
