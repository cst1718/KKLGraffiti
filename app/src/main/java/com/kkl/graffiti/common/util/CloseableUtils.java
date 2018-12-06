package com.kkl.graffiti.common.util;

import java.io.Closeable;
import java.io.IOException;

public class CloseableUtils {

	public static void close(Closeable c) {
		if(null != c) {
			try {
				c.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
