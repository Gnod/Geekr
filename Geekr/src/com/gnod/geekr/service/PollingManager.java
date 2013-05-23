package com.gnod.geekr.service;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemClock;

import com.gnod.geekr.app.AppConfig;

public class PollingManager {

	public static boolean isPolling(AppConfig config) {
		SharedPreferences pref = config.getSharedPreferences();
		return pref.getBoolean("Polling_Checked", false);
	}
	
	public static void setPolling(AppConfig config, boolean checked) {
		SharedPreferences pref = config.getSharedPreferences();
		Editor edit = pref.edit();
		edit.putBoolean("Polling_Checked", checked);
		edit.commit();
		
		Intent intent = new Intent(config, PollingService.class);
		PendingIntent operation = PendingIntent.getService(config, 0, intent, 0);
		AlarmManager manager = (AlarmManager) config.getSystemService(Context.ALARM_SERVICE);
		manager.cancel(operation);
		
		if(checked) {
			long interval = getPollingInterval(config);
			long triggerAtTime = SystemClock.elapsedRealtime() + interval;
			manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
					triggerAtTime, interval, operation);
		}
	}
	
	public static long getPollingInterval(AppConfig config) {
		SharedPreferences pref = config.getSharedPreferences();
		return pref.getLong("Polling_Interval", 3 * 60 * 1000);
	}
	
	public static String formateInterval(AppConfig config) {
		long interval = getPollingInterval(config);
		String format = "";
		if(interval < 60000) {
			format = (interval / 1000) + "秒";
		} else if(interval < 3600000) {
			format = (interval / 60000) + "分钟";
		} else {
			format = (interval / 3600000)  + "小时";
		}
		
		return format;
	}
	
	public static void setPollingInterval(AppConfig config, long interval) {
		SharedPreferences pref = config.getSharedPreferences();
		Editor edit = pref.edit();
		edit.putLong("Polling_Interval", interval);
		edit.commit();
	}
	
	
	public static void checkPolling(AppConfig config) {
		if(isPolling(config)) {
			Intent intent = new Intent(config, PollingService.class);
			PendingIntent operation = PendingIntent.getService(config, 0, intent, 0);
			AlarmManager manager = (AlarmManager) config.getSystemService(Context.ALARM_SERVICE);
		
			long interval = getPollingInterval(config);
			long triggerAtTime = SystemClock.elapsedRealtime() + interval;
			manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
					triggerAtTime, interval, operation);
		}
	}

	/**
	 * 使用低4位标志提醒类型， 从最低位开始分别表示：
	 * 		1 评论
	 * 		2@我的微博
	 * 		3@我的评论
	 * 		4新粉丝
	 */
	public static int getPollingType(AppConfig config) {
		SharedPreferences pref = config.getSharedPreferences();
		return pref.getInt("Polling_Type", 15);
	}
	
	public static boolean isPollingNewComment(AppConfig config) {
		return getPrefBoolean(config, "Polling_Comment", true);
	}
	
	public static void setPollingNewComment(AppConfig config, boolean checked) {
		setPrefBoolean(config, "Polling_Comment", checked);
	}
	
	public static boolean isPollingAtMe(AppConfig config) {
		return getPrefBoolean(config, "Polling_AtMe", true);
	}
	
	public static void setPollingAtMe(AppConfig config, boolean checked) {
		setPrefBoolean(config, "Polling_AtMe", checked);
	}
	
	public static boolean isPollingCommentAtMe(AppConfig config) {
		return getPrefBoolean(config, "Polling_CommentAtMe", true);
	}
	
	public static void setPollingCommentAtMe(AppConfig config, boolean checked) {
		setPrefBoolean(config, "Polling_CommentAtMe", checked);
	}
	
	public static boolean isPollingNewFans(AppConfig config) {
		return getPrefBoolean(config, "Polling_NewFollow", true);
	}
	
	public static void setPollingNewFans(AppConfig config, boolean checked) {
		setPrefBoolean(config, "Polling_NewFollow", checked);
	}
	
	public static boolean isPollingAvoidNightDistrubed(AppConfig config) {
		return getPrefBoolean(config, "Polling_AvoidNight", true);
	}
	
	public static void setPollingAvoidNightDistrubed(AppConfig config, boolean checked) {
		setPrefBoolean(config, "Polling_AvoidNight", checked);
	}
	
	public static String getPollingSpecialPersonName(AppConfig config) {
		SharedPreferences pref = config.getSharedPreferences();
		return pref.getString("Polling_SpecialPersonName", "");
	}

	public static void setPollingSpecialPerson(AppConfig config, String userName) {
		SharedPreferences pref = config.getSharedPreferences();
		Editor editor = pref.edit();
		editor.putString("Polling_SpecialPersonName", userName);
		editor.commit();
	}
	
	private static boolean getPrefBoolean(AppConfig config, String key, boolean defValue) {
		SharedPreferences pref = config.getSharedPreferences();
		return pref.getBoolean(key, defValue);
	}
	
	private static void setPrefBoolean(AppConfig config, String key, boolean checked) {
		SharedPreferences pref = config.getSharedPreferences();
		Editor editor = pref.edit();
		editor.putBoolean(key, checked);
		editor.commit();
	}
	
	private static void clearNotification(AppConfig config, int id) {
		NotificationManager manager = (NotificationManager)config.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(id);
	}
}
