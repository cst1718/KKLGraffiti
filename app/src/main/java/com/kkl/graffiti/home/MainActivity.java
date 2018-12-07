package com.kkl.graffiti.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kkl.graffiti.AppConfig;
import com.kkl.graffiti.BaseActivity;
import com.kkl.graffiti.BaseFragment;
import com.kkl.graffiti.Constants;
import com.kkl.graffiti.R;
import com.kkl.graffiti.common.util.PermissionUtils;
import com.kkl.graffiti.common.util.ResourceUtils;
import com.kkl.graffiti.common.util.UriUtils;
import com.kkl.graffiti.doodle.DoodleActivity;
import com.kkl.graffiti.home.fragment.AboutFragment;
import com.kkl.graffiti.home.fragment.MyPhotoFragment;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private static final int PERMISSION_LOOK   = 2112;
    private static final int PERMISSION_CAMERA = 2113;
    private static final int REQ_LOOK          = 1114;
    private static final int REQ_CAMERA        = 1115;
    private static final int REQ_DOODLE        = 1116;

    private MainActivity    mActivity = MainActivity.this;
    private String          mCameraTempPath;
    private MyPhotoFragment mPhotoFragmet;
    private AboutFragment   mAboutFragment;
    private BaseFragment    mCurFragment;
    private TextView        mTvTitle;
    private TextView        mTvSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        mCameraTempPath = AppConfig.getTempDirPath(this) + File.separator + Constants.TEMP_JPG_NAME;
    }

    private void initView() {
        mPhotoFragmet = new MyPhotoFragment();
        mAboutFragment = new AboutFragment();
        mTvTitle = findViewById(R.id.tv_main_title);
        RadioGroup radioGroup = findViewById(R.id.rg_main_tab);
        radioGroup.setOnCheckedChangeListener(this);
        radioGroup.check(R.id.one_tab_rl);
        mTvSelect = findViewById(R.id.tv_main_tab_select);
        mTvSelect.setOnClickListener(this);
    }

    private void showBottomSelect(View view) {
        String[] menu = getResources().getStringArray(R.array.home_tab_select);
        final BottomListPopwindow dialog = new BottomListPopwindow(mActivity, menu);
        dialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        if (android.os.Build.VERSION.SDK_INT >= 23) {
                            ArrayList<String> strings = PermissionUtils.checkPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            if (strings == null || strings.isEmpty()) {
                                go2SystemPictures();
                            } else {
                                PermissionUtils.reqPermissions(PERMISSION_LOOK, mActivity, strings);
                            }
                        } else {
                            go2SystemPictures();
                        }
                        break;
                    case 1:
                        if (android.os.Build.VERSION.SDK_INT >= 23) {
                            ArrayList<String> strings = PermissionUtils.checkPermission(mActivity,
                                                                                        Manifest.permission.CAMERA,
                                                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            if (strings == null || strings.isEmpty()) {
                                go2SystemCamera();
                            } else {
                                PermissionUtils.reqPermissions(PERMISSION_CAMERA, mActivity, strings);
                            }
                        } else {
                            go2SystemCamera();
                        }
                        break;
                    case 2:// 新建
                        go2DoodleActivity(null);
                        break;

                }
                dialog.dismiss();
            }
        });
        dialog.showAtBottom(view);
    }

    private void go2SystemPictures() {
        Intent intent = new Intent("android.intent.action.PICK", (Uri) null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQ_LOOK);
    }

    private void go2SystemCamera() {
        // 保存在内部缓存中可以删掉也可以不删
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri fileUri = UriUtils.getFileUri(this, mCameraTempPath);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, REQ_CAMERA);
    }

    private void go2DoodleActivity(String path) {
        Intent activityIntent = DoodleActivity.getActivityIntent(this, path, DoodleActivity.Type.NORMAL);
        startActivityForResult(activityIntent, REQ_DOODLE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_main_tab_select) {
            showBottomSelect(v);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.one_tab_rl:
                if (mPhotoFragmet != mCurFragment) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    if (!mPhotoFragmet.isAdded()) {
                        ft.add(R.id.fl_main_content, mPhotoFragmet, "mPhotoFragmet");
                    }
                    ft.show(mPhotoFragmet);
                    mCurFragment = mPhotoFragmet;
                    ft.hide(mAboutFragment);
                    ft.commitNowAllowingStateLoss();
                    mTvTitle.setText(ResourceUtils.getResourcesString(R.string.home_tab_photo_title));
                }
                break;
            case R.id.three_tab_rl:
                if (mAboutFragment != mCurFragment) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    if (!mAboutFragment.isAdded()) {
                        ft.add(R.id.fl_main_content, mAboutFragment, "mCurFragment");
                    }
                    ft.show(mAboutFragment);
                    mCurFragment = mAboutFragment;
                    ft.hide(mPhotoFragmet);
                    ft.commitNowAllowingStateLoss();
                    mTvTitle.setText(ResourceUtils.getResourcesString(R.string.home_tab_about_title));
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CAMERA://相机两个权限相机权限,sd卡权限
                if (grantResults == null || grantResults.length == 0) {
                    Toast.makeText(this, "您已拒绝了APP使用相机的权限，如需使用，请在手机系统的权限设置中启用。", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (int i = 0; i < grantResults.length; i++) {
                    if (i == 0 && grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "您已拒绝了APP使用相机的权限，如需使用，请在手机系统的权限设置中启用。", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (i == 1 && grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "您已拒绝了APP使用SD卡的权限，如需使用，请在手机系统的权限设置中启用。", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                go2SystemCamera();
                break;
            case PERMISSION_LOOK://查看相册两个权限,sd卡权限
                if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    go2SystemPictures();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQ_LOOK:// 相册选择
                    if (data != null) {
                        Uri uri = data.getData();// 图片源uri
                        go2DoodleActivity(UriUtils.getPathFromUri(mActivity, uri));
                    }
                    break;
                case REQ_CAMERA://拍照
                    go2DoodleActivity(mCameraTempPath);
                    break;
                case REQ_DOODLE:// 刷新
                    if (mPhotoFragmet != null) {
                        mPhotoFragmet.refreshPhoto();
                    }
                    break;
            }
        }
    }

    // 此方法是为了防止三星手机调用相机导致activity销毁重启,同时
    // android:launchMode="singleTask" android:configChanges="orientation|keyboardHidden|screenSize"
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
