package com.kkl.graffiti.edit;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;

import java.io.File;

/**
 * author  cst1718 on 2018/12/5 17:35
 * <p>
 * explain 直接在Activity中调用MediaScannerConnection有时候会内存泄漏导致无法刷新,提成工具类
 */
public class SingleMediaScanner implements MediaScannerConnectionClient {

    public interface ScanListener {
        public void onScanFinish();
    }

    private MediaScannerConnection mMs;
    private File                   mFile;
    private ScanListener           listener;

    public SingleMediaScanner(Context context, File f, ScanListener l) {
        listener = l;
        mFile = f;
        mMs = new MediaScannerConnection(context, this);
        mMs.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        mMs.scanFile(mFile.getAbsolutePath(), null);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        mMs.disconnect();
        listener.onScanFinish();
    }
}
