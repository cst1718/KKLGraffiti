package com.kkl.graffiti.home.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kkl.graffiti.R;

/**
 * @author cst1718 on 2018/12/11 12:51
 * @explain
 */
public class AboutView extends RelativeLayout {

    private TextView mTvLeft;
    private TextView mTvRight;
    private View     mLine;
    private View     mAlert;

    public AboutView(Context context) {
        super(context);
        init(context, null);
    }

    public AboutView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AboutView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.view_about_view, this, true);
        mTvLeft = inflate.findViewById(R.id.tv_about_left);
        mTvRight = inflate.findViewById(R.id.tv_about_right);
        mLine = inflate.findViewById(R.id.line_about_bottom);
        mAlert = inflate.findViewById(R.id.view_round_alert);
        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AboutView);
            setLeftText(a.getString(R.styleable.AboutView_about_left_text));
            setRightText(a.getString(R.styleable.AboutView_about_right_text));
            showBottomLine(a.getBoolean(R.styleable.AboutView_about_bottom_line, false));
            showAlert(a.getBoolean(R.styleable.AboutView_about_alert, false));
            showArrow(a.getBoolean(R.styleable.AboutView_about_arrow, false));
            a.recycle();
        }
    }

    public void setLeftText(CharSequence text) {
        mTvLeft.setText(text);
    }

    public void setRightText(CharSequence text) {
        mTvRight.setText(text);
    }

    public void showArrow(boolean show) {
        mTvRight.setCompoundDrawablesWithIntrinsicBounds(0, 0, show ? R.drawable.btn_public_arrow_right_hover : 0, 0);
    }

    public void showAlert(boolean show) {
        mAlert.setVisibility(show ? VISIBLE : GONE);
    }

    public void showBottomLine(boolean show) {
        mLine.setVisibility(show ? VISIBLE : GONE);
    }
}
