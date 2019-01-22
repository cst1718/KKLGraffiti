package com.kkl.graffiti.edit.drawType;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import com.kkl.graffiti.edit.bean.CurDrawBean;

/**
 * @author cst1718 on 2018/11/27 16:33
 * @explain 曲线, 自由画
 */
public class DrawCurve extends BaseDrawType {

    private float mLastX;
    private float mLastY;

    public DrawCurve(float x, float y, CurDrawBean info) {
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);//锯齿
        mPaint.setDither(true);//抖动
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);// 转弯连接处圆???
        mPaint.setStrokeCap(Paint.Cap.ROUND);// 起点和终点为半圆
        mPaint.setXfermode(info.mode);// 必须设置mode

        // 每次的大小颜色和透明度,注意颜色和透明度设置顺序
        mPaint.setColor(info.color);
        mPaint.setAlpha(info.alpha);
        mPaint.setStrokeWidth(info.size);

        mPath.moveTo(x, y);
        mLastX = x;
        mLastY = y;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    public void move(float mx, float my) {
        mPath.quadTo(
                mLastX,
                mLastY,
                (mx + mLastX) / 2,
                (my + mLastY) / 2); // 使用贝塞尔曲线 让涂鸦轨迹更圆滑
        //            path.lineTo(mx, my);
        mLastX = mx;
        mLastY = my;
    }
}
