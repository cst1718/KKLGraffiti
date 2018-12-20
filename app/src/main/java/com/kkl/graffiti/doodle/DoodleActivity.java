package com.kkl.graffiti.doodle;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kkl.graffiti.AppConfig;
import com.kkl.graffiti.BaseActivity;
import com.kkl.graffiti.BaseAlertDialogFragment;
import com.kkl.graffiti.R;
import com.kkl.graffiti.common.interfaces.IDialogsCallBack;
import com.kkl.graffiti.common.util.CloseableUtils;
import com.kkl.graffiti.common.util.FileUtils;
import com.kkl.graffiti.common.util.ResourceUtils;
import com.kkl.graffiti.doodle.detection.GpuImage;
import com.kkl.graffiti.doodle.detection.IDetectionResult;
import com.kkl.graffiti.doodle.dialog.ColorPickerDialog;
import com.kkl.graffiti.doodle.dialog.NormalAlertDialog;
import com.kkl.graffiti.doodle.dialog.SelectSizeAndAlphaDialog;
import com.kkl.graffiti.doodle.drawType.BaseDrawType;
import com.kkl.graffiti.doodle.view.MyCropView;
import com.kkl.graffiti.doodle.view.SimpleDoodleView;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author cst1718 on 2018/11/28 18:03
 * @explain 涂鸦板画, 包括图片裁剪和涂鸦
 */
public class DoodleActivity extends BaseActivity
        implements View.OnClickListener, View.OnTouchListener, IDetectionResult {

    private View mLayoutEdit;
    private View mLayoutDoodle;

    public enum Type {
        NORMAL, NOEDGE, NEW
    }

    private static final String PATH = "path";
    private static final String TYPE = "type";

    private DoodleActivity mActivity = DoodleActivity.this;

    private SimpleDoodleView mDoodleView;
    private MyCropView       mCropView;
    private ImageView        mIvBack;
    private TextView         mTvSure;
    private ImageView        mIvLast;
    private ImageView        mIvNormal;
    private ImageView        mIvEraser;
    private ImageView        mIvPain;
    private ImageView        mIvColor;
    private ImageView        mIvSize;
    private String           mSelectPath;//相机或者相册得到的图片的绝对路径

    private boolean        mFinish;// 是否完成可以保存
    private ProgressDialog mProgressDlg;
    private Bitmap         mNormal;
    private Type           mType;

    /** 从我的作品中进来的不做边缘检测 */
    public static Intent getActivityIntent(Activity activity, String path, Type type) {
        Intent intent = new Intent();
        intent.setClass(activity, DoodleActivity.class);
        intent.putExtra(PATH, path);
        intent.putExtra(TYPE, type.ordinal());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_doodle);
        if (!initIntent()) {
            Toast.makeText(this, "图片有误", Toast.LENGTH_SHORT).show();
            finish();
        }
        initView();
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

        // 图片裁剪
        mCropView = findViewById(R.id.cv_doodle_crop);

        // 涂鸦
        mDoodleView = findViewById(R.id.dv_doodle_doodle);
        mLayoutDoodle = findViewById(R.id.rl_layout);

        // 标题栏
        mIvBack = findViewById(R.id.iv_doodle_back);
        mTvSure = findViewById(R.id.tv_doodle_sure);
        mIvBack.setOnClickListener(this);
        mTvSure.setOnClickListener(this);

        // 画栏
        mLayoutEdit = findViewById(R.id.layout_doodle_edit);
        mIvLast = findViewById(R.id.iv_doodle_last);
        mIvNormal = findViewById(R.id.iv_doodle_normal);
        mIvEraser = findViewById(R.id.iv_doodle_eraser);
        mIvPain = findViewById(R.id.iv_doodle_pain);
        mIvColor = findViewById(R.id.iv_doodle_color);
        mIvSize = findViewById(R.id.iv_doodle_size);
        mIvPain.setSelected(true);
        mLayoutEdit.setVisibility(View.INVISIBLE);

        mIvLast.setOnClickListener(this);
        mIvNormal.setOnTouchListener(this);
        mIvEraser.setOnClickListener(this);
        mIvPain.setOnClickListener(this);
        mIvColor.setOnClickListener(this);
        mIvSize.setOnClickListener(this);


        // 初始化视图完成之后再设置,不然设置图片并没有重新绘制测量高度不对
        mCropView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mCropView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (mType == Type.NEW) {// 新建画布直接进涂鸦板
                    photoResult(null);
                } else {
                    mCropView.setBmpPath(mSelectPath, mActivity);
                }
            }
        });
    }

    /** 是否转换 */
    private void showChangeBitmapDialog() {
        NormalAlertDialog dialog = NormalAlertDialog.getNormalAlertDialog(ResourceUtils.getResourcesString(R.string.dialog_detection_doodle_msg));
        dialog.setOnButtonClickCallback(new IDialogsCallBack() {
            @Override
            public void DialogsCallBack(ButtonType buttonType, BaseAlertDialogFragment thisDialogs) {
                thisDialogs.dismiss();
                switch (buttonType) {
                    case rightButton:
                        detectionBitmap();
                        break;
                    case leftButton:
                        photoResult(null);
                        break;
                }
            }
        });
        dialog.show(getSupportFragmentManager(), "showChangeBitmapDialog");
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
                        saveDoodle();
                        break;
                    case leftButton:
                        finish();
                        break;
                }
            }
        });
        dialog.show(getSupportFragmentManager(), "showSavePhotoDialog");
    }

    private void detectionBitmap() {
        mProgressDlg.setMessage("正在转换");
        mProgressDlg.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 使用GPUImage处理图像
                mNormal = mCropView.getCroppedImage();
                GpuImage gpuImage = new GpuImage(mActivity, mActivity);
                gpuImage.detection(mNormal);
                gpuImage.createBitmap(0.75f);
            }
        }).start();
    }

    private void saveDoodle() {
        mProgressDlg.setMessage("正在保存");
        mProgressDlg.show();
        String appDir = AppConfig.getSaveDirPath();
        FileUtils.createDir(appDir);
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            mDoodleView.getEndMap().compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseableUtils.close(fos);
        }
        mProgressDlg.dismiss();
        MediaScannerConnection.scanFile(this, new String[]{file.getAbsolutePath()}, null, null);
        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
        goBackMainActivity();

        // 魅族不回调
        /*new SingleMediaScanner(mActivity, file, new SingleMediaScanner.ScanListener() {
            @Override
            public void onScanFinish() {
                mProgressDlg.dismiss();
                goBackMainActivity();
            }
        });*/
    }

    public void goBackMainActivity() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void photoResult(final Bitmap bitmap) {
        if (mNormal == null) {
            if (mType == Type.NEW) {// 新建画布没有背景图,创建一个纯色的背景图
                mNormal = Bitmap.createBitmap(mCropView.getWidth(), mCropView.getHeight(), Bitmap.Config.ARGB_8888);
                mNormal.eraseColor(Color.WHITE);
                mFinish = true;
            } else {
                mNormal = mCropView.getCroppedImage();// 先初始化转换默认图片
            }
        }
        mDoodleView.post(new Runnable() {
            @Override
            public void run() {
                mProgressDlg.cancel();
                mCropView.setVisibility(View.GONE);
                mDoodleView.setCanvasBackground(mNormal, bitmap);
                mLayoutDoodle.setVisibility(View.VISIBLE);
                mLayoutEdit.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!mFinish) {
            finish();
        } else {// 保存涂鸦画弹窗
            showSavePhotoDialog();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_doodle_back:// 返回
                if (!mFinish) {
                    finish();
                } else {// 保存涂鸦画弹窗
                    showSavePhotoDialog();
                }
                break;
            case R.id.tv_doodle_sure:// 确认
                if (mFinish) {//保存涂鸦画弹窗
                    showSavePhotoDialog();
                } else {
                    mFinish = true;
                    if (mType == Type.NORMAL) {
                        showChangeBitmapDialog();
                    } else {// 新建,不边缘转换
                        photoResult(null);
                    }
                }

                break;
            case R.id.iv_doodle_last:// 上一步
                mDoodleView.back(1);
                break;
            case R.id.iv_doodle_eraser:// 橡皮擦
                mDoodleView.setDrawType(BaseDrawType.Type.Eraser);
                mIvEraser.setSelected(true);
                mIvPain.setSelected(false);
                break;
            case R.id.iv_doodle_pain:// 画笔
                mDoodleView.setDrawType(BaseDrawType.Type.Curve);
                mIvEraser.setSelected(false);
                mIvPain.setSelected(true);
                break;
            case R.id.iv_doodle_size:// 字号大小
                SelectSizeAndAlphaDialog dialog = SelectSizeAndAlphaDialog.getSizeSelectDialog(mDoodleView.getPaintSize(), mDoodleView.getPaintAlpha(), mIvEraser.isSelected());
                dialog.setOnButtonClickCallback(new SelectSizeAndAlphaDialog.onProgressResult() {
                    @Override
                    public void onResult(int size, int alpha) {
                        mDoodleView.setSize(size);
                        mDoodleView.setLineAlpha(alpha);
                    }
                });
                dialog.show(getSupportFragmentManager(), "111");
                break;
            case R.id.iv_doodle_color:// 颜色
                Toast.makeText(this, "颜色选择器", Toast.LENGTH_SHORT).show();
                ColorPickerDialog dialog1 = ColorPickerDialog.getColorPickerDialog(mDoodleView.getPainColor());
                dialog1.setOnButtonClickCallback(new ColorPickerDialog.onColorProgressResult() {
                    @Override
                    public void onColorResult(int color) {
                        mDoodleView.setColor(color);
                    }
                });
                dialog1.show(getSupportFragmentManager(), "ColorPickerDialog");
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.iv_doodle_normal) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mIvNormal.setPressed(true);
                    mDoodleView.showNormalBitmap(true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_OUTSIDE:
                    mIvNormal.setPressed(false);
                    mDoodleView.showNormalBitmap(false);
                    break;
            }
        }
        return true;
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
