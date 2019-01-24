package com.kkl.graffiti.setting;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kkl.graffiti.BaseActivity;
import com.kkl.graffiti.R;
import com.kkl.graffiti.common.util.ResourceUtils;
import com.kkl.graffiti.view.AboutView;

/**
 * @author cst1718 on 2018/12/18 17:19
 * @explain 教程列表图
 */
public class TutorialsActivity extends BaseActivity implements View.OnClickListener {

    private AboutView mMouse;
    private AboutView mCow;
    private AboutView mPanda;
    private AboutView mPeiqi;
    private TextView  mTitle;
    private ImageView mBack;

    @Override
    public int getContentViewLayoutId() {
        return R.layout.activity_tutorals;
    }

    @Override
    public void initViewsAndListeners() {
        initView();
    }

    @Override
    protected boolean immersionStatusBar() {
        return true;
    }

    private void initView() {
        mMouse = findViewById(R.id.av_teach_mouse);
        mCow = findViewById(R.id.av_teach_cow);
        mPanda = findViewById(R.id.av_teach_panda);
        mPeiqi = findViewById(R.id.av_teach_peiqi);
        mTitle = findViewById(R.id.tv_teach_title);
        mBack = findViewById(R.id.iv_teach_back);
        mTitle.setText(ResourceUtils.getResourcesString(R.string.teach_title));
        mMouse.setOnClickListener(this);
        mCow.setOnClickListener(this);
        mPanda.setOnClickListener(this);
        mPeiqi.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mMouse.showRightDraw(R.drawable.mouse, 0);
        mCow.showRightDraw(R.drawable.cow, 0);
        mPanda.showRightDraw(R.drawable.panda, 0);
        mPeiqi.showRightDraw(R.drawable.peiqi, 0);
    }

    private void go2TeachDrawActivity(String path, String title) {
        Intent activityIntent = TeacherDrawActivity.getActivityIntent(this, path, title);
        startActivity(activityIntent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.av_teach_mouse:
                go2TeachDrawActivity("mouse.jpg", ResourceUtils.getResourcesString(R.string.teach_item_mouse));
                break;
            case R.id.av_teach_cow:
                go2TeachDrawActivity("cow.jpg", ResourceUtils.getResourcesString(R.string.teach_item_cow));
                break;
            case R.id.av_teach_panda:
                go2TeachDrawActivity("panda.jpg", ResourceUtils.getResourcesString(R.string.teach_item_panda));
                break;
            case R.id.av_teach_peiqi:
                go2TeachDrawActivity("peiqi.jpg", ResourceUtils.getResourcesString(R.string.teach_item_peiqi));
                break;
            case R.id.iv_teach_back:
                finish();
                break;
        }
    }
}
