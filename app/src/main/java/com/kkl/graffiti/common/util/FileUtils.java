package com.kkl.graffiti.common.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class FileUtils {

    /**
     * 读取128k以下文件的所有字符串
     *
     * @param filePath    文件路径
     * @param charsetName 编码类型
     * @return
     */
    public static String read(String filePath, String charsetName) {
        FileInputStream is = null;
        if (isExist(filePath)) {
            try {
                is = new FileInputStream(filePath);
                if (is.available() > 128 * 1024) {
                    LogUtils.d("File size > 128k not supported!");
                    return null;
                }
                byte[] data = new byte[is.available()];
                is.read(data);
                return new String(data, charsetName);
            } catch (Exception e) {
                LogUtils.w(e);
            } finally {
                closeStream(is);
            }
        }
        return null;
    }

    /**
     * 最多读取128K的字符串。
     * 已测。稳定。
     *
     * @param filePath
     * @param charsetName
     * @return
     */
    public static String bufferRead(String filePath, String charsetName) {
        if (isExist(filePath)) {

            try {
                return bufferRead(new FileInputStream(filePath), charsetName);
            } catch (Exception e) {
                LogUtils.w(e);
            }
        }
        return null;
    }

    /**
     * 最多读取128K的字符串。
     * 已测。稳定。
     *
     * @param stream
     * @param charsetName
     * @return
     */
    public static String bufferRead(InputStream stream, String charsetName) {
        if (null == stream) return null;
        BufferedInputStream is = null;
        try {
            is = new BufferedInputStream(stream);

            int readSize = is.available();
            int maxSize = 128 * 1024;
            if (readSize < 0) {
                return null;
            } else if (readSize > maxSize) {
                LogUtils.d("File size > 128k not supported!");
                readSize = maxSize;
            }
            byte[] data = new byte[readSize];
            is.read(data);
            return new String(data, charsetName);
        } catch (Exception e) {
            LogUtils.w(e);
        } finally {
            closeStream(is);
        }

        return null;
    }

    public static String readFromRaw(Context ctx, int rawId) {
        if (null == ctx) return null;

        InputStreamReader inputReader = null;
        BufferedReader bufReader = null;

        try {
            inputReader = new InputStreamReader(ctx.getResources().openRawResource(rawId));
            bufReader = new BufferedReader(inputReader);

            String line = "";
            String Result = "";
            while ((line = bufReader.readLine()) != null)
                Result += line;
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            CloseableUtils.close(bufReader);
            CloseableUtils.close(inputReader);
        }
    }

    public static String readFromAssets(Context ctx, String fileName) {
        if (null == ctx) return null;

        InputStreamReader inputReader = null;
        BufferedReader bufReader = null;

        try {
            inputReader = new InputStreamReader(ctx.getAssets().open(fileName));
            bufReader = new BufferedReader(inputReader);

            String line = "";
            String Result = "";
            while ((line = bufReader.readLine()) != null)
                Result += line;
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            CloseableUtils.close(bufReader);
            CloseableUtils.close(inputReader);
        }
    }

    /**
     * 把字符串写入文件
     *
     * @param str
     */
    public static void write(String path, byte[] str) {
        if (!TextUtils.isEmpty(path) && str != null) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(path);
                fos.write(str);
            } catch (Exception e) {
                LogUtils.w(e);
            } finally {
                try {
                    if (fos != null)
                        fos.close();
                } catch (IOException e) {
                    LogUtils.w(e);
                }
            }
        }
    }

    public static String read(Context ctx, String fName) {
        if (null == ctx || null == fName) return null;

        FileInputStream fin = null;
        ByteArrayOutputStream bout = null;
        try {
            fin = ctx.openFileInput(fName);
            bout = new ByteArrayOutputStream();

            byte[] bytes = new byte[1024];
            int readlen = 0;
            while ((readlen = fin.read(bytes, 0, 1024)) != -1) {
                bout.write(bytes, 0, readlen);
            }

            return new String(bout.toByteArray());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            CloseableUtils.close(fin);
            CloseableUtils.close(bout);
        }

    }

    public static boolean delete(Context ctx, String fileName) {
        if (null == ctx || null == fileName) return false;
        ctx.deleteFile(fileName);
        return true;
    }

    public static boolean write(Context ctx, String fileName, String content) {
        if (null == ctx || null == fileName || null == content) return false;

        FileOutputStream outputStream = null;
        try {
            outputStream = ctx.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(content.getBytes("utf-8"));
            outputStream.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            CloseableUtils.close(outputStream);
        }
    }

    /**
     * 追加内容到文件。
     *
     * @param ctx
     * @param fileName
     * @param content
     * @return
     */
    public static boolean append(Context ctx, String fileName, String content) {
        if (null == ctx || null == fileName || null == content) return false;

        FileOutputStream outputStream = null;
        try {
            outputStream = ctx.openFileOutput(fileName, Context.MODE_APPEND);
            outputStream.write(content.getBytes("utf-8"));
            outputStream.flush();
            return true;
        } catch (Exception e) {
            LogUtils.d("FileUtils", "append err=" + e);
            return false;
        } finally {
            CloseableUtils.close(outputStream);
        }
    }

    /**
     * 追加内容到指定的文件如果没有，则创建新文件。
     *
     * @param fileName
     * @param content  内容不可过大。
     * @return
     */
    public static boolean append(String fileName, String content) {
        if (null == fileName) return false;

        if (TextUtils.isEmpty(content)) return true;

        File file = new File(fileName);
        BufferedOutputStream os = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            os = new BufferedOutputStream(new FileOutputStream(file, true));
            os.write(content.getBytes("utf-8"));
            os.flush();

            return true;
        } catch (IOException ex) {
            return false;
        } finally {
            CloseableUtils.close(os);
        }
    }

    /**
     * 判断文件是否存在
     *
     * @param path
     * @return
     */
    public static boolean isExist(String path) {
        if (TextUtils.isEmpty(path))
            return false;
        File file = new File(path);
        return file.exists();
    }

    /**
     * 判断文件是否存在
     *
     * @param file
     * @return
     */
    public static boolean isExist(File file) {
        if (file == null)
            return false;
        return file.exists();
    }

    /**
     * 创建文件夹
     *
     * @return
     */
    public static boolean createDir(String path) {
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.exists()) {
                return true;
            } else {
                return file.mkdirs();
            }
        }
        return false;
    }

    /**
     * 创建文件夹
     *
     * @return
     */
    public static boolean createDir(File path) {
        boolean r = false;
        if (path != null && !path.exists()) {
            r = path.mkdirs();
        }
        return r;
    }

    /**
     * 创建文件，如果文件所在的目录不存在，则先创建其目录
     *
     * @param fileFullPath 文件全路径
     * @return
     */
    public static boolean createFile(String fileFullPath) {
        boolean r = false;
        if (!TextUtils.isEmpty(fileFullPath)) {
            File file = new File(fileFullPath);
            if (!file.exists()) {
                try {
                    if (file.getParentFile() != null && !file.getParentFile().exists()) {
                        r = file.getParentFile().mkdirs();
                    }
                    r = file.createNewFile();
                } catch (IOException e) {
                }
            }
        }
        return r;
    }

    /**
     * 创建文件 ，如果文件所在的目录不存在，则先创建其目录
     *
     * @param file
     * @return
     */
    public static boolean createFile(File file) {
        boolean r = false;
        if (file != null && !file.exists()) {
            try {
                if (file.getParentFile() != null && !file.getParentFile().exists()) {
                    r = file.getParentFile().mkdirs();
                }
                r = file.createNewFile();
            } catch (IOException e) {
            }
        }
        return r;
    }

    /**
     * 删除文件，可以是单个文件或文件夹
     *
     * @param fileName 待删除的文件名
     * @return 文件删除成功返回true, 否则返回false
     */

    public static boolean delete(String fileName) {
        if (!TextUtils.isEmpty(fileName)) {
            return delete(new File(fileName));
        }
        return false;
    }

    /**
     * 删除文件，可以是单个文件或文件夹
     *
     * @param file 待删除的文件
     * @return 文件删除成功返回true, 否则返回false
     */

    public static boolean delete(File file) {
        if (file == null || !file.exists()) {
            return false;
        } else {
            if (file.isFile()) {
                return deleteFile(file);

            } else {
                return deleteDirectory(file);

            }
        }
    }

    /**
     * 删除文件，可以是单个文件或文件夹
     *
     * @param filePath 待删除的文件名
     * @param filter   过滤包含filter字符的文件或文件夹
     * @return 文件删除成功返回true, 否则返回false
     */

    public static boolean delete(String filePath, final String filter) {
        if (!TextUtils.isEmpty(filePath)) {
            File file = new File(filePath);
            String[] files = file.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    if (filter.contains(filename)) {
                        return false;
                    }
                    return true;
                }
            });
            if (files != null && files.length > 0) {
                for (String f : files) {
                    delete(filePath + File.separator + f);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 删除文件，可以是单个文件或文件夹
     *
     * @param filePath 待删除的文件名
     * @param filters  过滤的文件或文件夹,区分大小写
     * @return 文件删除成功返回true, 否则返回false
     */

    public static boolean delete(final String filePath, final String... filters) {
        if (!TextUtils.isEmpty(filePath)) {
            File file = new File(filePath);
            String[] files = file.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    if (filters != null) {
                        if (ArrayUtils.contains(filters, filePath + File.separator + filename)) {
                            return false;
                        }
                    }
                    return true;
                }
            });
            if (files != null && files.length > 0) {
                for (String f : files) {
                    delete(filePath + File.separator + f);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 删除单个文件
     *
     * @param fileName 被删除文件的文件名
     * @return 单个文件删除成功返回true, 否则返回false
     */
    private static boolean deleteFile(String fileName) {
        if (TextUtils.isEmpty(fileName))
            return false;
        return deleteFile(new File(fileName));
    }

    /**
     * 删除单个文件
     *
     * @param file 被删除文件的文件名
     * @return 单个文件删除成功返回true, 否则返回false
     */
    public static boolean deleteFile(File file) {
        if (file != null && file.isFile() && file.exists()) {
            file.delete();
            return true;
        }
        return false;
    }

    /**
     * 删除目录（文件夹）以及目录下的文件
     *
     * @param dir 被删除目录的文件路径
     * @return 目录删除成功返回true, 否则返回false
     */
    public static boolean deleteDirectory(String dir) {
        if (TextUtils.isEmpty(dir))
            return false;
        if (!dir.endsWith(File.separator)) {// 如果dir不以文件分隔符结尾，自动添加文件分隔符
            dir = dir + File.separator;
        }
        return deleteDirectory(new File(dir));
    }

    /**
     * 删除目录（文件夹）以及目录下的文件
     *
     * @param dirFile 被删除目录的文件路径
     * @return 目录删除成功返回true, 否则返回false
     */
    private static boolean deleteDirectory(File dirFile) {
        if (dirFile == null || !dirFile.exists() || !dirFile.isDirectory()) {// 如果dir对应的文件不存在，或者不是一个目录，则退出
            return false;
        }
        boolean flag = true;
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {// 删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            } else {// 删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            }
        }
        if (!flag) {
            return false;

        }
        if (dirFile.delete()) {// 删除当前目录
            return true;

        } else {
            return false;
        }
    }

    /**
     * 得到父目录
     *
     * @param path
     * @return
     */
    public static String getParentPath(String path) {
        if (TextUtils.isEmpty(path))
            return null;
        File file = new File(path);
        if (file.isDirectory()) {
            return path;
        }
        return file.getParent();
    }

    /**
     * 判断sd卡上是否有指定大小的可用空间
     *
     * @param size 空间大小单位为兆
     * @return
     */
    @SuppressWarnings("deprecation")
    public static boolean hasSDSpaceAvailable(long size) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String sdcard = Environment.getExternalStorageDirectory().getPath();
            StatFs statFs = new StatFs(sdcard);
            long blockSize = statFs.getBlockSize();
            long blocks = statFs.getAvailableBlocks();
            long availableSpare = (blocks * blockSize) / (1024 * 1024);
            if (size > availableSpare) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断sd卡是否可用
     *
     * @return
     */
    public static boolean hasSDMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取存储盘的路径,如果存储卡可用返回存储卡路径，否则返回应用文件路径
     *
     * @param appContext
     * @return
     */
    public static String getSavePath(Context appContext) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        } else
            return appContext.getFilesDir().getAbsolutePath();
    }

    /**
     * 获取文件的大小
     *
     * @param file
     * @return
     */
    public static long fileSize(String file) {
        if (TextUtils.isEmpty(file)) {
            return 0;
        }
        File f = new File(file);
        return f.exists() ? f.length() : 0;
    }

    /**
     * 保存图片至sd卡
     *
     * @param bm           图片
     * @param fullFileName 文件全名
     */
    public static boolean saveBitmapToSd(Bitmap bm, String fullFileName) {
        if (bm == null) {
            LogUtils.i("bitmap is null");
            return false;
        }

        File file = new File(fullFileName);
        OutputStream outStream = null;
        try {
            // 如果指定目录未存在，先创建。否则创建文件时报错
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            file.createNewFile();
            outStream = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeStream(outStream);
        }
        return false;
    }

    /**
     * 保存图片至sd卡
     *
     * @param bm           图片
     * @param quality      0~100
     * @param fullFileName 文件全名
     */
    public static boolean saveBitmapToSd(Bitmap bm, int quality, String fullFileName) {
        File file = new File(fullFileName);
        return saveBitmap(bm, quality, file);
    }

    public static boolean saveBitmap(Bitmap bm, int quality, File file) {
        if (bm == null) {
            System.out.println("bitmap is null");
            return false;
        }

        OutputStream outStream = null;
        try {
            // 如果指定目录未存在，先创建。否则创建文件时报错
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            file.createNewFile();
            outStream = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, quality, outStream);
            outStream.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeStream(outStream);
        }
        return false;
    }

    /**
     * 保存图片至sd卡
     *
     * @param bm           图片
     * @param fullFileName 文件全名
     */
    public static boolean savePngBitmapToSd(Bitmap bm, String fullFileName) {
        if (bm == null) {
            LogUtils.i("bitmap is null");
            return false;
        }

        File file = new File(fullFileName);
        OutputStream outStream = null;
        try {
            // 如果指定目录未存在，先创建。否则创建文件时报错
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            file.createNewFile();
            outStream = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeStream(outStream);
        }
        return false;
    }

    /**
     * 关闭流
     *
     * @param stream
     */
    public static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * 复制assets下的文件到指定路径
     *
     * @param context
     * @param fileName 文件名
     * @param path     文件路径
     * @throws IOException
     */
    public static void copyAssetsData(Context context, String fileName, String path) throws IOException {
        String name = fileName.contains(File.separator) ? fileName.substring(fileName.lastIndexOf(File.separator) + 1)
                : fileName;
        FileUtils.copyStream(context.getAssets().open(name), new BufferedOutputStream(new FileOutputStream(path
                + File.separator + fileName)));
    }

    /**
     * 复制assets下的文件到应用目录下
     *
     * @param context
     * @param fileName 文件名
     * @throws IOException
     */
    public static void copyAssetsToApp(Context context, String fileName) throws IOException {
        if (null == context || null == fileName) return;
        FileUtils.copyStream(context.getAssets().open(fileName), context.openFileOutput(fileName, Context.MODE_PRIVATE));
    }

    /**
     * 流复制
     *
     * @param in
     * @param out
     * @throws IOException
     */
    public static void copyStream(InputStream in, OutputStream out)
            throws IOException {
        try {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
        } catch (IOException e) {
            throw e;
        } finally {
            closeStream(in);
            closeStream(out);
        }
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static void copyFile(String oldPath, String newPath) {
        File oldFile = new File(oldPath);
        copyFile(oldFile, newPath);
    }

    public static void copyFile(File source, String newPath) {
        InputStream inStream = null;
        OutputStream outputStream = null;
        try {
            int byteRead;
            if (source.exists()) { //文件存在时
                inStream = new FileInputStream(source); //读入原文件
                outputStream = new FileOutputStream(newPath);
                byte[] buffer = new byte[2048];
                while ((byteRead = inStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, byteRead);
                }
            }
        } catch (IOException e) {
            LogUtils.e(e.getMessage());
        } finally {
            CloseableUtils.close(inStream);
            CloseableUtils.close(outputStream);
        }
    }

    /**
     * 复制整个文件夹内容
     *
     * @param oldPath String 原文件路径 如：c:/fqf
     * @param newPath String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    public static void copyFolder(String oldPath, String newPath) {
        try {
            (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹
            File a = new File(oldPath);
            String[] file = a.list();
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file[i]);
                } else {
                    temp = new File(oldPath + File.separator + file[i]);
                }
                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" +
                            (temp.getName()).toString());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) {//如果是子文件夹
                    copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
                }
            }
        } catch (Exception e) {
            System.out.println("复制整个文件夹内容操作出错");
            e.printStackTrace();
        }
    }/*
     * <!-- 在SDCard中创建与删除文件权限 --> <uses-permission
     * android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS"/> <!--
     * 往SDCard写入数据权限 --> <uses-permission
     * android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
     */

    // =================get SDCard information===================
    public static boolean isSdcardAvailable() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    public static long getSDAllSizeKB() {
        // get path of sdcard
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        // get single block size(Byte)
        long blockSize = sf.getBlockSize();
        // 获取所有数据块数
        long allBlocks = sf.getBlockCount();
        // 返回SD卡大小
        return (allBlocks * blockSize) / 1024; // KB
    }

    /**
     * free size for normal application
     *
     * @return
     */
    public static long getSDAvalibleSizeKB() {
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        long blockSize = sf.getBlockSize();
        long avaliableSize = sf.getAvailableBlocks();
        return (avaliableSize * blockSize) / 1024;// KB
    }

    // =====================File Operation==========================
    public static boolean isFileExist(String director) {
        File file = new File(Environment.getExternalStorageDirectory()
                + File.separator + director);
        return file.exists();
    }

    public static File writeToSDCardFile(String directory, String fileName,
                                         String content, boolean isAppend) {
        return writeToSDCardFile(directory, fileName, content, "", isAppend);
    }


    public static long getFileSizeOfSDCard(String directory, String fname) {
        if (null == directory || null == fname) return -1;

        String path = Environment.getExternalStorageDirectory()
                + File.separator + directory + File.separator + fname;

        return getFileSize(path);
    }

    public static long getFileSize(String path) {
        if (TextUtils.isEmpty(path)) {
            return -1;
        }

        File file = new File(path);
        return (file.exists() && file.isFile() ? file.length() : -1);
    }

    public static void writeToSDCardFileWithLimit(String dir, String fname,
                                                  String content, String encoding, boolean isAppend, long maxsize) {

        if (null == dir || null == fname || maxsize < 0) return;

        String path = Environment.getExternalStorageDirectory() + File.separator + dir;

        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(fname)) {
            return;
        }

        OutputStream os = null;
        try {
            if (!createDir(path)) {
                return;
            }

            File file = new File(path + File.separator + fname);
            if (file.exists() && file.isFile() && file.length() > maxsize) {

                file.delete();
            }

            os = new BufferedOutputStream(new FileOutputStream(file, isAppend));
            if (encoding.equals("")) {
                os.write(content.getBytes());
            } else {
                os.write(content.getBytes(encoding));
            }
            os.flush();
        } catch (IOException e) {
            LogUtils.e("FileUtil", "writeToSDCardFile:" + e.getMessage());
        } finally {
            CloseableUtils.close(os);
        }

    }

    /**
     * @param directory (you don't need to begin with
     *                  Environment.getExternalStorageDirectory()+File.separator)
     * @param fileName
     * @param content
     * @param encoding  (UTF-8...)
     * @param isAppend  : Context.MODE_APPEND
     * @return
     */
    public static File writeToSDCardFile(String directory, String fileName,
                                         String content, String encoding, boolean isAppend) {
        // mobile SD card path +path
        File file = null;
        OutputStream os = null;
        try {
            if (!createDir(Environment.getExternalStorageDirectory()
                    + File.separator + directory)) {
                return file;
            }

            file = new File(Environment.getExternalStorageDirectory()
                    + File.separator + directory + File.separator + fileName);
            os = new FileOutputStream(file, isAppend);
            if (encoding.equals("")) {
                os.write(content.getBytes());
            } else {
                os.write(content.getBytes(encoding));
            }
            os.flush();
        } catch (IOException e) {
            LogUtils.e("FileUtil", "writeToSDCardFile:" + e.getMessage());
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * write data from inputstream to SDCard
     */
    public File writeToSDCardFromInput(String directory, String fileName,
                                       InputStream input) {
        File file = null;
        OutputStream os = null;
        try {
            if (!createDir(Environment.getExternalStorageDirectory()
                    + File.separator + directory)) {
                return file;
            }
            file = new File(Environment.getExternalStorageDirectory()
                    + File.separator + directory + fileName);
            os = new FileOutputStream(file);
            byte[] data = new byte[1024];
            int length = -1;
            while ((length = input.read(data)) != -1) {
                os.write(data, 0, length);
            }
            // clear cache
            os.flush();
        } catch (Exception e) {
            LogUtils.e("FileUtil", "" + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }


}
