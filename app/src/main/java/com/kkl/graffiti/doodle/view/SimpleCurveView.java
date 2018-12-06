package com.kkl.graffiti.doodle.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author cst1718 on 2018/12/5 15:25
 * @explain 一条曲线
 */
public class SimpleCurveView extends View {

    private Paint mPaint;
    private Paint mPaintBack;
    private Path  mPath;

    public SimpleCurveView(Context context) {
        super(context);
        init();
    }

    public SimpleCurveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SimpleCurveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 设置画笔
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setAntiAlias(true);//锯齿
        mPaint.setDither(true);//抖动
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);// 转弯连接处圆???
        mPaint.setStrokeCap(Paint.Cap.ROUND);// 起点和终点为半圆

        mPaintBack = new Paint();
        mPaintBack.setColor(Color.BLACK);
        mPaintBack.setAntiAlias(true);//锯齿
        mPaintBack.setDither(true);//抖动
        mPaintBack.setStyle(Paint.Style.STROKE);
        mPaintBack.setStrokeJoin(Paint.Join.ROUND);// 转弯连接处圆???
        mPaintBack.setStrokeCap(Paint.Cap.ROUND);// 起点和终点为半圆
        mPaintBack.setColor(0Xffffd943);
        mPaintBack.setStrokeWidth(110);

        mPath = new Path();
    }

    /** 画笔大小 */
    public void setLineWidth(int width) {
        mPaint.setStrokeWidth(width);
        invalidate();
    }

    /** 透明度，0透明，255不透明 */
    public void setLineAlpha(int alpha) {
        mPaint.setAlpha(alpha);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPath.moveTo(50, getHeight() / 2);
        mPath.cubicTo(getWidth() / 3, -50, getWidth() - getWidth() / 3, getHeight() + 50, getWidth() - 50, getHeight() / 2);
        canvas.drawPath(mPath, mPaintBack);
        canvas.drawPath(mPath, mPaint);
    }
}
