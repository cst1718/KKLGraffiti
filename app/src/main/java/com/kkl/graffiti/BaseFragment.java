package com.kkl.graffiti;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * @author cst1718 on 2018/12/4 13:59
 * @explain
 */
public class BaseFragment extends Fragment {

    /**
     * 不保存状态
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    @Override
    public void startActivity(Intent intent) {
        // TODO Auto-generated method stub
        super.startActivity(intent);

        // 设置切换动画，从右边进入，左边退出
        getActivity().overridePendingTransition(R.anim.anim_push_left_in, R.anim.anim_push_left_out);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);

        // 设置切换动画，从右边进入，左边退出
        getActivity().overridePendingTransition(R.anim.anim_push_left_in, R.anim.anim_push_left_out);
    }
}
