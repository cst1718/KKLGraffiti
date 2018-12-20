package com.kkl.graffiti;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.baidu.autoupdatesdk.AppUpdateInfo;
import com.baidu.autoupdatesdk.AppUpdateInfoForInstall;
import com.baidu.autoupdatesdk.BDAutoUpdateSDK;
import com.baidu.autoupdatesdk.CPCheckUpdateCallback;
import com.baidu.autoupdatesdk.CPUpdateDownloadCallback;
import com.baidu.autoupdatesdk.UICheckUpdateCallback;

public class TestActivity extends Activity implements View.OnClickListener {

    private TextView txtLog;

    private ProgressDialog dialog;

    private CheckBox mCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mCheckBox = (CheckBox) findViewById(R.id.usehttps);
        findViewById(R.id.btn_ui).setOnClickListener(this);
        findViewById(R.id.btn_silence).setOnClickListener(this);
        findViewById(R.id.btn_noui).setOnClickListener(this);
        txtLog = (TextView) findViewById(R.id.txt_log);
        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ui:
                txtLog.setText("");
                dialog.show();
                BDAutoUpdateSDK.uiUpdateAction(this, new MyUICheckUpdateCallback(), mCheckBox.isChecked());
                break;
            case R.id.btn_silence:
                txtLog.setText("");
                BDAutoUpdateSDK.silenceUpdateAction(this, mCheckBox.isChecked());
                break;
            case R.id.btn_noui:
                txtLog.setText("");
                dialog.show();
                BDAutoUpdateSDK.cpUpdateCheck(this, new MyCPCheckUpdateCallback(), mCheckBox.isChecked());
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        dialog.dismiss();
        super.onDestroy();
    }

    private class MyUICheckUpdateCallback implements UICheckUpdateCallback {

        @Override
        public void onNoUpdateFound() {

        }

        @Override
        public void onCheckComplete() {
            dialog.dismiss();
        }

    }

    private class MyCPCheckUpdateCallback implements CPCheckUpdateCallback {

        @Override
        public void onCheckUpdateCallback(final AppUpdateInfo info, AppUpdateInfoForInstall infoForInstall) {
            if (infoForInstall != null && !TextUtils.isEmpty(infoForInstall.getInstallPath())) {
                txtLog.setText(txtLog.getText() + "\n install info: " + infoForInstall.getAppSName() + ", \nverion="
                                       + infoForInstall.getAppVersionName() + ", \nchange log=" + infoForInstall.getAppChangeLog());
                txtLog.setText(txtLog.getText() + "\n we can install the apk file in: "
                                       + infoForInstall.getInstallPath());
                BDAutoUpdateSDK.cpUpdateInstall(getApplicationContext(), infoForInstall.getInstallPath());
            } else if (info != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TestActivity.this);
                long size = info.getAppPathSize() > 0 ? info.getAppPathSize() : info.getAppSize();
                builder.setTitle(info.getAppVersionCode() + ", " + byteToMb(size))
                        .setMessage(Html.fromHtml(info.getAppChangeLog()))
                        .setNeutralButton("普通升级", null)
                        .setPositiveButton("智能升级", null)
                        .setCancelable(info.getForceUpdate() != 1)
                        .setOnKeyListener(new DialogInterface.OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                if (keyCode == KeyEvent.KEYCODE_BACK) {
                                    return true;
                                }
                                return false;
                            }
                        });
                if (info.getForceUpdate() != 1) {
                    builder.setNegativeButton("暂不升级", null);
                }
                AlertDialog dialog = builder.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                        new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                BDAutoUpdateSDK.cpUpdateDownloadByAs(TestActivity.this);
                            }
                        });
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BDAutoUpdateSDK.cpUpdateDownload(TestActivity.this, info, new UpdateDownloadCallback());
                    }
                });
            } else {
                txtLog.setText(txtLog.getText() + "\n no update.");
            }

            dialog.dismiss();
        }

        private String byteToMb(long fileSize) {
            float size = ((float) fileSize) / (1024f * 1024f);
            return String.format("%.2fMB", size);
        }

    }

    private class UpdateDownloadCallback implements CPUpdateDownloadCallback {

        @Override
        public void onDownloadComplete(String apkPath) {
            txtLog.setText(txtLog.getText() + "\n onDownloadComplete: " + apkPath);
            BDAutoUpdateSDK.cpUpdateInstall(getApplicationContext(), apkPath);
        }

        @Override
        public void onStart() {
            txtLog.setText(txtLog.getText() + "\n Download onStart");
        }

        @Override
        public void onPercent(int percent, long rcvLen, long fileSize) {
            txtLog.setText(txtLog.getText() + "\n Download onPercent: " + percent + "%");
        }

        @Override
        public void onFail(Throwable error, String content) {
            txtLog.setText(txtLog.getText() + "\n Download onFail: " + content);
        }

        @Override
        public void onStop() {
            txtLog.setText(txtLog.getText() + "\n Download onStop");
        }

    }

}
