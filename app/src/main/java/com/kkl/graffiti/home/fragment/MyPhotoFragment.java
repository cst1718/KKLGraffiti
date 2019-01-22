package com.kkl.graffiti.home.fragment;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kkl.graffiti.AppConfig;
import com.kkl.graffiti.BaseActivity;
import com.kkl.graffiti.BaseFragment;
import com.kkl.graffiti.Constants;
import com.kkl.graffiti.R;
import com.kkl.graffiti.common.interfaces.OnRecycleViewItemClickListener;
import com.kkl.graffiti.edit.EditActivity;
import com.kkl.graffiti.edit.ShowActivity;
import com.kkl.graffiti.home.adapter.MyPhotoAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

/**
 * @author cst1718 on 2018/12/4 14:00
 * @explain 我的作品
 */
public class MyPhotoFragment extends BaseFragment {

    private static final int REQ_DOODLE = 101;

    private BaseActivity   mActivity;
    private MyPhotoAdapter mAdapter;
    private MyAsyncTask    mAsyncTask;
    private RecyclerView   mRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_photo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity = (BaseActivity) getActivity();
        mRecyclerView = view.findViewById(R.id.rv_home_photo_content);
        GridLayoutManager manager = new GridLayoutManager(mActivity, Constants.SHOW_COLUMN);
        mRecyclerView.setLayoutManager(manager);
        mAdapter = new MyPhotoAdapter(mActivity);
        mRecyclerView.setAdapter(mAdapter);
        mAsyncTask = new MyAsyncTask();
        mAsyncTask.execute();
        mAdapter.setOnRecycleViewItemClickListener(new OnRecycleViewItemClickListener<String>() {
            @Override
            public void onResultCallback(int index, String data, View view) {
//                go2DoodleActivity(data);
                go2ShowActivity(index);
            }
        });
    }

    private void go2DoodleActivity(String path) {
        Intent activityIntent = EditActivity.getActivityIntent(mActivity, path, EditActivity.Type.NOEDGE);
        startActivityForResult(activityIntent, REQ_DOODLE);
    }

    private void go2ShowActivity(int position) {
        Intent activityIntent = ShowActivity.getActivityIntent(mActivity, position);
        startActivityForResult(activityIntent, REQ_DOODLE);
    }

    public void refreshPhoto() {
        mAsyncTask = new MyAsyncTask();
        mAsyncTask.execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQ_DOODLE) {
            refreshPhoto();
        }
    }

    class MyAsyncTask extends AsyncTask<Void, Void, ArrayList<HashMap<String, String>>> {

        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(Void... voids) {
            //            ArrayList<HashMap<String, String>> list = queryThumb();
            ArrayList<HashMap<String, String>> list = getDirList();
            if (list == null || list.size() == 0) {
                return null;
            }
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<HashMap<String, String>> hashMaps) {
            mAdapter.update(hashMaps);
        }
    }

    private ArrayList<HashMap<String, String>> getDirList() {
        File[] files = new File(AppConfig.getSaveDirPath()).listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        //每张图片的id值，原地址值，缓存地址值为一个map
        ArrayList<HashMap<String, String>> list = new ArrayList<>(files.length);
        Arrays.sort(files);// 数组只有升序,此时是按照文件名从小到大
        for (File file : files) {
            if (file.isDirectory() || !file.getName().endsWith("jpg") || file.length() < 100) {
                continue;
            }
            HashMap<String, String> value = new HashMap<>(3);
            value.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            list.add(0, value);
        }
        return list;
    }

    /** 查询缩略图,某些机型插入数据库无回调?废弃不用 */
    private ArrayList<HashMap<String, String>> queryThumb() {
        boolean isFirst = true;
        StringBuilder sb = new StringBuilder();
        File[] files = new File(AppConfig.getSaveDirPath()).listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        ArrayList<String> selectArgs = new ArrayList<>(files.length);
        for (File file : files) {
            if (file.isDirectory() || !file.getName().endsWith("jpg") || file.length() < 100) {
                continue;
            }
            if (!isFirst) {
                sb.append(" OR ");
            }
            sb.append(MediaStore.Images.Media.DATA).append("=?");
            isFirst = false;
            selectArgs.add(file.getAbsolutePath());
        }
        ContentResolver contentResolver = mActivity.getContentResolver();
        String[] projection = {MediaStore.Images.Media.DATA,
                               MediaStore.Images.Media._ID,
                               };

        // 通过绝对路径查询数据库中相片对应的唯一id
        Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                              projection,
                                              sb.toString(),
                                              selectArgs.toArray(new String[selectArgs.size()]),
                                              MediaStore.Images.Media.DATE_ADDED + " DESC");
        if (cursor == null || cursor.getCount() == 0) {
            return null;
        }

        //每张图片的id值，原地址值，缓存地址值为一个map
        ArrayList<HashMap<String, String>> list = new ArrayList<>(files.length);
        if (cursor.moveToFirst()) {
            int _idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            int _dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            do {
                HashMap<String, String> value = new HashMap<>(3);
                value.put(MediaStore.Images.Media._ID, cursor.getInt(_idColumn) + "");
                value.put(MediaStore.Images.Media.DATA, cursor.getString(_dataColumn));
                list.add(value);
            } while (cursor.moveToNext());
        }
        cursor.close();

        // 根据每个图片id获取系统保存的缓存
        String media_id;
        for (int i = 0; i < list.size(); i++) {
            HashMap<String, String> map = list.get(i);
            media_id = map.get(MediaStore.Images.Media._ID);
            if (media_id != null) {
                cursor = contentResolver.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                                               new String[]{
                                                       MediaStore.Images.Thumbnails.DATA,
                                                       },
                                               MediaStore.Images.Thumbnails.IMAGE_ID + " = " + media_id,
                                               null,
                                               null);
                if (cursor == null || cursor.getCount() == 0) {
                    continue;
                }
                if (cursor.moveToFirst()) {
                    do {
                        // 保存缓存图片路径
                        map.put(MediaStore.Images.Thumbnails.DATA, cursor.getString(0));
                    } while (cursor.moveToNext());
                    cursor.close();
                }
            }
        }
        return list;
    }
}
