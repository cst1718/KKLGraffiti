package com.kkl.graffiti.home.adapter;

import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.kkl.graffiti.BaseActivity;
import com.kkl.graffiti.Constants;
import com.kkl.graffiti.R;
import com.kkl.graffiti.common.interfaces.OnRecycleViewItemClickListener;
import com.kkl.graffiti.common.util.ResourceUtils;
import com.kkl.graffiti.common.util.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author cst1718 on 2018/12/4 15:20
 * @explain
 */
public class MyPhotoAdapter extends RecyclerView.Adapter<MyPhotoAdapter.MyPhotoHolder> {

    private       BaseActivity                           mActivity;
    private       ArrayList<HashMap<String, String>>     mList;
    private       OnRecycleViewItemClickListener<String> mListener;
    private final int                                    mWidthPixels;

    public MyPhotoAdapter(BaseActivity activity) {
        mActivity = activity;
        DisplayMetrics viewMEtrics = activity.getResources()
                .getDisplayMetrics();
        mWidthPixels = viewMEtrics.widthPixels;
    }

    public void update(ArrayList<HashMap<String, String>> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public void setOnRecycleViewItemClickListener(OnRecycleViewItemClickListener<String> listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public MyPhotoHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.adapter_home_photo, viewGroup, false);
        MyPhotoHolder viewHolder = new MyPhotoHolder(view);
        viewHolder.mIvPic = (ImageView) view.findViewById(R.id.iv_holder_photo);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewHolder.mIvPic.getLayoutParams();
        params.height = Util.getScreenWidth(mActivity) / Constants.SHOW_COLUMN;
        viewHolder.mIvPic.setLayoutParams(params);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyPhotoHolder myPhotoHolder, final int i) {
        String thumb = mList.get(i).get(MediaStore.Images.Thumbnails.DATA);
        if (TextUtils.isEmpty(thumb)) {
            thumb = mList.get(i).get(MediaStore.Images.Media.DATA);
        }
        Picasso.with(mActivity).load("file://" + thumb)
                .fit()
                .error(R.drawable.doodle_ic_zoomer)
                .centerCrop()
                .skipMemoryCache()
                .transform(new CircleTransform(ResourceUtils.getResourcesDimension(R.dimen.public_round_radius_10px), mWidthPixels / 3))
                .into(myPhotoHolder.mIvPic);
        myPhotoHolder.mIvPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onResultCallback(i, mList.get(i).get(MediaStore.Images.Media.DATA), v);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mList != null) {
            return mList.size();
        }
        return 0;
    }

    class MyPhotoHolder extends RecyclerView.ViewHolder {
        ImageView mIvPic;

        public MyPhotoHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
