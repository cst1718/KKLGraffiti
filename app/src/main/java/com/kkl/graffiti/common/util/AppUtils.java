package com.kkl.graffiti.common.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * App 相关工具类包括判断Service 是否启动、运行，Activity是否启动 App 是否安装，快捷方式创建
 * 
 */
public class AppUtils {

	/**
	 * 获取最多100个当前正在运行的服务，放进ArrList里，以目前的手机性能看，100足够， 后期手机发展了可调节改数值
	 */
	public static int MAX_SERVICE_CHECK = 100;
	/**
	 * 获取最多1000个当前正在运行的任务，放进ArrList里 以目前的手机性能看，100足够， 后期手机发展了可调节改数值
	 */
	public static int MAX_TASK_CHECK = 1000;

	/**
	 * 判断本应用的service是否运行
	 * 
	 * 
	 * @param appContext
	 *            应用上下文
	 * @return
	 */
	public static boolean isServiceRunning(Context appContext) {
		ActivityManager myManager = (ActivityManager) appContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> myServiceList = myManager
				.getRunningServices(MAX_SERVICE_CHECK);
		for (RunningServiceInfo info : myServiceList) {
			if (appContext.getPackageName().equals(
					info.service.getPackageName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断特定的service是否在运行
	 *
	 * @param appContext
	 *            应用上下文
	 * @param serviceName
	 *            服务名（包括包名和类名)
	 * @return
	 */
	public static boolean isServiceRunning(Context appContext,
			String serviceName) {
		ActivityManager myAM = (ActivityManager) appContext
				.getSystemService(Context.ACTIVITY_SERVICE);

		ArrayList<RunningServiceInfo> runningServices = (ArrayList<RunningServiceInfo>) myAM
				.getRunningServices(MAX_SERVICE_CHECK);
		for (int i = 0; i < runningServices.size(); i++) { // 循环枚举对比
			String className4i = runningServices.get(i).service.getClassName();
			// Logger.i("XMPPClient serviceName: ", className4i);
			if (className4i.equals(serviceName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断本应用的activity是否启动
	 *
	 * @param appContext
	 *            应用上下文
	 * @return
	 */
	public static boolean isAcvitityStart(Context appContext) {
		ActivityManager myManager = (ActivityManager) appContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> myActivityList = myManager
				.getRunningTasks(MAX_TASK_CHECK);
		for (ActivityManager.RunningTaskInfo info : myActivityList) {
			if (appContext.getPackageName().equals(
					info.baseActivity.getPackageName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断特定的activity是否启动
	 *
	 * @param appContext
	 *            应用上下文
	 * @param activityName
	 *            Activity名（包括包名和类名)
	 * @return
	 */
	public static boolean isAcvitityStart(Context appContext,
			String activityName) {
		ActivityManager myManager = (ActivityManager) appContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> myActivityList = myManager
				.getRunningTasks(MAX_TASK_CHECK);
		if (myActivityList != null && myActivityList.size() > 0) {
			return (myActivityList.get(0).topActivity).getShortClassName()
					.endsWith(activityName);
		}
		return false;
	}

	/**
	 * 判断特定的activity是否存活
	 *
	 * @param appContext
	 *            应用上下文
	 * @param activityName
	 *            Activity名（包括包名和类名)
	 * @return
	 */
	public static boolean isAcvitityAlive(Context appContext,
			String activityName) {
		ActivityManager myManager = (ActivityManager) appContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> myActivityList = myManager
				.getRunningTasks(MAX_TASK_CHECK);

		for (ActivityManager.RunningTaskInfo taskInfo : myActivityList) {
			LogUtils.d("AppUtils", taskInfo.baseActivity.getShortClassName());
			if (taskInfo.baseActivity.getShortClassName()
					.endsWith(activityName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 检查应用是否安装
	 *
	 * @param appContext
	 *            应用上下文
	 * @param packageName
	 *            应用包名
	 * @return
	 */
	public static boolean isAppInstalled(Context appContext, String packageName) {
		PackageInfo packageInfo;
		try {
			packageInfo = appContext.getPackageManager().getPackageInfo(
					packageName, 0);
		} catch (NameNotFoundException e) {
			packageInfo = null;
		}
		if (packageInfo != null) {
			return true;
		}
		return false;
	}

	/**
	 * 获取APK的包名
	 *
	 * @param appContext
	 *            应用上下文
	 * @param apkPath
	 *            apk文件路径
	 * @return
	 */
	public static String getApkPackageName(Context appContext, String apkPath) {
		PackageManager pm = appContext.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(apkPath,
				PackageManager.GET_ACTIVITIES);
		if (info != null) {
			ApplicationInfo appInfo = info.applicationInfo;
			if (appInfo != null) {
				return appInfo.packageName;
			}
		}
		return null;
	}

	/**
	 * 通过包名获取版本号
	 *
	 * @param appContext
	 *            应用上下文
	 * @param packageName
	 *            应用包名
	 * @return
	 */
	public static String getApkVersion(Context appContext, String packageName) {
		if (appContext.getPackageName().equalsIgnoreCase(packageName)) {
			return getApkVersion(appContext);
		}
		String versionName = null;
		PackageManager pm = appContext.getPackageManager();
		PackageInfo packageInfo;
		try {
			packageInfo = pm.getPackageInfo(packageName, 0);
			if (packageInfo != null) {
				versionName = packageInfo.versionName;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionName;
	}

	/**
	 * 通过包名获取版本号
	 *
	 * @param appContext
	 *            应用上下文
	 * @param packageName
	 *            应用包名
	 * @return
	 */
	public static int getApkVersionCode(Context appContext, String packageName) {
		if (!appContext.getPackageName().equalsIgnoreCase(packageName)) {
			return -1;
		}

		int verCode = -1;
		PackageManager pm = appContext.getPackageManager();
		PackageInfo packageInfo;
		try {
			packageInfo = pm.getPackageInfo(packageName, 0);
			if (packageInfo != null) {
				verCode = packageInfo.versionCode;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return verCode;
	}

	/**
	 * 通过本应用版本号
	 *
	 * @param appContext
	 *            应用上下文
	 * @return
	 */
	public static String getApkVersion(Context appContext) {
		String versionName = null;
		PackageManager pm = appContext.getPackageManager();
		PackageInfo packageInfo;
		try {
			packageInfo = pm.getPackageInfo(appContext.getPackageName(), 0);
			if (packageInfo != null) {
				versionName = packageInfo.versionName;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionName;
	}

	/**
	 * 获取APP语言
	 * @param context
	 * @return
	 */
	public static String getAppLang(Context context) {
		return Locale.getDefault().getLanguage();

	}

	/**
	 * 获取APP语言 格式 zh-cn
	 * @param context
	 * @return
	 */
	public static String getAppLanguage(Context context) {
		return Locale.getDefault().getLanguage()+"-"+Locale.getDefault().getCountry().toLowerCase(Locale.getDefault());
	}

	/**
	 * 创建快捷方式
	 *
	 * @param appContext
	 *            应用上下文
	 * @param appName
	 *            应用名称，一般对应R.string.app_name
	 * @param iconResId
	 *            应用icon Id, 一般对应R.drawable.icon
	 * @param launcherCompCls
	 *            为此组件对应的类创建快捷方式, 一般对应SplashActivity.class
	 */
	public static void createDeskShortCut(Context appContext, String appName,
			int iconResId, Class<?> launcherCompCls) {

		Intent shortcutIntent = new Intent(
				"com.android.launcher.action.INSTALL_SHORTCUT");
		shortcutIntent.putExtra("duplicate", false);
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName);

		Parcelable icon = Intent.ShortcutIconResource.fromContext(appContext,
				iconResId);

		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
		Intent intent = new Intent(appContext, launcherCompCls);

		intent.setAction("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.LAUNCHER");

		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
		appContext.sendBroadcast(shortcutIntent);
	}

	/**
	 * 删除快捷方式
	 *
	 * @param appContext
	 *            应用上下文
	 * @param packageName
	 *            应用包名
	 * @param appName
	 *            应用名称，一般对应R.string.app_name
	 * @param launcherCompName
	 *            删除此组件对应的快捷方式, 一般对应"SplashActivity"
	 *
	 */
	public static void delShortcut(Context appContext, String packageName,
			String appName, String launcherCompName) {
		Intent shortcut = new Intent(
				"com.android.launcher.action.UNINSTALL_SHORTCUT");
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName);
		ComponentName comp = new ComponentName(packageName, launcherCompName);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(
				Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
				.setComponent(comp));

		appContext.sendBroadcast(shortcut);
	}

	/**
	 * 判断快捷方式是否已经创建
	 *
	 * @param context
	 * @return
	 */
	public static boolean isInstallShortcut(Context context, String appName) {
		boolean isInstallShortcut = false;
		final ContentResolver cr = context.getContentResolver();
		final String AUTHORITY = "com.android.launcher.settings";
		final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
				+ "/favorites?notify=true");
		Cursor c = cr.query(CONTENT_URI,
				new String[] { "title", "iconResource" }, "title=?",
				new String[] { appName }, null);// XXX表示应用名称,需要判断什么就修改成什么
		if (c != null && c.getCount() > 0) {
			isInstallShortcut = true;
		}
		c.close();
		return isInstallShortcut;
	}

	public static void installApp(Context context, String apkFile) {

		Intent intent = new Intent(Intent.ACTION_VIEW);
		Uri data;
		File file = new File(apkFile);
		// 判断版本大于等于7.0
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			// "net.csdn.blog.ruancoder.fileprovider"即是在清单文件中配置的authorities
			data = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
			// 给目标应用一个临时授权
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		} else {
			data = Uri.fromFile(file);
		}
		intent.setDataAndType(data, "application/vnd.android.package-archive");


		context.startActivity(intent);
	}

	/**
	 * 判断app在前台还是后台
	 *
	 * @param context
	 * @return
	 */
	public static boolean isBackground(Context context) {

		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcesses = activityManager
				.getRunningAppProcesses();
		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(context.getPackageName())) {
				if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
					Log.i("后台", appProcess.processName);
					return true;
				} else {
					Log.i("前台", appProcess.processName);
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * activity跳转
	 *
	 * @param context
	 * @param action
	 */
	public static void startHintActivity(Context context, String action) {
		Intent intent = new Intent();
		intent.setAction(action);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	/**
	 * 重新启动App
	 */
	public static void reStartApp(Context context) {
		Intent launch = context.getPackageManager().getLaunchIntentForPackage(
				context.getPackageName());
		launch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(launch);
	}

	/**
	 * 检测当前版本是否需要更新
	 *
	 * @param oldAppVersion
	 *            旧版本
	 * @param newAppVersion
	 *            新版本
	 * @return
	 */
	public static boolean checkVersionUpgrade(String oldAppVersion,
			String newAppVersion) {
		if (!StringUtils.stringEmpty(oldAppVersion)
				&& !StringUtils.stringEmpty(newAppVersion)) {
			if (oldAppVersion.equals(newAppVersion)) {
				return false;
			}
			if (newAppVersion.compareTo(oldAppVersion) > 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 调用系统Wifi设置的界面
	 */
	public static void openSystemWifiSetting(Context context) {
		if (Build.VERSION.SDK_INT > 10) {
			// 3.0以上打开设置界面
			context.startActivity(new Intent(
					android.provider.Settings.ACTION_WIFI_SETTINGS));
		} else {
			context.startActivity(new Intent(
					android.provider.Settings.ACTION_WIRELESS_SETTINGS));
		}
	}

	public static String getCurProcessName(Context context) {
		int pid = android.os.Process.myPid();
		ActivityManager mActivityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningAppProcessInfo appProcess : mActivityManager
				.getRunningAppProcesses()) {
			if (appProcess.pid == pid) {
				return appProcess.processName;
			}
		}
		return null;
	}
}
