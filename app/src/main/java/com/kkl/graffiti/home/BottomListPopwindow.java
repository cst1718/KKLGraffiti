package com.kkl.graffiti.home;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.kkl.graffiti.R;
import com.kkl.graffiti.common.util.ResourceUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2016/3/10.
 */
public class BottomListPopwindow extends PopupWindow {
    private static final String TAG = "BottomListPopwindow";
    protected Activity mActivity;
    private PopWindowAdapter mAdapter;
    private ListView mListView;
    private List<String> mData;
    private Window mWindow;

    public BottomListPopwindow(Activity activity) {
        this(activity, null);
    }

    public BottomListPopwindow(Activity activity, String[] data) {
        this.mActivity = activity;
        mWindow = mActivity.getWindow();
        createListPopWindow();
        setData(data);
    }


    private void createListPopWindow() {
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setOutsideTouchable(true);
        this.setFocusable(true);
        this.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                setDim(1.0f);
            }
        });

        this.setContentView(initListView());
    }

    private void setDim(float alpha) {
        if (null == mWindow) {
            mWindow = mActivity.getWindow();
        }
        if (null == mWindow)
            return;
        WindowManager.LayoutParams lp = mWindow.getAttributes();
        lp.alpha = alpha; //越小越暗,window背景

        // 华为不变透明度
        if (alpha == 1.0f) {
            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        } else {
            mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        mWindow.setAttributes(lp);
        this.setBackgroundDrawable(ResourceUtils.getResourcesDrawable(R.color.public_color_bg_protrude));
    }


    public void setOnItemClickListener(AdapterView.OnItemClickListener l) {
        if (null != mListView) {
            mListView.setOnItemClickListener(l);
        }
    }


    public void showAtBottom(View v) {
        setDim(0.4f);
        this.showAtLocation(v, Gravity.BOTTOM | Gravity.RIGHT, 0, 0);
    }

    public void showAtBottomNoDim(View v) {
        this.setBackgroundDrawable(ResourceUtils.getResourcesDrawable(R.color.public_color_bg_protrude));
        this.showAtLocation(v, Gravity.BOTTOM | Gravity.RIGHT, 0, 0);
    }

    private ListView initListView() {
        mListView = new ListView(mActivity);
        mListView.setVerticalScrollBarEnabled(false);
        mListView.setSelector(R.drawable.selector_bg_white_normal);
        mListView.setDivider(null);
        mAdapter = new PopWindowAdapter(mData);
        mListView.setAdapter(mAdapter);
        return mListView;
    }

    public void setData(String[] data, boolean isNeedCancel) {
        if (null != mAdapter) {
            if (null == data) {
                return;
            } else {
                mData = Arrays.asList(data);
            }
            mData = new ArrayList<String>(mData);
            if (isNeedCancel) {
                mData.add("-1");
            }
            mAdapter.resetData(mData);
        }
        /*if (null != mData) {
            this.setHeight(ScreenUtils.dip2px(mData.size() * 60));
        }*/
    }

    private int mUnablePosition = -1;

    public void setData(String[] data, int unablePosition) {
        mUnablePosition = unablePosition;
        setData(data, false);
    }

    public void setData(String[] data) {
        setData(data, false);
    }

    private class PopWindowAdapter extends BaseAdapter {
        private List<String> data;

        private PopWindowAdapter(List<String> data) {
            this.data = data;
        }

        private void resetData(List<String> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if (null == data || data.isEmpty())
                return 0;
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            if (null == data)
                return null;
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String info = data.get(position);
            if (null == info)
                return convertView;
            ViewHolder viewHolder;
            if (null == convertView) {
                convertView = LayoutInflater.from(mActivity).inflate(R.layout.adapter_listpopwindow_list_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.mIvClose = (ImageView) convertView.findViewById(R.id.iv_listpopwindow_cancel);
                viewHolder.mTvTitle = (TextView) convertView.findViewById(R.id.tv_listpopwindow_item);
                viewHolder.mLine = convertView.findViewById(R.id.view_line);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.mTvTitle.setEnabled(mUnablePosition != position);

            if (info.equals("-1")) {
                viewHolder.mIvClose.setVisibility(View.VISIBLE);
                viewHolder.mTvTitle.setVisibility(View.GONE);
                viewHolder.mLine.setVisibility(View.GONE);
            } else {
                viewHolder.mTvTitle.setText(info);
                viewHolder.mIvClose.setVisibility(View.GONE);
                viewHolder.mLine.setVisibility(View.VISIBLE);
                viewHolder.mTvTitle.setVisibility(View.VISIBLE);
            }
            return convertView;
        }

        class ViewHolder {
            TextView mTvTitle;
            ImageView mIvClose;
            View mLine;
        }
    }

}
