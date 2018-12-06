package com.kkl.graffiti.doodle.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.kkl.graffiti.common.util.ImageUtils;

/**
 * @author cst1718 on 2018/11/28 17:59
 * @explain 自由裁剪, 参考https://blog.csdn.net/suxiaofang/article/details/68496758
 * 根据传入bitMap对比match_parent,换算比例之后有四种做法:采用第三种
 * 1.根据比例缩小画布,但是会导致触摸点也要根据比例来换算,麻烦
 * 2.单独只移动画布,看起来显示居中,但是涂鸦画的时候可以画出边界,因为画布还是存在的
 * 3.重新绘制View的大小,不过这样子下次复用这个view最大宽高值就不一定是match_parent了,需要销毁重新初始化
 * 4.直接按照比例设置成view的padding(如果外部设置了match_parent,那么padding无效
 * 2018/11/30
 * 其实只要定义好地图的矩阵位置就好了,这里的画布只是为了拿到截图的图片,所以画布大了也无所谓
 */
public class MyCropView extends ImageView {

    private static final String TAG = "MyCropView";

    private static final int POS_TOP_LEFT     = 0;// 左上角
    private static final int POS_TOP_RIGHT    = 1;// 右上角
    private static final int POS_BOTTOM_LEFT  = 2;// 左下角
    private static final int POS_BOTTOM_RIGHT = 3;// 右下角
    private static final int POS_TOP          = 4;// 上边线
    private static final int POS_BOTTOM       = 5;// 下边线
    private static final int POS_LEFT         = 6;// 左边线
    private static final int POS_RIGHT        = 7;// 右边线
    private static final int POS_CENTER       = 8;// 中间不缩放只移动
    private static final int MAX_SCALE        = 3;// 放大最大倍数

    private static final float BORDER_DOT_WIDTH     = 30f;//裁剪框圆点半径
    private static final float BORDER_CORNER_LENGTH = 30f;
    private static final float TOUCH_FIELD          = 30f;// 边框触摸范围大小


    //图片（第一轮经过转换后的,此bitmap宽高只是在屏幕宽高极限左右）
    private Bitmap mBmpToCrop;

    // 图片矩阵
    private RectF mBmpBound;

    // 裁剪框矩阵
    private RectF mBorderBound;

    //图片画笔
    private Paint mBmpPaint;

    // 裁剪区圆点的笔
    private Paint mDotPaint;

    // 裁剪框网格线的笔
    private Paint mGuidelinePaint;

    // 蒙层的笔
    private Paint mBgPaint;

    private PointF mLastPoint = new PointF();

    private float mBorderWidth;
    private float mBorderHeight;
    private float mCenterScale;// 实际图片和显示图片的比值
    private int   touchPos;// 当前按下点类型,移动还是缩放


    public MyCropView(Context context) {
        super(context);
        init(context);
    }

    public MyCropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyCropView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mBmpToCrop == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {// 设置完图片之后重绘控件大小
            setMeasuredDimension((int) mCurWidth, (int) mCurHeight);
        }
    }*/

    //Canvas 画布
    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        // super.onDraw(canvas);
        if (mBmpToCrop != null) {

           /* // 移动画布让其居中,那么偏移出去的画布画笔还是能画上去的
            canvas.translate((getWidth() - mCurWidth) / 2, (getHeight() - mCurHeight) / 2);

            // 按照缩放/扩大比例值缩小画布(此时触摸点坐标会有问题)
            canvas.scale(mCenterScale, mCenterScale);*/

            // 画传入的图片为底,根据矩阵画大小会按照比例缩放,根据坐标画会以当前大小画出画布或者不满画布
            canvas.drawBitmap(mBmpToCrop, null, mBmpBound, mBmpPaint);
            /*// 画一个矩形框做裁剪框,计算画笔的宽度之后的矩阵
             canvas.drawRect(mBorderBound.left, mBorderBound.top, mBorderBound.right, mBorderBound.bottom, mDotPaint);*/
            // 画裁剪框网格
            drawGuidlines(canvas);
            // 画非选中的阴影
            drawBackground(canvas);
            // 裁剪网格框圆点,不画框了,在最上层
            drawGuidDot(canvas);
        }
    }

    //识别裁剪框的伸缩位置
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        // super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setLastPosition(event);
                getParent().requestDisallowInterceptTouchEvent(true);
                // onActionDown(event.getX(), event.getY());
                touchPos = detectTouchPosition(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                onActionMove(event.getX(), event.getY());
                setLastPosition(event);
                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        return true;
    }

    public void setBmpPath(String picPath, Activity activity) {
        mBmpToCrop = ImageUtils.createBitmapFromPath(picPath, activity);
        setBmp();
    }

    // 得到裁剪框里面的图片
    public Bitmap getCroppedImage() {

        // 加上四个角的画布宽高,其实也是截图完成之后出去要显示的View的大小
        float showWidth = mBmpBound.width() + BORDER_DOT_WIDTH * 2;
        float showHeight = mBmpBound.height() + BORDER_DOT_WIDTH * 2;

        // 如果不设置matrix缩放比例,那么裁剪完的图片大小是根据实际大小的尺寸返回,如果设置了缩放比例,那么大小是根据当前显示大小
        // 比如控件显示最终为500*500,裁剪为全图,大图片返回为1000* 1000,小图片返回为100*100,如果设置缩放比例,那么返回都是500*500

        float left = mBorderBound.left - (getWidth() - mBmpBound.width()) / 2;// 左坐标
        float top = mBorderBound.top - (getHeight() - mBmpBound.height()) / 2; // 上坐标

        // 得到传进来图的截图,此时尺寸是未经修改的
        mBmpToCrop = Bitmap.createBitmap(mBmpToCrop, (int) (left / mCenterScale), (int) (top / mCenterScale),
                                         (int) (mBorderWidth / mCenterScale),// 宽度
                                         (int) (mBorderHeight / mCenterScale));// 高度

        // 放大超过4倍的,按截图的宽高4倍返回(会太模糊了),不足4倍,按比例值放大截图之后再返回,不在matrix是会按比例算全图再截,会OOM
        // 倍数自定义看具体效果
        if (mCenterScale >= MAX_SCALE) {// 4倍图刚好满控件
            return Bitmap.createScaledBitmap(mBmpToCrop,
                                             (int) (mBmpToCrop.getWidth() * MAX_SCALE),
                                             (int) (mBmpToCrop.getHeight() * MAX_SCALE),
                                             false);
        } else {
            // 判断截图比例参考横比例还是竖比例
            float scaleWidth = mBorderWidth / mBmpBound.width();
            float scaleHeight = mBorderHeight / mBmpBound.height();
            if (scaleWidth > scaleHeight) {// 横比列
                if (mBmpToCrop.getWidth() * MAX_SCALE > showWidth) {// 4倍宽之后大于控件,那么以控件宽为截图的宽
                    return Bitmap.createScaledBitmap(mBmpToCrop,
                                                     (int) (showWidth),
                                                     (int) (mBmpToCrop.getHeight() * (showWidth / mBmpToCrop.getWidth())),
                                                     false);
                } else {// 4倍宽之后小于控件,那么以4倍宽为截图的宽
                    return Bitmap.createScaledBitmap(mBmpToCrop,
                                                     (int) (mBmpToCrop.getWidth() * MAX_SCALE),
                                                     (int) (mBmpToCrop.getHeight() * MAX_SCALE),
                                                     false);
                }
            } else {
                if (mBmpToCrop.getHeight() * MAX_SCALE > showHeight) {// 4倍高之后大于控件,那么以控件高为截图的高
                    return Bitmap.createScaledBitmap(mBmpToCrop,
                                                     (int) (mBmpToCrop.getWidth() * (showHeight / mBmpToCrop.getHeight())),
                                                     (int) (showHeight),
                                                     false);

                } else {// 4倍高之后小于控件,那么以4倍高为截图的高
                    return Bitmap.createScaledBitmap(mBmpToCrop,
                                                     (int) (mBmpToCrop.getWidth() * MAX_SCALE),
                                                     (int) (mBmpToCrop.getHeight() * MAX_SCALE),
                                                     false);
                }
            }
        }
    }

    //初始化，对各类画笔的初始化
    private void init(Context context) {

        mBmpPaint = new Paint();// 传入图片的画笔
        // 以下是抗锯齿
        mBmpPaint.setAntiAlias(true);// 防止边缘的锯齿
        mBmpPaint.setFilterBitmap(true);// 对位图进行滤波处理

        mDotPaint = new Paint();// 裁剪框圆点的笔
        mDotPaint.setAntiAlias(true);// 防止边缘的锯齿
        mDotPaint.setStyle(Paint.Style.FILL);// 实心
        mDotPaint.setColor(Color.parseColor("#FFFFFF"));
        // 不要设置画笔宽度,因为实际上显示出来的效果半径会等于设置的半径+画笔宽度一半
        mDotPaint.setStrokeWidth(1f);

        mGuidelinePaint = new Paint();// 裁剪框网格的画笔
        mGuidelinePaint.setAntiAlias(true);// 防止边缘的锯齿
        mGuidelinePaint.setColor(Color.parseColor("#AAFFFFFF"));
        mGuidelinePaint.setStrokeWidth(1f);

        mBgPaint = new Paint();// 非选中蒙层画笔
        mBgPaint.setColor(Color.parseColor("#B0000000"));
        mBgPaint.setAlpha(150);

    }

    private void setBmp() {
        if (mBmpToCrop == null) {
            return;
        }
        int w = mBmpToCrop.getWidth();
        int h = mBmpToCrop.getHeight();
        // 实际控件的宽度减去四个角上显示圆点的半径,才是最终图片显示的高度
        float nw = w * 1f / (getWidth() - BORDER_DOT_WIDTH * 2);
        float nh = h * 1f / (getHeight() - BORDER_DOT_WIDTH * 2);

        float curWidth;
        float curHeight;

        if (nw > nh) {
            mCenterScale = 1 / nw;
            curWidth = getWidth() - BORDER_DOT_WIDTH * 2;
            curHeight = (int) (h * mCenterScale);
        } else {
            mCenterScale = 1 / nh;
            curWidth = (int) (w * mCenterScale);
            curHeight = getHeight() - BORDER_DOT_WIDTH * 2;
        }
        mBmpBound = new RectF();// 显示图片的矩阵,居中显示
        mBmpBound.left = (getWidth() - curWidth) / 2;
        mBmpBound.top = (getHeight() - curHeight) / 2;
        mBmpBound.right = mBmpBound.left + curWidth;
        mBmpBound.bottom = mBmpBound.top + curHeight;

        mBorderBound = new RectF();// 裁剪框的矩阵,默认与图片一样大小
        mBorderBound.left = mBmpBound.left;
        mBorderBound.top = mBmpBound.top;
        mBorderBound.right = mBmpBound.right;
        mBorderBound.bottom = mBmpBound.bottom;

        getBorderEdgeLength();

        // 因为控件是match_parent,要使画布居中通过控制padding
       /* setPadding((int) (getWidth() - mCurWidth) / 2,
                   (int) (getHeight() - mCurHeight) / 2,
                   (int) (getWidth() - mCurWidth) / 2,
                   (int) (getHeight() - mCurHeight) / 2);*/
        //        setPadding(200, 200, 200, 200);
        //        requestLayout();
        invalidate();
    }

    private void drawBackground(Canvas canvas) {

        /*-
          -------------------------------------
          |                top                |
          -------------------------------------
          |      |                    |       |<——————————mBmpBound
          |      |                    |       |
          | left |                    | right |
          |      |                    |       |
          |      |                  <─┼───────┼────mBorderBound
          -------------------------------------
          |              bottom               |
          -------------------------------------
         */

        // Draw "top", "bottom", "left", then "right" quadrants.
        // because the border line width is larger than 1f, in order to draw a complete border rect ,
        // i have to change zhe rect coordinate to draw

        //        float delta = BORDER_DOT_WIDTH / 2;
        float delta = 0;
        float left = mBorderBound.left - delta;
        float top = mBorderBound.top - delta;
        float right = mBorderBound.right + delta;
        float bottom = mBorderBound.bottom + delta;

        canvas.drawRect(mBmpBound.left, mBmpBound.top, mBmpBound.right, top, mBgPaint);
        canvas.drawRect(mBmpBound.left, bottom, mBmpBound.right, mBmpBound.bottom, mBgPaint);
        canvas.drawRect(mBmpBound.left, top, left, bottom, mBgPaint);
        canvas.drawRect(right, top, mBmpBound.right, bottom, mBgPaint);
    }

    // 画裁剪区域中间的参考线,
    private void drawGuidlines(Canvas canvas) {
        // Draw vertical guidelines.
        final float oneThirdCropWidth = mBorderBound.width() / 3;

        final float x1 = mBorderBound.left + oneThirdCropWidth;
        canvas.drawLine(x1, mBorderBound.top, x1, mBorderBound.bottom, mGuidelinePaint);
        final float x2 = mBorderBound.right - oneThirdCropWidth;
        canvas.drawLine(x2, mBorderBound.top, x2, mBorderBound.bottom, mGuidelinePaint);

        // Draw horizontal guidelines.
        final float oneThirdCropHeight = mBorderBound.height() / 3;

        final float y1 = mBorderBound.top + oneThirdCropHeight;
        canvas.drawLine(mBorderBound.left, y1, mBorderBound.right, y1, mGuidelinePaint);
        final float y2 = mBorderBound.bottom - oneThirdCropHeight;
        canvas.drawLine(mBorderBound.left, y2, mBorderBound.right, y2, mGuidelinePaint);

        // 四条边,看效果画不画
        canvas.drawLine(mBorderBound.left, mBorderBound.top, mBorderBound.left, mBorderBound.bottom, mGuidelinePaint);
        canvas.drawLine(mBorderBound.right, mBorderBound.top, mBorderBound.right, mBorderBound.bottom, mGuidelinePaint);
        canvas.drawLine(mBorderBound.left, mBorderBound.top, mBorderBound.right, mBorderBound.top, mGuidelinePaint);
        canvas.drawLine(mBorderBound.left, mBorderBound.bottom, mBorderBound.right, mBorderBound.bottom, mGuidelinePaint);
    }

    // 画裁剪框的圆点
    private void drawGuidDot(Canvas canvas) {
        canvas.drawCircle(mBorderBound.left, mBorderBound.top, BORDER_DOT_WIDTH, mDotPaint);
        canvas.drawCircle(mBorderBound.left, mBorderBound.bottom, BORDER_DOT_WIDTH, mDotPaint);
        canvas.drawCircle(mBorderBound.right, mBorderBound.top, BORDER_DOT_WIDTH, mDotPaint);
        canvas.drawCircle(mBorderBound.right, mBorderBound.bottom, BORDER_DOT_WIDTH, mDotPaint);
    }

    //移动裁剪框时定位到的位置大小
    private void onActionMove(float x, float y) {
        Log.e(TAG, String.valueOf(mBmpBound.right));
        Log.e(TAG, String.valueOf(mBorderBound.right));
        float deltaX = x - mLastPoint.x;
        float deltaY = y - mLastPoint.y;
        // 这里先不考虑裁剪框放最大的情况【这个就是原博主没有考虑到将裁剪框放置到最大】
        switch (touchPos) {
            case POS_CENTER:
                mBorderBound.left += deltaX;
                // fix border position
                if (mBorderBound.left < mBmpBound.left) {
                    mBorderBound.left = mBmpBound.left;
                }

                if ((mBorderBound.left > mBmpBound.right - mBorderWidth) || (mBorderBound.left == mBmpBound.right - mBorderWidth)) {
                    mBorderBound.left = mBmpBound.right - mBorderWidth;
                }


                mBorderBound.top += deltaY;
                if (mBorderBound.top < mBmpBound.top)
                    mBorderBound.top = mBmpBound.top;

                if (mBorderBound.top > mBmpBound.bottom - mBorderHeight)
                    mBorderBound.top = mBmpBound.bottom - mBorderHeight;

                mBorderBound.right = mBorderBound.left + mBorderWidth;
                mBorderBound.bottom = mBorderBound.top + mBorderHeight;

                break;

            case POS_TOP:
                resetTop(deltaY);
                break;
            case POS_BOTTOM:
                resetBottom(deltaY);
                break;
            case POS_LEFT:
                resetLeft(deltaX);
                break;
            case POS_RIGHT:
                resetRight(deltaX);
                break;
            case POS_TOP_LEFT:
                resetTop(deltaY);
                resetLeft(deltaX);
                break;
            case POS_TOP_RIGHT:
                resetTop(deltaY);
                resetRight(deltaX);
                break;
            case POS_BOTTOM_LEFT:
                resetBottom(deltaY);
                resetLeft(deltaX);
                break;
            case POS_BOTTOM_RIGHT:
                resetBottom(deltaY);
                resetRight(deltaX);
                break;
            default:

                break;
        }
        invalidate();
    }

    /** 判断是按到四条边上还是四个角上还是只是移动 */
    private int detectTouchPosition(float x, float y) {
        if (x > mBorderBound.left + TOUCH_FIELD && x < mBorderBound.right - TOUCH_FIELD
                && y > mBorderBound.top + TOUCH_FIELD && y < mBorderBound.bottom - TOUCH_FIELD)
            return POS_CENTER;

        if (x > mBorderBound.left + BORDER_CORNER_LENGTH && x < mBorderBound.right - BORDER_CORNER_LENGTH) {
            if (y > mBorderBound.top - TOUCH_FIELD && y < mBorderBound.top + TOUCH_FIELD)
                return POS_TOP;
            if (y > mBorderBound.bottom - TOUCH_FIELD && y < mBorderBound.bottom + TOUCH_FIELD)
                return POS_BOTTOM;
        }

        if (y > mBorderBound.top + BORDER_CORNER_LENGTH && y < mBorderBound.bottom - BORDER_CORNER_LENGTH) {
            if (x > mBorderBound.left - TOUCH_FIELD && x < mBorderBound.left + TOUCH_FIELD)
                return POS_LEFT;
            if (x > mBorderBound.right - TOUCH_FIELD && x < mBorderBound.right + TOUCH_FIELD)
                return POS_RIGHT;
        }

        // 前面的逻辑已经排除掉了几种情况 所以后面的 ┏ ┓ ┗ ┛ 边角就按照所占区域的方形来判断就可以了
        if (x > mBorderBound.left - TOUCH_FIELD && x < mBorderBound.left + BORDER_CORNER_LENGTH) {
            if (y > mBorderBound.top - TOUCH_FIELD && y < mBorderBound.top + BORDER_CORNER_LENGTH)
                return POS_TOP_LEFT;
            if (y > mBorderBound.bottom - BORDER_CORNER_LENGTH && y < mBorderBound.bottom + TOUCH_FIELD)
                return POS_BOTTOM_LEFT;
        }

        if (x > mBorderBound.right - BORDER_CORNER_LENGTH && x < mBorderBound.right + TOUCH_FIELD) {
            if (y > mBorderBound.top - TOUCH_FIELD && y < mBorderBound.top + BORDER_CORNER_LENGTH)
                return POS_TOP_RIGHT;
            if (y > mBorderBound.bottom - BORDER_CORNER_LENGTH && y < mBorderBound.bottom + TOUCH_FIELD)
                return POS_BOTTOM_RIGHT;
        }

        return -1;
    }

    private void setLastPosition(MotionEvent event) {
        mLastPoint.x = event.getX();
        mLastPoint.y = event.getY();
    }

    private void getBorderEdgeLength() {
        mBorderWidth = mBorderBound.width();
        mBorderHeight = mBorderBound.height();

    }
    //接下来，这里的设置可以让裁剪框拉伸到最大而且不报错哦

    /**
     * 这里设置的是mBorderWidth（裁剪框的宽度），但是会出现将裁剪框左右拉到最大的时候，会出现裁剪框上的白边有些超出图片，所以会出现mBorderWidth>mBmpBound.width而报错，
     * 所以设置一下的if条件来设置当左右拉伸为最大时，裁剪框的宽度和图片宽度一致
     */

    private void getBorderEdgeWidth() {
        mBorderWidth = mBorderBound.width();
        if (mBorderWidth > mBmpBound.width()) {
            mBorderWidth = mBmpBound.width();
        }
    }

    /**
     * 这里设置的是mBorderHeight（裁剪框的高度），但是会出现将裁剪框上下拉到最大的时候，会出现裁剪框上的白边有些超出图片，所以会出现mBorderHeight>mBmpBound.height()而报错，
     * 所以设置一下的if条件来设置当上下拉伸为最大时，裁剪框的高度和图片高度一致
     */
    private void getBorderEdgeHeight() {
        mBorderHeight = mBorderBound.height();
        if (mBorderHeight > mBmpBound.height()) {
            mBorderHeight = mBmpBound.height();
        }
    }

    // 缩小/扩大裁剪框
    private void resetLeft(float delta) {
        mBorderBound.left += delta;
        getBorderEdgeWidth();
        fixBorderLeft();
    }

    // 缩小/扩大裁剪框
    private void resetTop(float delta) {
        mBorderBound.top += delta;
        getBorderEdgeHeight();
        fixBorderTop();
    }

    // 缩小/扩大裁剪框
    private void resetRight(float delta) {
        //这句话，如果注销，就不能够左右伸缩裁剪框，只能上下伸缩
        mBorderBound.right += delta;
        getBorderEdgeWidth();
        fixBorderRight();

    }

    // 缩小/扩大裁剪框
    private void resetBottom(float delta) {
        //这句话，如果注销，就不能够上下伸缩裁剪框，只能左右伸缩
        mBorderBound.bottom += delta;
        getBorderEdgeHeight();
        fixBorderBottom();
    }

    private void fixBorderLeft() {
        // fix left
        if (mBorderBound.left < mBmpBound.left)
            mBorderBound.left = mBmpBound.left;
        if (mBorderWidth < 2 * BORDER_CORNER_LENGTH)
            mBorderBound.left = mBorderBound.right - 2 * BORDER_CORNER_LENGTH;
    }

    private void fixBorderTop() {
        // fix top
        if (mBorderBound.top < mBmpBound.top)
            mBorderBound.top = mBmpBound.top;
        if (mBorderHeight < 2 * BORDER_CORNER_LENGTH)
            mBorderBound.top = mBorderBound.bottom - 2 * BORDER_CORNER_LENGTH;
    }

    private void fixBorderRight() {
        // fix right
        if (mBorderBound.right > mBmpBound.right) {
            mBorderBound.right = mBmpBound.right;
        }

        if (mBorderWidth < 2 * BORDER_CORNER_LENGTH) {
            mBorderBound.right = mBorderBound.left + 2 * BORDER_CORNER_LENGTH;
        }

    }

    private void fixBorderBottom() {
        // fix bottom
        if (mBorderBound.bottom > mBmpBound.bottom)
            mBorderBound.bottom = mBmpBound.bottom;
        if (mBorderHeight < 2 * BORDER_CORNER_LENGTH)
            mBorderBound.bottom = mBorderBound.top + 2 * BORDER_CORNER_LENGTH;
    }
}
