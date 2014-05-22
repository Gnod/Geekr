package com.gnod.geekr.tool.manager;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.StrictMode;
import android.os.Build.VERSION_CODES;

public class Utils {

	private Utils() {};
	
	public static boolean hasFroyo() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.FROYO;
	}
	
	public static boolean hasGingerbread() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD;
	}
	
	public static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;
	}
	
	public static boolean hasHoneycombMR1() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR1;
	}
	
	public static boolean hasJellyBean() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;
	}
	
	public static boolean hasKitKat() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT;
	}
	
	/**
	 * method for testing log 
	 */
	@TargetApi(VERSION_CODES.HONEYCOMB)
	public static void enableStrictMode() {
		
		if (Utils.hasGingerbread()) {
			StrictMode.ThreadPolicy.Builder threadPolicyBuilder = 
					new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog();
			StrictMode.VmPolicy.Builder vmPolicyBuilder = 
					new StrictMode.VmPolicy.Builder().detectAll().penaltyLog();
			
			if (Utils.hasHoneycomb()) {
				threadPolicyBuilder.penaltyFlashScreen();
			}
			
			StrictMode.setThreadPolicy(threadPolicyBuilder.build());
			StrictMode.setVmPolicy(vmPolicyBuilder.build());
		}
	}
}
