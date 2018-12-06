package com.kkl.graffiti.common.interfaces;

import com.kkl.graffiti.BaseAlertDialogFragment;

/**
 * @author cst1718 on 2018/12/5 18:49
 * @explain
 */

public interface IDialogsCallBack {
    public enum ButtonType {
        leftButton, rightButton,
    }

    void DialogsCallBack(ButtonType buttonType, BaseAlertDialogFragment thisDialogs);
}
