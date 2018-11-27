package com.kkl.graffiti.doodle.drawType;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * @author cst1718 on 2018/11/27 16:31
 * @explain 基础类型, 线条, 直线, 矩形, 圆形, 椭圆等自行增加,同时增加type
 */
public abstract class BaseDrawType {

    public enum Type{
        Curve,
        Line,
        Eraser
    }

    public abstract void draw(Canvas canvas, Paint paint);

    public abstract void move(float mx, float my);
}
