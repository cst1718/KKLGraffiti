package com.kkl.graffiti.home.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kkl.graffiti.AppConfig;
import com.kkl.graffiti.BaseActivity;
import com.kkl.graffiti.BaseFragment;
import com.kkl.graffiti.Constants;
import com.kkl.graffiti.R;
import com.kkl.graffiti.common.util.PermissionUtils;
import com.kkl.graffiti.common.util.UriUtils;
import com.kkl.graffiti.doodle.DoodleActivity;
import com.kkl.graffiti.doodle.SolidPicActivity;
import com.kkl.graffiti.home.MainActivity;
import com.kkl.graffiti.setting.PPTActivity;
import com.kkl.graffiti.setting.SettingActivity;
import com.kkl.graffiti.setting.TutorialsActivity;
import com.kkl.graffiti.view.AboutView;

import java.io.File;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * @author cst1718 on 2018/12/15 14:54
 * @explain
 */
public class LeftMenuFragment extends BaseFragment implements View.OnClickListener {

    private static final int PERMISSION_LOOK   = 2112;
    private static final int PERMISSION_CAMERA = 2113;
    private static final int REQ_LOOK          = 1114;
    private static final int REQ_CAMERA        = 1115;

    private BaseActivity mActivity;
    private AboutView    mCamera;
    private AboutView    mAlbum;
    private AboutView    mNormal;
    private AboutView    mNewDraw;
    private AboutView    mPpt;
    private AboutView    mTutorials;
    private AboutView    mSetting;
    private String       mCameraTempPath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_left_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity = (BaseActivity) getActivity();
        mCamera = view.findViewById(R.id.left_menu_camera);
        mAlbum = view.findViewById(R.id.left_menu_album);
        mNormal = view.findViewById(R.id.left_menu_normal);
        mNewDraw = view.findViewById(R.id.left_menu_new);
        mPpt = view.findViewById(R.id.left_menu_powerpoint);
        mTutorials = view.findViewById(R.id.left_menu_tutorials);
        mSetting = view.findViewById(R.id.left_menu_setting);
        mCamera.setOnClickListener(this);
        mAlbum.setOnClickListener(this);
        mNormal.setOnClickListener(this);
        mNewDraw.setOnClickListener(this);
        mPpt.setOnClickListener(this);
        mTutorials.setOnClickListener(this);
        mSetting.setOnClickListener(this);

        mCameraTempPath = AppConfig.getTempDirPath(mActivity) + File.separator + Constants.TEMP_JPG_NAME;
    }

    private void go2SystemPictures() {
        Intent intent = new Intent("android.intent.action.PICK", (Uri) null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQ_LOOK);
    }

    private void go2SystemCamera() {
        // 保存在内部缓存中可以删掉也可以不删
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri fileUri = UriUtils.getFileUri(mActivity, mCameraTempPath);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, REQ_CAMERA);
    }

    /** 在activity中回调 */
    private void go2DoodleActivity(String path) {
        Intent activityIntent = DoodleActivity.getActivityIntent(mActivity, path, DoodleActivity.Type.NORMAL);
        mActivity.startActivityForResult(activityIntent, MainActivity.REQ_DOODLE);
    }

    private void go2SettingActivity() {
        Intent activityIntent = new Intent(mActivity, SettingActivity.class);
        startActivity(activityIntent);
    }

    private void go2TutorialsActivity() {
        Intent activityIntent = new Intent(mActivity, TutorialsActivity.class);
        startActivity(activityIntent);
    }

    private void go2PPTActivity() {
        Intent activityIntent = new Intent(mActivity, PPTActivity.class);
        startActivity(activityIntent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_menu_camera:// 相机
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
            case R.id.left_menu_album:// 相册
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
            case R.id.left_menu_normal:// 自带
                go2SolidPicActivity();
                break;
            case R.id.left_menu_new:// 新建
                go2DoodleActivity(null);
                break;
            case R.id.left_menu_powerpoint:// 幻灯片
                go2PPTActivity();
                break;
            case R.id.left_menu_tutorials:// 教程
                go2TutorialsActivity();
                break;
            case R.id.left_menu_setting:// 设置
                go2SettingActivity();
                break;
        }
    }

    private void go2SolidPicActivity() {
        startActivity(new Intent(mActivity, SolidPicActivity.class));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CAMERA://相机两个权限相机权限,sd卡权限
                if (grantResults == null || grantResults.length == 0) {
                    Toast.makeText(mActivity, "您已拒绝了APP使用相机的权限，如需使用，请在手机系统的权限设置中启用。", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (int i = 0; i < grantResults.length; i++) {
                    if (i == 0 && grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(mActivity, "您已拒绝了APP使用相机的权限，如需使用，请在手机系统的权限设置中启用。", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (i == 1 && grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(mActivity, "您已拒绝了APP使用SD卡的权限，如需使用，请在手机系统的权限设置中启用。", Toast.LENGTH_SHORT).show();
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
}
