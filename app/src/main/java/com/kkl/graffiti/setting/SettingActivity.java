package com.kkl.graffiti.setting;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kkl.graffiti.AppConfig;
import com.kkl.graffiti.BaseActivity;
import com.kkl.graffiti.BaseAlertDialogFragment;
import com.kkl.graffiti.R;
import com.kkl.graffiti.common.interfaces.IDialogsCallBack;
import com.kkl.graffiti.common.util.AppUtils;
import com.kkl.graffiti.common.util.FileUtils;
import com.kkl.graffiti.common.util.ResourceUtils;
import com.kkl.graffiti.doodle.dialog.NormalAlertDialog;
import com.kkl.graffiti.view.AboutView;

import java.io.File;

/**
 * @author cst1718 on 2018/12/17 13:11
 * @explain
 */
public class SettingActivity extends BaseActivity implements View.OnClickListener {

    private SettingActivity mActivity;
    private AboutView       mClean;
    private AboutView       mVersion;
    private TextView        mTitle;
    private ImageView       mBack;
    private boolean         mCanClean;
    private boolean         mCanUpdate;

    private static final float NUM = 1024f;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_setting);
        initView();
    }

    private void initView() {
        mActivity = SettingActivity.this;
        mClean = findViewById(R.id.av_about_clean);
        mVersion = findViewById(R.id.av_about_version);
        mTitle = findViewById(R.id.tv_about_title);
        mBack = findViewById(R.id.iv_about_back);
        mClean.setOnClickListener(this);
        mVersion.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mTitle.setText(ResourceUtils.getResourcesString(R.string.about_title));
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
            return String.format("%.2fMB", total / NUM / NUM);
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
            public void DialogsCallBack(IDialogsCallBack.ButtonType buttonType, BaseAlertDialogFragment thisDialogs) {
                thisDialogs.dismiss();
                switch (buttonType) {
                    case rightButton:
                        FileUtils.delete(AppConfig.getSaveDirPath());
                        mClean.setRightText(getCacheSize());
                        mCanClean = false;
                        mActivity.notifyOtherFragment(null);
                        break;
                }
            }
        });
        dialog.show(getSupportFragmentManager(), "showCleanDialog");
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
            case R.id.iv_about_back:
                finish();
                break;
        }
    }
}
