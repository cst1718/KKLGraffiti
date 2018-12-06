package com.kkl.graffiti.doodle.drawType;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * @author cst1718 on 2018/11/27 16:31
 * @explain 基础类型, 线条, 直线, 矩形, 圆形, 椭圆等自行增加,同时增加type
 * 轨迹和画笔都必须新建,不然会出现回退的时候都变成了最后一个画笔设置
 */
public abstract class BaseDrawType {
    public Path  mPath;
    public Paint mPaint;
    public enum Type {
        Curve,
        Line,
        Eraser
    }

    public abstract void draw(Canvas canvas);

    public abstract void move(float mx, float my);
}
