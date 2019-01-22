package com.kkl.graffiti.edit.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kkl.graffiti.R;
import com.kkl.graffiti.common.interfaces.OnRecycleViewItemClickListener;
import com.kkl.graffiti.common.util.ResourceUtils;
import com.kkl.graffiti.edit.bean.FilterBean;

import java.util.ArrayList;

/**
 * @author cst1718 on 2019/1/18 09:53
 * @explain
 */
public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.MyHolder> {

    private ArrayList<FilterBean> mList;

    private OnRecycleViewItemClickListener<FilterBean> mCallback;

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_filter_item, viewGroup, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {
        final FilterBean info = mList.get(position);

        if (info.mIsSelect) {
            holder.mLayoutBg.setBackgroundColor(ResourceUtils.getResourcesColor(R.color.public_color_title_bg));
            holder.mTvName.setEnabled(true);
        } else {
            holder.mTvName.setEnabled(false);
            holder.mLayoutBg.setBackgroundColor(ResourceUtils.getResourcesColor(R.color.transparent));
        }

        holder.mIvIcon.setImageBitmap(info.mBitmap);
        holder.mTvName.setText(info.mName);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (info.mIsSelect) {
                    return;
                }
                for (FilterBean info : mList) {
                    info.mIsSelect = false;
                }
                info.mIsSelect = true;
                if (mCallback != null) {
                    mCallback.onResultCallback(position, mList.get(position), holder.itemView);
                }
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public void updateList(ArrayList<FilterBean> list) {
        if (list == null) {
            return;
        }
        if (mList == null) {
            mList = new ArrayList<>();
        }
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public ArrayList<FilterBean> getData() {
        return mList;
    }

    public void onDestroy() {
        if (mList != null) {
            for (FilterBean bean : mList) {
                if (bean.mBitmap != null) {
                    bean.mBitmap.recycle();
                    bean.mBitmap = null;
                }
            }
            mList.clear();
            mList = null;
        }
    }

    public void setOnItemClick(OnRecycleViewItemClickListener<FilterBean> callback) {
        mCallback = callback;
    }

    class MyHolder extends RecyclerView.ViewHolder {

        ImageView mIvIcon;
        TextView  mTvName;
        View      mLayoutBg;

        MyHolder(@NonNull View itemView) {
            super(itemView);
            mIvIcon = itemView.findViewById(R.id.iv_filter_small);
            mTvName = itemView.findViewById(R.id.tv_filter_name);
            mLayoutBg = itemView.findViewById(R.id.fl_filter_bg);
        }
    }
}
