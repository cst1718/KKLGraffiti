package com.kkl.graffiti.doodle.drawType;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * @author cst1718 on 2018/11/27 18:51
 * @explain 橡皮擦, 与自由画笔逻辑一致
 */
public class DrawEraser extends BaseDrawType {

    private Path  mPath;
    private  float mLastX;
    private  float mLastY;

    public DrawEraser(float x, float y) {
        this.mPath = new Path();
        mPath.moveTo(x, y);
        //        path.lineTo(x, y);
        mLastX = x;
        mLastY = y;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
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
