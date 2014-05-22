package com.gnod.geekr.tool.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.gnod.geekr.app.AppConfig;
import com.gnod.geekr.tool.converter.GeekrViewConverter.IMAGE_MODEL;

public class SettingManager {
	public static final int QUALITY_HIGHT = 0;
	public static final int QUALITY_MID = 1;
	public static final int QUALITY_LOW = 2;
	public static final int QUALITY_AUTO = 3;
	private static final String UPLOAD_QUALITY = "Upload_Quality";
	
	public static final int PIC_LARGE = 0;
	public static final int PIC_SMALL = 1;
	public static final int PIC_AUTO = 2;
	private static final String PIC_MODEL = "Pic_Shown_Model";

	public static IMAGE_MODEL picModel;
	private static int mPicState = 2;
	
	public static void init(AppConfig config) {
		int model = getPicShowModel();
		setPicModelFlag(model);
	}
	
	public static int getUploadQuality() {
		SharedPreferences pref = getPreferences();
		return pref.getInt(UPLOAD_QUALITY, QUALITY_AUTO);
	}

	public static void setUploadQuality(int quality) {
		SharedPreferences pref = getPreferences();
		Editor editor = pref.edit();
		editor.putInt(UPLOAD_QUALITY, quality);
		editor.commit();
	}
	
	public static int getPicShowModel() {
		SharedPreferences pref = getPreferences();
		return pref.getInt(PIC_MODEL, PIC_SMALL);
	}

	public static void setPicShowModel(int q) {
		SharedPreferences pref = getPreferences();
		Editor editor = pref.edit();
		editor.putInt(PIC_MODEL, q);
		editor.commit();
		
		setPicModelFlag(q);
	}
	
	private static SharedPreferences getPreferences() {
		AppConfig config = AppConfig.getInstance();
		return config.getSharedPreferences();
	}

	private static void setPicModelFlag(int model) {
		mPicState = model;
		if(model == PIC_LARGE){
			picModel = IMAGE_MODEL.BIG;
		}else if(model == PIC_SMALL){
			picModel = IMAGE_MODEL.SMALL;
		}else if(model == PIC_AUTO) {
			picModel = AppConfig.getInstance().isWifi()? 
					IMAGE_MODEL.BIG: IMAGE_MODEL.SMALL;
		}
	}
	
	public static IMAGE_MODEL getPicModel() {
		return picModel;
	}
	
	public static void registNetworkStateReceiver(Context context) {
		IntentFilter filter  = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		context.registerReceiver(mNetworkStateReceiver, filter);
	}

	public static void unRegistNetworkStateReceiver(Context context) {
		context.unregisterReceiver(mNetworkStateReceiver);
	}
	
	private static BroadcastReceiver mNetworkStateReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(
					Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetInfo = mgr.getActiveNetworkInfo();
			if (activeNetInfo != null
					&& activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI
					&& mPicState == 2) {
				picModel = IMAGE_MODEL.BIG;
			} else if(mPicState == 2){
				picModel = IMAGE_MODEL.SMALL;
			}
		}
	};
}

