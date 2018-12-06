package com.kkl.graffiti.doodle.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.kkl.graffiti.doodle.bean.CurDrawBean;
import com.kkl.graffiti.doodle.drawType.BaseDrawType;
import com.kkl.graffiti.doodle.drawType.DrawCurve;
import com.kkl.graffiti.doodle.drawType.DrawEraser;
import com.kkl.graffiti.doodle.touchUtils.TouchGestureDetector;

import java.util.ArrayList;

/**
 * author  cst1718 on 2018/11/27 18:38
 * <p>
 * explain 涂鸦控件,https://github.com/1993hzw/Androids 1.2.3版本
 * 控制起始点不能自定义控制宽高,所以要让背景图居中需要再套一个ViewGroup,需要重写onMeasure
 * 2018-12-01
 * 画的越多后期越卡,而且出现问题,橡皮擦会把图像给清空,那么背景图也被擦除,只能在父布局中设置图片,当前view不画传进来的图片
 * 改用双缓冲绘图,创建新缓冲画布和缓冲bitmap,画轨迹,再用当前画布画缓冲bitmap,相当于surfaceView
 * 优点1.缓冲画布是在内存中画画,当前画布是在屏幕上画,效率低
 * 优点2.当前画布每ondraw,是都重新创建一个空白bitmap来画,必须把所有轨迹记录画出,缓冲画布是由始至终都画在一个缓冲画布上的后续轨迹单独画上
 */
public class SimpleDoodleView extends View {
    private final static String TAG = "SimpleDoodleView";

    private ArrayList<BaseDrawType> mDrawList = new ArrayList<>();// 保存涂鸦轨迹的集合
    private BaseDrawType.Type       mCurType  = BaseDrawType.Type.Curve;
    private Paint                   mPaint    = new Paint();// 普通画笔
    private BaseDrawType            mCurDraw;
    private TouchGestureDetector    mTouchGestureDetector; // 触摸手势监听
    private Bitmap                  mNormalBitmap;// 原图,即截图
    private Bitmap                  mRobertBitmap;// Robert算法边缘化后的图,或者是null不边缘化
    private Bitmap                  mBufferBitmap;// 缓冲bitmap
    private RectF                   mBitMapRectF;
    private Canvas                  mBufferCanvas;// 缓冲画布
    private boolean                 mIsShowNormal;// 是否只显示原画
    private CurDrawBean             mCurPenBean;// 当前画笔信息
    private CurDrawBean             mCurEraserBean;// 当前橡皮擦信息

    /**
     * 设置了透明度画笔,会出现重影,所以另外开一张缓冲图专门绘制每一次的透明度,并且要设置模式为SRC
     * SRC模式是后面的绘制会把前面的绘制完全覆盖,这样子效果就是移动画的时候显示正常不会重影
     * 最后需要在移动结束之后,把画笔模式切换成SRC_OVER在保存,可以回退,因为回退不会一直ondraw而是直接一笔所以不会重影
     */
    private boolean mIsAlpha;// 是否设置了透明度
    private Bitmap  mBufferAlphaBitmap;// 缓冲bitmap,记录透明度的
    private Canvas  mBufferAlphaCanvas;// 缓冲画布,记录透明度

    public SimpleDoodleView(Context context) {
        super(context);
        init();
    }

    public SimpleDoodleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SimpleDoodleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        // 关闭硬件加速
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        setDrawingCacheEnabled(true);

        // 设置画图画笔
        mPaint.setAntiAlias(true);//锯齿
        mPaint.setDither(true);//抖动

        // 初始化当前画笔
        mCurPenBean = new CurDrawBean();
        mCurPenBean.alpha = 255;
        mCurPenBean.color = Color.BLACK;
        mCurPenBean.size = 25;
        mCurEraserBean = new CurDrawBean();
        mCurEraserBean.alpha = 0;
        mCurEraserBean.size = 70;

        // 虽然外面是精确计算,但是有可能出现图片比控件大,那么onMeasure即使设置了图片的大小也只有控件大小,还是用最终尺寸为矩阵吧
        mBitMapRectF = new RectF();
        // 由手势识别器处理手势
        mTouchGestureDetector = new TouchGestureDetector(getContext(), new TouchGestureDetector.OnTouchGestureListener() {

            @Override
            public void onScrollBegin(MotionEvent e) { // 滑动开始
                if (mBufferBitmap == null) {
                    initBuffer();
                }
                switch (mCurType) {
                    case Curve:
                        if (mIsAlpha) {
                            mCurPenBean.mode = new PorterDuffXfermode(PorterDuff.Mode.SRC);
                            if (mBufferAlphaBitmap == null) {
                                initAlphaBuffer();
                            }
                        } else {
                            mCurPenBean.mode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
                        }
                        mCurDraw = new DrawCurve(e.getX(), e.getY(), mCurPenBean);
                        break;
                    case Eraser:
                        mCurDraw = new DrawEraser(e.getX(), e.getY(), mCurEraserBean);
                        break;
                }
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { // 滑动中
                mCurDraw.move(e2.getX(), e2.getY());
                if (mIsAlpha && mCurType == BaseDrawType.Type.Curve) {// 透明度的画在另外一张图缓冲画上
                    mCurDraw.draw(mBufferAlphaCanvas);
                } else {
                    mCurDraw.draw(mBufferCanvas);// 缓冲画轨迹
                }
                invalidate();
                return true;
            }

            @Override
            public void onScrollEnd(MotionEvent e) { // 滑动结束
                // 此次操作轨迹加入集合
                mDrawList.add(mCurDraw);

                if (mIsAlpha && mCurType == BaseDrawType.Type.Curve) {// 透明度缓冲画布清空,每次只画一次,并且要把轨迹中的画笔模式切换回来,不然回退到这一轨迹会把效果不一致
                    mCurDraw.mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
                    mBufferAlphaBitmap.eraseColor(Color.TRANSPARENT);
                    // 再在缓冲画上把此次轨迹完整画出
                    mCurDraw.draw(mBufferCanvas);
                    // 置空,onDraw中判断空的话不画缓冲透明
                    mCurDraw = null;
                    invalidate();
                } else {
                    mCurDraw = null;
                }
            }
        });
    }

    private void initBuffer() {
        mBufferBitmap = Bitmap.createBitmap((int) mBitMapRectF.width(), (int) mBitMapRectF.height(), Bitmap.Config.ARGB_8888);
        mBufferCanvas = new Canvas(mBufferBitmap);
    }

    /** 透明度单独记录,因为移动的时候ondraw会重影 */
    private void initAlphaBuffer() {
        mBufferAlphaBitmap = Bitmap.createBitmap((int) mBitMapRectF.width(), (int) mBitMapRectF.height(), Bitmap.Config.ARGB_8888);
        mBufferAlphaCanvas = new Canvas(mBufferAlphaBitmap);
    }

    /**
     * 设置画笔的颜色
     *
     * @param color 颜色
     */
    public void setColor(int color) {
        mCurPenBean.color = color;
    }

    /** 透明度，0透明，255不透明 */
    public void setLineAlpha(int alpha) {
        if (mCurType == BaseDrawType.Type.Eraser) {
            return;
        }
        mCurPenBean.alpha = alpha;
        mIsAlpha = alpha != 255;
    }

    /**
     * 设置画笔的粗细
     *
     * @param size 画笔的粗细
     */
    public void setSize(int size) {
        if (mCurType == BaseDrawType.Type.Eraser) {
            mCurEraserBean.size = size;
        } else {
            mCurPenBean.size = size;
        }
    }

    public int getPaintSize() {
        if (mCurType == BaseDrawType.Type.Eraser) {
            return (int) mCurEraserBean.size;
        }
        return (int) mCurPenBean.size;
    }

    public int getPaintAlpha() {
        return mCurPenBean.alpha;
    }

    /**
     * 设置画笔类型
     *
     * @param type
     */
    public void setDrawType(BaseDrawType.Type type) {
        mCurType = type;
    }

    public void setCanvasBackground(Bitmap normalBitmap, Bitmap robertBitmap) {
        mNormalBitmap = normalBitmap;
        mRobertBitmap = robertBitmap;
    }

    /**
     * 回退,此时是缓冲画置空,按照轨迹重画
     *
     * @param index 一般都是传1,表示清除上一操作
     */
    public void back(int index) {
        if (mBufferBitmap == null) {
            return;
        }
        if (mDrawList.size() < index) {
            mDrawList.clear();
        } else {
            mDrawList.remove(mDrawList.size() - index);
        }
        // bitmap重置成透明
        mBufferBitmap.eraseColor(Color.TRANSPARENT);
        for (BaseDrawType drawType : mDrawList) {
            drawType.draw(mBufferCanvas);
        }

        invalidate();
    }

    /** 是否显只示原画,不画缓冲画即可 */
    public void showNormalBitmap(boolean normal) {
        mIsShowNormal = normal;
        invalidate();
    }

    /**
     * 清空画板,只需要把缓冲画置空即可
     */
    public void reset() {
        mBufferBitmap = null;
        mBufferAlphaBitmap = null;
        invalidate();
    }

    /** 获取最后的画, 缓冲画清空并画上底图和轨迹,返回 */
    public Bitmap getEndMap() {

        /*// 还未画图,把传进来的直接返回
        if (mBufferCanvas == null) {
            return mRobertBitmap == null ? mNormalBitmap : mRobertBitmap;
        }
        mBufferBitmap.eraseColor(Color.TRANSPARENT);
        mBufferCanvas.drawBitmap(mRobertBitmap == null ? mNormalBitmap : mRobertBitmap, null, mBitMapRectF, mPaint);
        for (BaseDrawType drawType : mDrawList) {
            if (drawType instanceof DrawEraser) {
                drawType.draw(mBufferCanvas, mEraserPaint);
            } else {
                drawType.draw(mBufferCanvas, mPaint);
            }
        }*/
        Bitmap drawingCache = getDrawingCache();
        Bitmap result = Bitmap.createBitmap(drawingCache);
        destroyDrawingCache();
        mNormalBitmap.recycle();
        if (mRobertBitmap != null) {
            mRobertBitmap.recycle();
        }
        if (mBufferBitmap != null) {
            mBufferBitmap.recycle();
        }
        if (mBufferAlphaBitmap != null) {
            mBufferAlphaBitmap.recycle();
        }
        mNormalBitmap = null;
        mRobertBitmap = null;
        mBufferBitmap = null;
        mBufferAlphaBitmap = null;
        return result;
    }

    // 继承View的话,如果有父控件,那么父控件重绘的时候会调用子控件,此时这个控件设置的无论是wrap还是match,结果显示出来都是撑满
    // 所以需要重绘时候判断,重写warp时候的实际大小,此控件实际大小就是图片的大小
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mNormalBitmap != null) {
            int width = measureDimension(mNormalBitmap.getWidth(), widthMeasureSpec);
            int height = measureDimension(mNormalBitmap.getHeight(), heightMeasureSpec);
            setMeasuredDimension(width, height);
            if (mBitMapRectF != null) {
                mBitMapRectF.left = 0;
                mBitMapRectF.top = 0;
                mBitMapRectF.right = width;
                mBitMapRectF.bottom = height;
            }
        }
    }

    public int measureDimension(int defaultSize, int measureSpec) {
        int result;

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {//全屏
            result = specSize;
        } else {
            result = defaultSize;   //UNSPECIFIED
            if (specMode == MeasureSpec.AT_MOST) {//wrap
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 只显示原效果画
        if (mIsShowNormal && mNormalBitmap != null) {
            canvas.drawBitmap(mNormalBitmap, null, mBitMapRectF, mPaint);
            return;
        }

        // 画底图
        if (mNormalBitmap != null) {
            // 如果是边缘化了,那么显示边缘画
            canvas.drawBitmap(mRobertBitmap == null ? mNormalBitmap : mRobertBitmap, null, mBitMapRectF, mPaint);
        }

        // 画轨迹
        if (mBufferBitmap != null) {
            // 这里用矩阵画的话重叠了?重编译...
            canvas.drawBitmap(mBufferBitmap, null, mBitMapRectF, mPaint);
        }
        // 判断是否还有缓冲透明画,并且是在移动画中,有的话画上去
        if (mIsAlpha && mCurDraw != null && mBufferAlphaBitmap != null) {
            canvas.drawBitmap(mBufferAlphaBitmap, null, mBitMapRectF, mPaint);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean consumed = mTouchGestureDetector.onTouchEvent(event); // 由手势识别器处理手势
        if (!consumed) {
            return super.dispatchTouchEvent(event);
        }
        return true;
    }
   /* public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        final float x = event.getX();
        final float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                if (mPath == null) {
                    mPath = new Path();
                }
                mPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                //这里终点设为两点的中心点的目的在于使绘制的曲线更平滑，如果终点直接设置为x,y，效果和lineto是一样的,实际是折线效果
                mPath.quadTo(mLastX, mLastY, (x + mLastX) / 2, (y + mLastY) / 2);
                if (mBufferBitmap == null) {
                    initBuffer();
                }
                mBufferCanvas.drawPath(mPath, mPaint);
                invalidate();
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                mPath.reset();
                break;
        }
        return true;
    }*/
}
