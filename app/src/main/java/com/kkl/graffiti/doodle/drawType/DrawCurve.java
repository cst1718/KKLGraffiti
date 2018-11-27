package com.kkl.graffiti.doodle.drawType;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * @author cst1718 on 2018/11/27 16:33
 * @explain 曲线, 自由画
 */
public class DrawCurve extends BaseDrawType {

    private Path  mPath;
    private float mLastX;
    private float mLastY;

    public DrawCurve(float x, float y) {
        this.mPath = new Path();
        mPath.moveTo(x, y);
        //        path.lineTo(x, y);
        mLastX = x;
        mLastY = y;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);// 转弯连接处圆???
        paint.setStrokeCap(Paint.Cap.ROUND);// 起点和终点为半圆
        canvas.drawPath(mPath, paint);
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
