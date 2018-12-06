package com.kkl.graffiti.doodle.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.kkl.graffiti.BaseAlertDialogFragment;
import com.kkl.graffiti.R;
import com.kkl.graffiti.common.interfaces.IDialogsCallBack;

import static com.kkl.graffiti.common.interfaces.IDialogsCallBack.ButtonType.leftButton;
import static com.kkl.graffiti.common.interfaces.IDialogsCallBack.ButtonType.rightButton;

/**
 * @author cst1718 on 2018/12/5 16:01
 * @explain
 */
public class NormalAlertDialog extends BaseAlertDialogFragment implements View.OnClickListener {

    private final static String MSG = "msg";

    private String   mMessage;
    private TextView mTitle;
    private TextView mContent;
    private TextView mLeft;
    private TextView mRight;

    private IDialogsCallBack mCallback;

    public static NormalAlertDialog getNormalAlertDialog(String message) {
        NormalAlertDialog dialog = new NormalAlertDialog();
        Bundle bundle = new Bundle();
        bundle.putString(MSG, message);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    protected int getGravity() {
        return Gravity.CENTER;
    }

    @Override
    protected double getWidthRate() {
        return 0.65f;
    }

    @Override
    protected double getHeightRate() {
        return 0.3f;
    }

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.dialog_normal_alert;
    }

    @Override
    protected void initBundleDate() {
        super.initBundleDate();
        if (getArguments() != null) {
            mMessage = getArguments().getString(MSG);
        }
    }

    @Override
    protected void initView(View view) {
        mTitle = view.findViewById(R.id.dialog_title_text);
        mContent = view.findViewById(R.id.dialog_content_text);
        mLeft = view.findViewById(R.id.dialog_left_button_text);
        mRight = view.findViewById(R.id.dialog_right_button_text);
        setContent(mMessage);
        mLeft.setOnClickListener(this);
        mRight.setOnClickListener(this);
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }

    public void setContent(String content) {
        mContent.setText(content);
    }

    public void setBtnText(String left, String right) {
        mLeft.setText(left);
        mRight.setText(right);
    }

    public void setOnButtonClickCallback(IDialogsCallBack callback) {
        this.mCallback = callback;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_left_button_text:
                if (mCallback != null) {
                    mCallback.DialogsCallBack(leftButton, NormalAlertDialog.this);
                }
                break;
            case R.id.dialog_right_button_text:
                if (mCallback != null) {
                    mCallback.DialogsCallBack(rightButton, NormalAlertDialog.this);
                }
                break;
        }
    }
}
