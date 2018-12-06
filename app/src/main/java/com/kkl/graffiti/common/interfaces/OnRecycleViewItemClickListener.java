package com.kkl.graffiti.common.interfaces;

import android.view.View;

public interface OnRecycleViewItemClickListener<T> {
    void onResultCallback(int index, T data, View view);
}