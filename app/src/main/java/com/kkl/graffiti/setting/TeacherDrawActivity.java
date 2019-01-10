package com.kkl.graffiti.setting;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kkl.graffiti.BaseActivity;
import com.kkl.graffiti.R;
import com.kkl.graffiti.common.util.CloseableUtils;

import java.io.InputStream;

/**
 * @author cst1718 on 2018/12/18 17:52
 * @explain 教程图片
 */
public class TeacherDrawActivity extends BaseActivity implements View.OnClickListener {
    private static final String PATH  = "path";
    private static final String TITLE = "title";

    private TextView  mTitle;
    private ImageView mBack;
    private Bitmap    mBitmap;

    public static Intent getActivityIntent(Activity activity, String path, String title) {
        Intent intent = new Intent();
        intent.setClass(activity, TeacherDrawActivity.class);
        intent.putExtra(PATH, path);
        intent.putExtra(TITLE, title);
        return intent;
    }

    @Override
    public int getContentViewLayoutId() {
        return R.layout.activity_teach_draw;
    }

    @Override
    public void initViewsAndListeners() {
        if (!initIntent()) {
            finish();
        }
    }

    private boolean initIntent() {
        if (getIntent() == null) {
            return false;
        }
        String path = getIntent().getStringExtra(PATH);
        InputStream open = null;
        try {
            open = getAssets().open(path);
            mBitmap = BitmapFactory.decodeStream(open);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            CloseableUtils.close(open);
        }
        if (mBitmap == null) {
            return false;
        }
        mTitle = findViewById(R.id.tv_teach_draw_title);
        mBack = findViewById(R.id.iv_teach_draw_back);
        mTitle.setText(getIntent().getStringExtra(TITLE));
        mBack.setOnClickListener(this);
        ImageView icon = findViewById(R.id.iv_teach_draw_pic);
        icon.setImageBitmap(mBitmap);
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_teach_draw_back) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
        super.onDestroy();
    }
}
