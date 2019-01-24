package com.kkl.graffiti.home.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.squareup.picasso.Transformation;

/**
 * @author cst1718 on 2019/1/22 15:34
 * @explain
 */
public class CircleTransform implements Transformation {

    private int   defaultSize;
    private float radius;

    public CircleTransform(float radius, int defaultSize) {
        this.radius = radius;
        this.defaultSize = defaultSize;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        //获取最小边长
        int size = Math.min(source.getWidth(), source.getHeight());
        //获取正方形图片的左上角坐标
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;
        //创建一个正方形区域的Bitmap
        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }
        Bitmap.Config config = source.getConfig() != null ? source.getConfig() : Bitmap.Config.ARGB_8888;
        //创建一张可以操作的正方形图片的位图
        Bitmap bitmap = Bitmap.createBitmap(size, size, config);
        //创建一个画布Canvas
        Canvas canvas = new Canvas(bitmap);
        //创建画笔
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);
        float actualRadius = radius;
        if (defaultSize > 0) {
            actualRadius = radius * size / defaultSize;
        }
        canvas.drawRoundRect(new RectF(0, 0, size, size), actualRadius, actualRadius, paint);
        squaredBitmap.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "circle";
    }
}
