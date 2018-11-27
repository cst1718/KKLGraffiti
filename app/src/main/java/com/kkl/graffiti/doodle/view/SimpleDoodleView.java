package com.kkl.graffiti.doodle.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.kkl.graffiti.doodle.drawType.BaseDrawType;
import com.kkl.graffiti.doodle.drawType.DrawCurve;
import com.kkl.graffiti.doodle.drawType.DrawEraser;
import com.kkl.graffiti.doodle.touchUtils.TouchGestureDetector;

import java.util.ArrayList;

/**
 * author  cst1718 on 2018/11/27 18:38
 * <p>
 * explain 涂鸦控件
 */
public class SimpleDoodleView extends View {

    private final static String TAG = "SimpleDoodleView";

    private ArrayList<BaseDrawType> mDrawList    = new ArrayList<>();// 保存涂鸦轨迹的集合
    private BaseDrawType.Type       mCurType     = BaseDrawType.Type.Curve;
    private BaseDrawType            mCurDraw;
    private Paint                   mPaint       = new Paint();// 普通画笔
    private Paint                   mEraserPaint = new Paint();// 橡皮擦
    private TouchGestureDetector    mTouchGestureDetector; // 触摸手势监听
    private Bitmap                  mBitmap;

    public SimpleDoodleView(Context context) {
        super(context);
        // 设置画笔
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(20);
        mPaint.setAntiAlias(true);//锯齿
        mPaint.setDither(true);//抖动

        // 设置橡皮擦
        mEraserPaint.setAlpha(0);
        mEraserPaint.setAntiAlias(true);
        mEraserPaint.setDither(true);
        mEraserPaint.setStrokeWidth(20);
        mEraserPaint.setStyle(Paint.Style.STROKE);
        mEraserPaint.setStrokeJoin(Paint.Join.ROUND);
        mEraserPaint.setStrokeCap(Paint.Cap.ROUND);// 起点和终点为半圆
        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        // 由手势识别器处理手势
        mTouchGestureDetector = new TouchGestureDetector(getContext(), new TouchGestureDetector.OnTouchGestureListener() {

            @Override
            public void onScrollBegin(MotionEvent e) { // 滑动开始
                Log.d(TAG, "onScrollBegin: ");
                switch (mCurType) {
                    case Curve:
                        mCurDraw = new DrawCurve(e.getX(), e.getY());
                        break;
                    case Eraser:
                        mCurDraw = new DrawEraser(e.getX(), e.getY());
                        break;
                }
                // 此次操作轨迹加入集合
                mDrawList.add(mCurDraw);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { // 滑动中
                Log.d(TAG, "onScroll: " + e2.getX() + " " + e2.getY());
                mCurDraw.move(e2.getX(), e2.getY());
                invalidate(); // 刷新绘制
                return true;
            }

            @Override
            public void onScrollEnd(MotionEvent e) { // 滑动结束
                Log.d(TAG, "onScrollEnd: ");
                mCurDraw = null;
            }
        });
    }

    /**
     * 设置画笔的颜色
     *
     * @param color 颜色
     */
    public void setColor(String color) {
        mPaint.setColor(Color.parseColor(color));
    }

    /**
     * 设置画笔的粗细
     *
     * @param size 画笔的粗细
     */
    public void setSize(int size) {
        if (mCurType == BaseDrawType.Type.Eraser) {
            mEraserPaint.setStrokeWidth(size);
        } else {
            mPaint.setStrokeWidth(size);
        }
    }

    /**
     * 设置画笔类型
     *
     * @param type
     */
    public void setDrawType(BaseDrawType.Type type) {
        mCurType = type;
    }

    /**
     * 回退
     *
     * @param index 一般都是传1,表示清除上一操作
     */
    public void back(int index) {
        if (mDrawList.size() < index) {
            mDrawList.clear();
        } else {
            mDrawList.remove(mDrawList.size() - index);
        }
        invalidate();
    }

    /**
     * 清空画板
     */
    public void reset() {
        mDrawList.clear();
        invalidate();
    }

    /**
     * 设置背景图
     *
     * @param bitmap
     */
    public void setBackground(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean consumed = mTouchGestureDetector.onTouchEvent(event); // 由手势识别器处理手势
        if (!consumed) {
            return super.dispatchTouchEvent(event);
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }
        for (BaseDrawType draw : mDrawList) {
            if (draw instanceof DrawEraser) {//橡皮擦
                draw.draw(canvas, mEraserPaint);
            } else {
                draw.draw(canvas, mPaint);
            }
        }
    }
}
