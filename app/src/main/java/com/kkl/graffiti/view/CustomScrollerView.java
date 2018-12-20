package com.kkl.graffiti.view;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * @author cst1718 on 2018/12/18 15:58
 * @explain
 */
public class CustomScrollerView extends Scroller {
    private int mDuration;

    public CustomScrollerView(Context context) {
        super(context);
    }

    public CustomScrollerView(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    public CustomScrollerView(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, mDuration);
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }
}
