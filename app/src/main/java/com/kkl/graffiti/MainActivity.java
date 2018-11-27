package com.kkl.graffiti;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kkl.graffiti.doodle.drawType.BaseDrawType;
import com.kkl.graffiti.doodle.view.SimpleDoodleView;

public class MainActivity extends Activity implements View.OnClickListener, View.OnLongClickListener {

    private SimpleDoodleView mSimpleDoodleView;
    private ImageView        mIvEraser;
    private ImageView        mIvPain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewGroup simpleContainer = findViewById(R.id.fl_content);
        mSimpleDoodleView = new SimpleDoodleView(this);
        simpleContainer.addView(mSimpleDoodleView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        findViewById(R.id.btn_back).setOnClickListener(this);
        mIvPain = findViewById(R.id.btn_curve);
        mIvEraser = findViewById(R.id.btn_eraser);
        mIvEraser.setOnClickListener(this);
        mIvPain.setOnClickListener(this);
        findViewById(R.id.btn_color).setOnClickListener(this);
        findViewById(R.id.btn_eraser).setOnLongClickListener(this);
        findViewById(R.id.btn_size).setOnLongClickListener(this);

        mIvPain.setSelected(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:// 回退一步
                mSimpleDoodleView.back(1);
                break;
            case R.id.btn_curve:// 画画
                mSimpleDoodleView.setDrawType(BaseDrawType.Type.Curve);
                mIvEraser.setSelected(false);
                mIvPain.setSelected(true);
                break;
            case R.id.btn_eraser:// 橡皮擦
                mSimpleDoodleView.setDrawType(BaseDrawType.Type.Eraser);
                mIvEraser.setSelected(true);
                mIvPain.setSelected(false);
                break;
            case R.id.btn_color:// 颜色选择
                showColorSelectDialog();
                break;
            case R.id.btn_size:// 字号大小
                showSizeSelectDialog();
                break;
        }
    }

    private void showColorSelectDialog() {
        
    }

    private void showSizeSelectDialog() {

    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }
}
