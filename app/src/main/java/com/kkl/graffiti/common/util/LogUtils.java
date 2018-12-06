package com.kkl.graffiti.common.util;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 日志工具类
 * 
 */
@Deprecated
public class LogUtils {

	private static final String TAG = "KKL";
	
	/**
	 * 过滤类型：等级
	 * */
	public static final int FILTER_TYPE_LEVEL = 1;
	
	/**
	 * 过滤类型：模块
	 * */
	public static final int FILTER_TYPE_MODULE = 2;
	
	/**
	 * 打印类型：控制台
	 * */
	public static final int PRINTER_TYPE_CONSOLE = 1;
	
	/**
	 * 打印类型：文件
	 * */
	public static final int PRINTER_TYPE_FILE = 2;
	
	/**
	 * 日志等级:  0
	 * */
	public static final int VERBOSE = 0;
	
	/**
	 * 日志等级:  1
	 * */
	public static final int DEBUG = 1;
	
	/**
	 * 日志等级:  2
	 * */
	public static final int INFO = 2;
	
	/**
	 * 日志等级:  3
	 * */
	public static final int WARN = 3;
	
	/**
	 * 日志等级:  4
	 * */
	public static final int ERROR = 4;
	
	/**
	 * 日志等级:  5
	 * */
	public static final int ASSERT = 5;
	
	private static final String[] LEVEL_TOKEN = {
		"V", "D", "I", "W", "E", "A"
	};
	
	private static long FILESIZE_DEFAULT = 1024 * 1024 * 2;
	
	private static int FILENUMBER_DEFAULT = 50000;

	private static int TIME_INTERVAL_UPLOAD_LOG = 60 * 60;//单位秒

	private static int TIMER_OCLOCK_UPLOAD_LOG = 22;//24小时计时
	
	private static final String CONFIG_KEY_FILTERTYPE = "filter_type";
	private static final String CONFIG_KEY_PRINTERTYPE = "printer_type";
	private static final String CONFIG_KEY_LOGLEVEL = "log_level";
	private static final String CONFIG_KEY_FILESIZE = "file_size";
	private static final String CONFIG_KEY_FILENUMBER = "file_number";
	private static final String CONFIG_KEY_MODULEFILTER = "module_filter";
	private static final String CONFIG_KEY_INTERVAL_UPLOAD_LOG = "interval_time_upload";
	private static final String CONFIG_KEY_TIMER_OCLOCK_UPLOAD_LOG = "timer_oclock_upload";
	private static final String CONFIG_KEY_EXTRA_TAGS = "extra_tags";

	private static int filterType = FILTER_TYPE_MODULE;
	private static int printerType = PRINTER_TYPE_CONSOLE;
	private static int logLevel = VERBOSE;
	private static long fileSize = FILESIZE_DEFAULT;
	private static int fileNumber = FILENUMBER_DEFAULT;
	private static long timeIntervalUpload = TIME_INTERVAL_UPLOAD_LOG;
	private static int timerOclock = TIMER_OCLOCK_UPLOAD_LOG;

	private static AtomicReference<String> sLogFileName = new AtomicReference<>(null);
	
	/**
	 * 模块过滤列表
	 * */
	private static List<String> moduleFilterList = new ArrayList<String>();
	
	/**
	 * 文件日志队列
	 * */
	private static LinkedBlockingQueue<String> fileLogQueue = new LinkedBlockingQueue<String>();
	
	private static FilePrintThread filePrintThread = null;
	
	private static SimpleDateFormat logDateTimeFormate = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
	private static SimpleDateFormat logFileDateTimeFormate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSS");
	
	
	private static String logDir;
	private static boolean isProfguard = false;
	private static List<String> sExtraTags = null;

	public static String getLogFilePath(){
        return logDir;
    }

    public static String getCurrentLogFilePath(){
        return sLogFileName.get();
    }
	
	public static void init(Context context) {
		String appRootDir = Environment.getExternalStorageDirectory() + File.separator + context.getPackageName();
		if(!createAndCheckDir(appRootDir)){
			appRootDir = context.getFilesDir().getAbsolutePath();
		}
		logDir = appRootDir + File.separator + "log";
		createAndCheckDir(logDir);
		initConfig();
	}
	
	private static void initConfig() {
		String configFilePath = getConfigFilePath();
		File configFile = new File(configFilePath);
		boolean isConfigFileExists = configFile.exists();
		if (!isConfigFileExists) {
			FileUtils.createFile(configFilePath);
		}
		Properties props=new Properties();
		try {
			String level = VERBOSE+"";
			String filterType = FILTER_TYPE_MODULE+"";
			String printerType = PRINTER_TYPE_FILE+"";
			String fileNumber = FILENUMBER_DEFAULT + "";
			String fileSize = FILESIZE_DEFAULT + "";
			String moduleFilters = "";
			String timeIntervalUpload = TIME_INTERVAL_UPLOAD_LOG + "";
			String timerOclockUpload = TIMER_OCLOCK_UPLOAD_LOG + "";
			if (!isConfigFileExists) {
				props.setProperty(CONFIG_KEY_LOGLEVEL, level);
				props.setProperty(CONFIG_KEY_FILTERTYPE, filterType);
				props.setProperty(CONFIG_KEY_PRINTERTYPE, printerType);
				props.setProperty(CONFIG_KEY_FILENUMBER, fileNumber);
				props.setProperty(CONFIG_KEY_FILESIZE, fileSize);
				props.setProperty(CONFIG_KEY_MODULEFILTER, moduleFilters);
				props.setProperty(CONFIG_KEY_INTERVAL_UPLOAD_LOG, timeIntervalUpload);
				props.setProperty(CONFIG_KEY_TIMER_OCLOCK_UPLOAD_LOG, timerOclockUpload);
				props.store(new FileOutputStream(configFile), "");
			} else {
				props.load(new FileInputStream(configFile));
				level = props.getProperty(CONFIG_KEY_LOGLEVEL, level);
				filterType = props.getProperty(CONFIG_KEY_FILTERTYPE, filterType);
				printerType = props.getProperty(CONFIG_KEY_PRINTERTYPE, printerType);
				fileNumber = props.getProperty(CONFIG_KEY_FILENUMBER, fileNumber);
				fileSize = props.getProperty(CONFIG_KEY_FILESIZE, fileSize);
				moduleFilters = props.getProperty(CONFIG_KEY_MODULEFILTER, moduleFilters);
				timeIntervalUpload = props.getProperty(CONFIG_KEY_INTERVAL_UPLOAD_LOG, timeIntervalUpload);
				timerOclockUpload = props.getProperty(CONFIG_KEY_TIMER_OCLOCK_UPLOAD_LOG, timerOclockUpload);

				String extraTags = props.getProperty(CONFIG_KEY_EXTRA_TAGS, "");//其值为空或者短划线分隔的字符串。
				if (!TextUtils.isEmpty(extraTags)) {
					try {
						sExtraTags = Arrays.asList(extraTags.split("-"));
					} catch (Exception ex) {
						Log.d(TAG, "parse extra tags err!");
					}
				}
			}
			setLevel(level);
			setFilterType(filterType);
			setPrinterType(printerType);
			setFileNumber(fileNumber);
			setFileSize(fileSize);
			stringToModuleFilterList(moduleFilters);
			setTimeIntervalUploadLog(timeIntervalUpload);
			setTimerOclockUploadLog(timerOclockUpload);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getConfigFilePath(){
		return logDir + File.separator + "log.cfg";
	}

	private static File getConfigFile(){
		String configFilePath = getConfigFilePath();
		File configFile = new File(configFilePath);
		boolean isConfigFileExists = configFile.exists();
		if (!isConfigFileExists) {
			FileUtils.createFile(configFilePath);
		}
		return configFile;
	}
	
	public static void setFilterType(int type) {
		filterType = type;
	}
	
	private static void setFilterType(String type) {
		Integer t = FILTER_TYPE_MODULE;
		try {
			t = Integer.valueOf(type);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		setFilterType(t);
	}
	
	public static void setPrinterType(int type) {
		printerType = type;
		if (printerType == PRINTER_TYPE_FILE) {
			startFilePrintThread();
		} else {
			stopFilePrintThread();
		}
	}

	public static int changePrinterType() {
		if(printerType == PRINTER_TYPE_CONSOLE){
			printerType = PRINTER_TYPE_FILE;
			startFilePrintThread();
		}else if(printerType == PRINTER_TYPE_FILE){
			printerType = PRINTER_TYPE_CONSOLE;
			stopFilePrintThread();
		}

		Properties props=new Properties();
		String configFilePath = getConfigFilePath();
		File configFile = new File(configFilePath);
		try {
			props.load(new FileInputStream(configFile));
			props.setProperty(CONFIG_KEY_PRINTERTYPE, String.valueOf(printerType));
			props.store(new FileOutputStream(configFile), "");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return printerType;
	}

	public static boolean isFilePrint(){
		return PRINTER_TYPE_FILE == printerType;
	}

	public static boolean isConsolePrint(){
		return PRINTER_TYPE_CONSOLE == printerType;
	}
	
	private static void setPrinterType(String type) {
		Integer t = PRINTER_TYPE_CONSOLE;
		try {
			t = Integer.valueOf(type);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		setPrinterType(t);
	}

	public static void setLevel(int level) {
		logLevel = level;
	}
	
	private static void setLevel(String level) {
		Integer l = VERBOSE;
		try {
			l = Integer.valueOf(level);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		setLevel(l);
	}

	public static void setTimeIntervalUploadLog(String interval){
		long time = TIME_INTERVAL_UPLOAD_LOG;
		try {
			time = Long.valueOf(interval);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		setTimeIntervalUploadLog(time);
	}

	public static void setTimeIntervalUploadLog(long interval){
		timeIntervalUpload = interval;
	}

	public static long getTimeIntervalUploadLog(){
		return timeIntervalUpload;
	}

	public static void setTimerOclockUploadLog(String oclock){
		int time = TIMER_OCLOCK_UPLOAD_LOG;
		try {
			time = Integer.valueOf(oclock);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		setTimerOclockUploadLog(time);
	}

	public static void setTimerOclockUploadLog(int oclock){
		timerOclock = oclock;
	}

	public static int getTimerOclockUploadLog(){
		return timerOclock;
	}
	
	public static void setFileSize(long size) {
		fileSize = size;
	}
	
	private static void setFileSize(String size) {
		long s = FILESIZE_DEFAULT;
		try {
			s = Long.valueOf(size);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		setFileSize(s);
	}

	public static long getFileSize(){
		return FILESIZE_DEFAULT;
	}
	
	public static void setFileNumber(int number) {
		fileNumber = number;
	}
	
	private static void setFileNumber(String number) {
		Integer num = FILENUMBER_DEFAULT;
		try {
			num = Integer.valueOf(number);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		setFileNumber(num);
	}
	
	public static void addModuleFilter(String module) {
		if (!StringUtils.stringEmpty(module) && !hasModuleFilter(module)) {
			moduleFilterList.add(module);
		}
	}
	
	public static void removeModuleFilter(String module) {
		if (!StringUtils.stringEmpty(module)) {
			moduleFilterList.remove(module);
		}
	}
	
	private static boolean hasModuleFilter(String module) {
		boolean flag = false;
		for (String mf : moduleFilterList) {
			if (mf.equalsIgnoreCase(module)) {
				flag = true;
				break;
			}
		}
		return flag;
	}
	
	private static void stringToModuleFilterList(String str) {
		moduleFilterList.clear();
		if (!StringUtils.stringEmpty(str)) {
			String[] modules = str.split(";");
			for (String m : modules) {
				moduleFilterList.add(m);
			}
		}
	}
	
	//JNI日志打印接口
	public static void printLog(int level, String tag, String msg) {
		boolean print = true;
		if (filterType == FILTER_TYPE_LEVEL) {
			if (level < logLevel) {
				print = false;
			}
		} else if (filterType == FILTER_TYPE_MODULE) {
			print = !hasModuleFilter(tag);
		}
		
		if (!print) {
			return;
		}

		//如果是VERBOSE，始终不会打印到文件
		if (level == VERBOSE || printerType == PRINTER_TYPE_CONSOLE) {
			consolePrint(level, tag, msg);
		} else if (printerType == PRINTER_TYPE_FILE) {
			filePrint(level, tag, msg);
		}
	}
	
	private static void consolePrint(int level, String tag, String msg) {
		switch (level) {
			case VERBOSE:
				Log.v(tag, msg);
				break;
			case DEBUG:
				Log.d(tag, msg);
				break;
			case INFO:
				Log.i(tag, msg);
				break;
			case WARN:
				Log.w(tag, msg);
				break;
			case ERROR:
				Log.e(tag, msg);
				break;
			case ASSERT:
				Log.i(tag, msg);
				break;
			default:
				break;
		}
	}
	
	private static void filePrint(int level, String tag, String msg) {
		String log = formatFileLog(level, tag, msg);
		fileLogQueue.add(log);
	}
	
	public static String formatFileLog(int level, String tag, String msg) {
		String time = logDateTimeFormate.format(new Date());
		String log = String.format("%s: %s/%s: %s\r\n", time, LEVEL_TOKEN[level], tag, msg);
		return log;
	}
	
	private static void startFilePrintThread() {
		if (filePrintThread != null) {
			stopFilePrintThread();
		}
		filePrintThread = new FilePrintThread();
		filePrintThread.start();
	}
	
	public static void stopFilePrintThread() {
		if (filePrintThread != null) {
			filePrintThread.quit = true;
			filePrintThread.interrupt();
		}
		filePrintThread = null;
	}
	
	private static boolean createAndCheckDir(String dir){
		if(!FileUtils.isExist(dir)){
			if(!FileUtils.createDir(dir)){
				return false;
			}
		}
		return true;
	}
	
	private static class FilePrintThread extends Thread {
		
		protected volatile boolean quit = false;
		protected File logFile = null;
		protected FileOutputStream fos = null;
		protected BufferedOutputStream bos = null; 
		
		public FilePrintThread() {}

        @Override
		public void run() {
			while(true) {
				try {
					String log = fileLogQueue.take();
					try {
						write(log.getBytes("utf-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				} catch (InterruptedException e) {
					release();
					if (quit) {
	                    return;
	                }
	                continue;
				}
			}
			
		}
		
		private void write(byte[] buffer) {
			if (bos == null || logFile == null || !logFile.exists()) {
				release();
				String path = getLogFilePath();
				try {
					logFile = new File(path);
					fos = new FileOutputStream(logFile, true);
					bos = new BufferedOutputStream(fos, 1024);

					sLogFileName.set(path);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			
			if (bos == null) {
				return;
			}
			
			try {
				bos.write(buffer);
				bos.flush();
				if (logFile.length() >= fileSize) {
					release();
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
		
		/***
		 * 获取日志文件路径
		 */
		private String getLogFilePath() {
			String path = null;
			File file = getLastModifiedLogFile();
			
			if (file != null && file.length() < fileSize) {
				path = file.getAbsolutePath();
			}
			
			if (path == null) {
				checkLogFileNumber();
//				long time = System.currentTimeMillis();
				String time = logFileDateTimeFormate.format(new Date());
				path = logDir + File.separator + time + ".log";
				FileUtils.createFile(path);
			}
			
			return path;
		}
		
		/***
		 * 获取所有日志文件
		 */
		private File[] getAllLogFiles() {
			File[] files = new File(logDir).listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					String extensionName = getFileExtensionName(pathname.getAbsolutePath());
					if (extensionName != null && extensionName.equals("log") && !pathname.getName().startsWith("error")) {
						return true;
					}
					return false;
				}
			});
			if (files == null || files.length == 0) {
				return null;
			}
			return files;
		}
		
		/***
		 * 检测日志文件数量
		 */
		private void checkLogFileNumber() {
			while(true) {
				File[] files = getAllLogFiles();
				if (files == null) {
					return;
				}
				int count = files.length;
				if (count > 0 && count >= fileNumber) {
					deleteLRMLogFile(files);
				} else {
					return;
				}
			}
		}
		
		/***
		 * 获取最后修改的日志文件
		 */
		private File getLastModifiedLogFile() {
			File[] files = getAllLogFiles();
			if (files == null) {
				return null;
			}
			
			long lastModifiedTime = files[0].lastModified();
			File logFile = files[0];
			for (File f : files) {
				try {
					long time = f.lastModified();
					if (time > lastModifiedTime) {
						lastModifiedTime = time;
						logFile = f;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			return logFile;
		}
		
		/***
		 * 删除最近最少修改的日志文件  The least recently modified
		 */
		private void deleteLRMLogFile(File[] files) {
			if (files == null) {
				return;
			}
			
			long lastModifiedTime = files[0].lastModified();
			File logFile = files[0];
			for (File f : files) {
				try {
					long time = f.lastModified();
					if (time < lastModifiedTime) {
						lastModifiedTime = time;
						logFile = f;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if (logFile != null) {
				logFile.delete();
			}
		}
		
		private String getFileName(String pathname) {
			String extensionName = null;
			int index = pathname.indexOf('\\');
			int end = pathname.lastIndexOf('.');
			if (end > 0) {
				extensionName = pathname.substring(index + 1, end);
			}
			return extensionName;
		}
		
		private String getFileExtensionName(String pathname) {
			String extensionName = null;
			int index = pathname.lastIndexOf('.');
			if (index > 0) {
				extensionName = pathname.substring(index + 1);
			}
			return extensionName;
		}
		
		private void release() {
			try {
                if (bos != null){
                	bos.flush();
                    bos.close();
                    bos = null;
                }
                if (fos != null) {
                	fos.close();
                	fos = null;
                }
                if (logFile != null) {
                	logFile = null;
                }
            } catch (IOException e) {
            }
			finally {
				sLogFileName.set(null);
			}
		}


	}

	/**
	 * 取得类名和行号
	 * 
	 * @param 
	 * @return
	 */
	public static String getFunctionName() {
		if(isProfguard) return "";

		StackTraceElement[] sts = Thread.currentThread().getStackTrace();

		if (sts == null) {
			return null;
		}

		for (StackTraceElement st : sts) {
			if (st.isNativeMethod()) {
				continue;
			}

			if (st.getClassName().equals(Thread.class.getName())) {
				continue;
			}
			if (st.getClassName().equals(LogUtils.class.getName())) {
				continue;
			}

			return  "("+ st.getFileName()
					+ ":" + st.getLineNumber() + ")";
		}

		return null;
	}

	public static void e(String msg) {
		printLog(ERROR, TAG, getFunctionName() + "-----" + msg);
	}

	public static void e(String tag, String msg) {
		printLog(ERROR, tag, getFunctionName() + "-----" + msg);
	}

	public static void e(String tag, String msg, Throwable throwable) {
		printLog(ERROR, tag, getFunctionName() + "-----" + msg + "-----Throwable------" + throwable.toString());
	}

	public static void i(String msg) {
		printLog(INFO, TAG, msg);
	}

	public static void i(String tag, String msg) {
		printLog(INFO, tag, msg);
	}

	public static void i(String tag, String msg, Throwable throwable) {
		printLog(INFO, tag, "-----Throwable------" + throwable.toString());
	}

	public static void d(String tag, String msg) {
		printLog(DEBUG, tag, getFunctionName() + "-----" + msg);
	}

	public static void d(String msg) {
		printLog(DEBUG, TAG, getFunctionName() + "-----" + msg);
	}

	public static void v(String msg) {
		printLog(VERBOSE, TAG, getFunctionName() + "-----" + msg);
	}

	public static void v(String tag, String msg) {
		printLog(VERBOSE, tag, getFunctionName() + "-----" + msg);
	}
	
	public static void w(String msg) {
		printLog(WARN, TAG, getFunctionName() + "-----" + msg);
	}

	public static void w(String tag, String msg) {
		printLog(WARN, tag, getFunctionName() + "-----" + msg);
	}

	public static void w(Throwable throwable) {
		printLog(WARN, TAG, getFunctionName() + "---Throwable---" + throwable.toString());
	}

	public static void w(String tag, Throwable throwable) {
		printLog(WARN, tag, getFunctionName() + "---Throwable---" + throwable.toString());
	}

	public static void tr(String tag, String msg) {
		if (null == sExtraTags || !sExtraTags.contains(tag)) return;

		d(tag, msg);
	}

    public static void setExtraTags(@NonNull String tags) {
        if (TextUtils.isEmpty(tags)) {
            tags = "";
        } else {
            try {
                sExtraTags = Arrays.asList(tags.split("-"));
            } catch (Exception ex) {
                Log.d(TAG, "parse extra tags err!");
            }
        }

        String configFilePath = getConfigFilePath();
        File configFile = new File(configFilePath);

        Properties props = new Properties();
        try {
            props.load(new FileInputStream(configFile));
            props.setProperty(CONFIG_KEY_EXTRA_TAGS, tags);
            props.store(new FileOutputStream(configFile), "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasExtraTag(@NonNull String tag) {
        return null != sExtraTags && sExtraTags.contains(tag);
    }

	static {
		isProfguard = !LogUtils.class.getName().contains("LogUtils");
	}
}
