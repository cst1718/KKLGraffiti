package com.kkl.graffiti.doodle;

import com.kkl.graffiti.BaseActivity;
import com.kkl.graffiti.R;
import com.kkl.graffiti.doodle.view.SolidView;

/**
 * @author cst1718 on 2019/1/3 19:38
 * @explain 涂色
 */
public class SolidPicActivity extends BaseActivity {

    private SolidView mSolidView;

    @Override
    public int getContentViewLayoutId() {
        return R.layout.activity_solid;
    }

    @Override
    public void initViewsAndListeners() {
        mSolidView = findViewById(R.id.solid_view);
        mSolidView.setBmpPath("/storage/emulated/0/Pictures/KKLSave/1546520190364.jpg",this);
    }
}
