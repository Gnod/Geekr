package com.gnod.geekr.tool;

public class StringUtils {

	public static boolean isNullOrEmpty(String str) {
		return str == null || str.trim().equalsIgnoreCase("");
	}

}
