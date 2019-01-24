package com.kkl.graffiti.edit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kkl.graffiti.AppConfig;
import com.kkl.graffiti.BaseActivity;
import com.kkl.graffiti.BaseAlertDialogFragment;
import com.kkl.graffiti.BaseFragment;
import com.kkl.graffiti.R;
import com.kkl.graffiti.common.interfaces.IDialogsCallBack;
import com.kkl.graffiti.common.interfaces.IFilterResult;
import com.kkl.graffiti.common.util.CloseableUtils;
import com.kkl.graffiti.common.util.FileUtils;
import com.kkl.graffiti.common.util.ResourceUtils;
import com.kkl.graffiti.edit.dialog.NormalAlertDialog;
import com.kkl.graffiti.edit.fragment.CropFragment;
import com.kkl.graffiti.edit.fragment.DoodleFragment;
import com.kkl.graffiti.edit.fragment.FilterFragment;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author cst1718 on 2018/11/28 18:03
 * @explain 涂鸦板画, 包括滤镜/图片裁剪和涂鸦
 */
public class EditActivity extends BaseActivity implements View.OnClickListener {


    private BaseFragment    mCurFragment;
    private CropFragment    mCropFragment;
    private DoodleFragment  mDoodleFragment;
    private FilterFragment  mFilterFragment;
    private FragmentManager mManager;

    public enum Type {
        NORMAL, // 普通
        NOEDGE, // 从我的作品中进来,不用素描转换
        NEW // 新建画板
    }

    private static final String PATH = "path";
    private static final String TYPE = "type";

    private EditActivity mActivity = EditActivity.this;

    private ImageView      mIvBack;
    private TextView       mTvSave;
    private TextView       mTvNext;
    private TextView       mTvTitle;
    private ProgressDialog mProgressDlg;
    private Bitmap         mNormal;
    private Type           mType;
    private String         mSelectPath;//相机或者相册得到的图片的绝对路径

    /** 从我的作品中进来的不做边缘检测 */
    public static Intent getActivityIntent(Activity activity, String path, Type type) {
        Intent intent = new Intent();
        intent.setClass(activity, EditActivity.class);
        intent.putExtra(PATH, path);
        intent.putExtra(TYPE, type.ordinal());
        return intent;
    }

    @Override
    public int getContentViewLayoutId() {
        return R.layout.activity_edit;
    }

    @Override
    public void initViewsAndListeners() {
        if (!initIntent()) {
            Toast.makeText(this, "图片有误", Toast.LENGTH_SHORT).show();
            finish();
        }
        initView();
        initFragment();
    }

    private boolean initIntent() {
        if (getIntent() != null) {
            mSelectPath = getIntent().getStringExtra(PATH);
            mType = Type.values()[getIntent().getIntExtra(TYPE, 1)];
            if (TextUtils.isEmpty(mSelectPath)) {
                mType = Type.NEW;
            }
            return true;
        }
        return false;
    }

    private void initView() {
        mProgressDlg = new ProgressDialog(this);
        mProgressDlg.setCancelable(false);

        // 标题栏
        mIvBack = findViewById(R.id.iv_edit_back);
        mTvNext = findViewById(R.id.tv_edit_next);
        mTvSave = findViewById(R.id.tv_edit_save);
        mTvTitle = findViewById(R.id.tv_edit_title);
        mIvBack.setOnClickListener(this);
        mTvNext.setOnClickListener(this);
        mTvSave.setOnClickListener(this);
    }


    private void initFragment() {
        mCropFragment = CropFragment.getInstance(mSelectPath);
        mDoodleFragment = new DoodleFragment();
        mFilterFragment = new FilterFragment();
        mManager = getSupportFragmentManager();
        if (mType == Type.NEW) {// 新建画板
            mTvTitle.setText(ResourceUtils.getResourcesString(R.string.edit_doodle_title));
            mTvNext.setVisibility(View.GONE);
            mCurFragment = mDoodleFragment;
            mDoodleFragment.setDoodleBitmap(null, null);
        } else {// 非新建的先显示裁剪
            mTvSave.setVisibility(View.GONE);
            mCurFragment = mCropFragment;
        }
        showFragment(mCurFragment);
    }

    private void showFragment(BaseFragment fragment) {
        FragmentTransaction transaction = mManager.beginTransaction();
        if (mCurFragment != fragment) {
            transaction.hide(mCropFragment);
        }
        if (!fragment.isAdded()) {
            transaction.add(R.id.fl_edit_content, fragment);
        }
        transaction.setCustomAnimations(
                R.anim.push_left_in,
                R.anim.push_left_out,
                R.anim.anim_push_left_in,
                R.anim.anim_push_left_out
        );
        transaction.show(fragment);
        transaction.commitNowAllowingStateLoss();
        mCurFragment = fragment;
    }

    /** 保存涂鸦 */
    private void showSavePhotoDialog() {
        NormalAlertDialog dialog = NormalAlertDialog.getNormalAlertDialog(ResourceUtils.getResourcesString(R.string.dialog_save_doodle_msg));
        dialog.setOnButtonClickCallback(new IDialogsCallBack() {
            @Override
            public void DialogsCallBack(ButtonType buttonType, BaseAlertDialogFragment thisDialogs) {
                thisDialogs.dismiss();
                switch (buttonType) {
                    case rightButton:
                        if (mCurFragment == mFilterFragment) {
                            mFilterFragment.getFilterBitmap(new IFilterResult() {
                                @Override
                                public void photoResult(Bitmap bitmap) {
                                    saveBitmap(bitmap);
                                }
                            });
                        } else {
                            saveBitmap(mDoodleFragment.getDoodleBitmap());
                        }
                        break;
                    case leftButton:
                        finish();
                        break;
                }
            }
        });
        dialog.show(getSupportFragmentManager(), "showSavePhotoDialog");
    }

    private void saveBitmap(Bitmap bitmap) {
        String appDir = AppConfig.getSaveDirPath();
        FileUtils.createDir(appDir);
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseableUtils.close(fos);
        }
        MediaScannerConnection.scanFile(this, new String[]{file.getAbsolutePath()}, null, null);
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        goBackMainActivity();

        // 魅族不回调,所以只要通知让系统搜索即可
        /*new SingleMediaScanner(mActivity, file, new SingleMediaScanner.ScanListener() {
            @Override
            public void onScanFinish() {
                mProgressDlg.dismiss();
                goBackMainActivity();
            }
        });*/
    }

    private void goBackMainActivity() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (mCurFragment == mFilterFragment || mCurFragment == mDoodleFragment) {
            showSavePhotoDialog();
        } else {
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_edit_back:// 返回
                onBackPressed();
                break;
            case R.id.tv_edit_next:// 下一步
                if (mCurFragment == mCropFragment) {// 裁剪页面点下一步,跳转滤镜
                    go2FilterFragment();
                } else if (mCurFragment == mFilterFragment) {// 滤镜页面点下一步,跳转涂鸦
                    go2DoodleFragment();
                }
                break;
            case R.id.tv_edit_save:
                showSavePhotoDialog();
                break;
        }
    }

    private void go2FilterFragment() {
        mTvTitle.setText(ResourceUtils.getResourcesString(R.string.edit_filter_title));
        mTvSave.setVisibility(View.VISIBLE);
        mNormal = mCropFragment.getCroppedImage();
        mFilterFragment.setNormalBitmap(mNormal);
        showFragment(mFilterFragment);
    }

    private void go2DoodleFragment() {
        mTvTitle.setText(ResourceUtils.getResourcesString(R.string.edit_doodle_title));
        mTvNext.setVisibility(View.GONE);
        mFilterFragment.getFilterBitmap(new IFilterResult() {

            @Override
            public void photoResult(Bitmap bitmap) {
                mDoodleFragment.setDoodleBitmap(mNormal, bitmap);
                showFragment(mDoodleFragment);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (mNormal != null) {
            mNormal.recycle();
            mNormal = null;
        }
        super.onDestroy();
    }
}