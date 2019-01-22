package com.kkl.graffiti.edit.drawType;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import com.kkl.graffiti.edit.bean.CurDrawBean;

/**
 * @author cst1718 on 2018/11/27 18:51
 * @explain 橡皮擦, 与自由画笔逻辑一致
 */
public class DrawEraser extends BaseDrawType {

    private Paint mEraserPaint;
    private Path  mPath;
    private float mLastX;
    private float mLastY;

    public DrawEraser(float x, float y, CurDrawBean info) {
        this.mPath = new Path();
        mEraserPaint = new Paint();
        mEraserPaint.setAlpha(0);
        mEraserPaint.setAntiAlias(true);
        mEraserPaint.setDither(true);
        mEraserPaint.setStyle(Paint.Style.STROKE);
        mEraserPaint.setStrokeJoin(Paint.Join.ROUND);
        mEraserPaint.setStrokeCap(Paint.Cap.ROUND);// 起点和终点为半圆
        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mEraserPaint.setStrokeWidth(info.size);//主要是大小

        mPath.moveTo(x, y);
        mLastX = x;
        mLastY = y;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawPath(mPath, mEraserPaint);
    }

    @Override
    public void move(float mx, float my) {
        float x = mLastX;
        float y = mLastY;
        mPath.quadTo(
                x,
                y,
                (mx + x) / 2,
                (my + y) / 2); // 使用贝塞尔曲线 让涂鸦轨迹更圆滑
        //            path.lineTo(mx, my);
        mLastX = mx;
        mLastY = my;
    }
}
