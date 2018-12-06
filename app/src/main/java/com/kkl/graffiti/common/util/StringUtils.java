package com.kkl.graffiti.common.util;

import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtils {
	private static final String TAG = "StringUtils";

	/**
	 * 是否为null或空字符串
	 */
	public static boolean isEmpty(String str) {
		return TextUtils.isEmpty(str);
	}

	/**
	 * 判断是字符串是否没有（空或者"")
	 *
	 * @param aData
	 * @return
	 */
	public static boolean stringEmpty(String aData) {
		if (null == aData || "".equals(aData) || "".equals(aData.trim())) {
			return true;
		}
		return false;
	}

	/**
	 * 检测邮箱地址是否合法
	 *
	 * @param email
	 * @return
	 */
	public static Boolean emailFormat(String email) {
		boolean tag = true;
		final String pattern1 = "^[\\w.\\-]+@(?:[a-z0-9]+(?:-[a-z0-9]+)*\\.)+[a-z]{2,6}$";
		final Pattern pattern = Pattern.compile(pattern1);
		final Matcher mat = pattern.matcher(email);
		if (!mat.find()) {
			tag = false;
		}
		return tag;
	}

	/**
	 * 检测手机号码是否合法
	 *
	 * @param phone
	 * @return
	 */
	public static Boolean phoneFormat(String phone) {
//		Pattern pattern = Pattern
//				.compile("(^1[3|4|5|7|8]\\d{9}$)|(^\\++\\d{2,}$)");
		Pattern pattern = Pattern
				.compile("^1\\d{10}$");
		Matcher matcher = pattern.matcher(phone);
		if (matcher.matches() && phone.length() == 11) {
			return true;
		}
		return false;
	}

	/**
	 * 判断密码是否安全
	 */
	public static boolean isPasswordSafe(String password) {
		Pattern pattern1 = Pattern.compile("[^\\x00-\\x7f]+");
		Matcher matcher1 =pattern1.matcher(password);
		if(matcher1.find()){
			return  false;
		}

		if(password.matches("^\\d+$")){
			return false;
		}

		if(password.matches("^[A-Za-z]+$")){
			return false;
		}

		if(password.matches("^[^A-Za-z0-9]+$")){
			return false;
		}

		if(!password.matches("[^\\s]+")){
			return false;
		}
		return true;
	}

	/**
	 * 判断是否是数字
	 */
	public static boolean isNumber(String str) {
		String reg = "[0-9]+";
		return str.matches(reg);
	}

	/**
	 * 是否为中文
	 *
	 * @param c
	 * @return
	 */
	private static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	/**
	 * 是否包含了汉字
	 *
	 * @param str
	 * @return
	 */
	public static boolean isContainChinese(String str) {
		boolean cf = false;
		for (int i = 0; i < str.length(); i++) {
			cf = isChinese(str.charAt(i));
			if (cf)
				break;
		}
		return cf;
	}

	/**
	 * 字符串转为整数(如果转换失败,则返回 defaultValue)
	 */
	public static int stringToInt(String str, int defaultValue) {
		if (isEmpty(str)) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(str.trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 字符串转为long(如果转换失败,则返回defaultValue)
	 */
	public static long stringToLong(String str, long defaultValue) {
		if (isEmpty(str)) {
			return defaultValue;
		}
		try {
			return Long.parseLong(str.trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 字体串转为boolean (如果转换失败,则返回false)
	 */
	public static boolean stringToBoolean(String str) {
		if (isEmpty(str)) {
			return false;
		}
		try {
			return Boolean.parseBoolean(str.trim());
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * boolean转为字体串
	 */
	public static String booleanToString(Boolean bool) {
		String booleanString = "false";
		if (bool) {
			booleanString = "true";
		}
		return booleanString;
	}

	/**
	 * 把arraylist转换成string
	 *
	 * @param lists
	 * @param splitChar
	 * @return
	 */
	public static String arrayListToString(List<?> lists,
			String splitChar) {
		StringBuffer buffer = new StringBuffer(32 * 1024);
		if (lists != null && lists.size() > 0) {
			for (Object o : lists) {
				buffer.append(object2string(o));
				buffer.append(splitChar);
			}
		}
		return buffer.toString();
	}

	/**
	 * 把string转换成list
	 *
	 * @param text
	 * @param splitChar
	 *            分隔字符串
	 * @return
	 */
	public static List<String> stringToList(String text, String splitChar) {
		if (isEmpty(text)) {
			return null;
		}
		return Arrays.asList(text.split(splitChar));
	}

	/**
	 * 从异常中获取调用栈
	 */
	public static String getExceptionStackTrace(Throwable ex) {
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		return writer.toString();
	}

	/**
	 * 将字符串多空格，多换行替换成一个空格
	 */
	public static String tirmString(String content) {
		if (StringUtils.isEmpty(content)) {
			return "";
		}

		return content.replaceAll("[ \n\r\t]+", " ");
	}

	/**
	 * 判断是否存在自定义的特殊字符,其他特殊字符可以往里面加
	 */
	public static boolean isErrorCodeStr(String str) {

		return str.contains("\\") || str.contains("/") || str.contains(":")
				|| str.contains("*") || str.contains("?") || str.contains("\"")
				|| str.contains("<") || str.contains(">") || str.contains("|");
	}

	/**
	 * 对象转换为字符串
	 */
	public static String object2string(Object object) {
		if (null == object) {
			return "null";
		}
		StringBuffer sb = new StringBuffer();

		if ((object instanceof String) || (object instanceof Number)
				|| (object instanceof Map) || (object instanceof Collection)) {
			sb.append(object);
		} else {
			ArrayList<Field> fieldList = new ArrayList<Field>();
			for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass
					.getSuperclass()) {
				fieldList.addAll(Arrays.asList(superClass.getDeclaredFields()));
			}
			try {
				for (Field field : fieldList) {
					sb.append(field.getName()).append(':');
					sb.append(
							ReflectUtil.getFieldValue(object, field.getName()))
							.append(',');
				}
				if (sb.length() > 0) {
					sb.deleteCharAt(sb.length() - 1);
				}
			} catch (Exception e) {
				LogUtils.e(TAG, "object2string error.", e);
			}

		}

		return sb.toString();
	}

	/**
	 * 去掉url中多余的斜杠
	 */
	public static String fixUrl(String url) {
		if (null == url) {
			return "";
		}
		StringBuffer stringBuffer = new StringBuffer(url);
		for (int i = stringBuffer.indexOf("//", stringBuffer.indexOf("//") + 2); i != -1; i = stringBuffer
				.indexOf("//", i + 1)) {
			stringBuffer.deleteCharAt(i);
		}
		return stringBuffer.toString();
	}

	/**
	 * 替换字符串中特殊字符,其他特殊字符可以往里面加
	 */
	public static String encodeString(String strData) {
		if (strData == null) {
			return "";
		}
		return strData.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;").replaceAll("'", "&apos;")
				.replaceAll("\"", "&quot;");
	}

	public static String intArrayToString(int[] data) {
		StringBuilder sbResult = new StringBuilder();
		sbResult.append(data[0]);
		int length = data.length;
		for (int i = 1; i < length; i++) {
			sbResult.append(",");
			sbResult.append(data[i]);
		}
		return sbResult.toString();
	}

	public static int[] stringToIntArray(String data) {
		String[] codeArray = data.trim().split(",");
		int length = codeArray.length;
		int[] resArray = new int[length];
		for (int i = 0; i < length; i++) {
			resArray[i] = Integer.valueOf(codeArray[i].trim());
		}
		return resArray;
	}

	/**
	 * 将字符串转换成UTF-8
	 *
	 * @param str
	 * @return
	 */
	public static String ConverStrToUtf(String str) {
		if (!StringUtils.stringEmpty(str)) {
			try {
				return new String(str.getBytes(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return str;
			}
		}
		return null;
	}

	public static boolean containString(String[] list, String s) {
		if (null == list || null == s)
			return false;

		for (int i = 0; i < list.length; i++) {
			if (s.equals(list[i]))
				return true;
		}

		return false;
	}

	public static Map<String, String> truncateParam(String uri) {

		try {
			Map<String, String> paramMap = new HashMap<String, String>();
			String[] response = new String[2];
			String param = uri.substring(uri.indexOf("?"));
			response = param.split("&");
			for (String strSplit : response) {
				String[] arrEqual = null;
				arrEqual = param.split("=");
				if (arrEqual.length > 1) {
					paramMap.put(arrEqual[0], arrEqual[1]);
				}
			}

			return paramMap;
		} catch (Exception e) {
			return null;
		}
	}

	public static String hexStr2Str(String hexStr) {
		String str = "0123456789ABCDEF";
		char[] hexs = hexStr.toCharArray();
		byte[] bytes = new byte[hexStr.length() / 2];
		int n;
		for (int i = 0; i < bytes.length; i++) {
			n = str.indexOf(hexs[2 * i]) * 16;
			n += str.indexOf(hexs[2 * i + 1]);
			bytes[i] = (byte) (n & 0xff);
		}
		return new String(bytes);
	}

	// 转化字符串为十六进制编码
	public static String toHexString(String s) {
		String str = "";
		for (int i = 0; i < s.length(); i++) {
			int ch = (int) s.charAt(i);
			String s4 = Integer.toHexString(ch);
			str = str + s4;
		}
		return str;
	}

	// 转化十六进制编码为字符串
	public static String toStringHex1(String s) {
		byte[] baKeyword = new byte[s.length() / 2];
		for (int i = 0; i < baKeyword.length; i++) {
			try {
				baKeyword[i] = (byte) (0xff & Integer.parseInt(
						s.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			// s = new String(baKeyword, "utf-8");// UTF-16le:Not
			s = new String(baKeyword);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return s;
	}

	// 转化十六进制编码为字符串
	public static String toStringHex2(String s) {
		byte[] baKeyword = new byte[s.length() / 2];
		for (int i = 0; i < baKeyword.length; i++) {
			try {
				baKeyword[i] = (byte) (0xff & Integer.parseInt(
						s.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			s = new String(baKeyword, "utf-8");// UTF-16le:Not
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return s;
	}

	/*
	 * 16进制数字字符集
	 */
	private static String hexString = "0123456789ABCDEF";

	/*
	 * 将字符串编码成16进制数字,适用于所有字符（包括中文）
	 */
	public static String encode(String str) {
		// 根据默认编码获取字节数组
		byte[] bytes = str.getBytes();
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		// 将字节数组中每个字节拆解成2位16进制整数
		for (int i = 0; i < bytes.length; i++) {
			sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
			sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
		}
		return sb.toString();
	}

	/*
	 * 将16进制数字解码成字符串,适用于所有字符（包括中文）
	 */
	public static String decode(String bytes) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(
				bytes.length() / 2);
		// 将每2位16进制整数组装成一个字节
		for (int i = 0; i < bytes.length(); i += 2)
			baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString
					.indexOf(bytes.charAt(i + 1))));
		return new String(baos.toByteArray());
	}

	/**
	 * 64. * bytes转换成十六进制字符串 65.
	 */
	public static byte[] hexStr2Bytes(String src) {
		int m = 0, n = 0;
		int l = src.length() / 2;
//		System.out.println(l);
		byte[] ret = new byte[l];
		for (int i = 0; i < l; i++) {
			m = i * 2 + 1;
			n = m + 1;
			ret[i]=uniteBytes(src.substring(i * 2, m), src.substring(m, n));
		}
		return ret;
	}

	public static String byte2hex(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1) {
				hs = hs + "0" + stmp;
			} else {
				hs = hs + stmp;
			}
		}
		return hs.toUpperCase();
	}


	private static byte uniteBytes(String src0, String src1) {
		byte b0 = Byte.decode("0x" + src0).byteValue();
		b0 = (byte) (b0 << 4);
		byte b1 = Byte.decode("0x" + src1).byteValue();
		byte ret = (byte) (b0 | b1);
		return ret;
	}

}
