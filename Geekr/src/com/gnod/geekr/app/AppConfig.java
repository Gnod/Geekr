package com.gnod.geekr.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.DisplayMetrics;

import com.gnod.geekr.tool.FileUtils;
import com.gnod.geekr.tool.ImageHelper;
import com.gnod.geekr.tool.WeiboBaseTool;
import com.gnod.geekr.tool.StringUtils;
import com.gnod.geekr.tool.manager.AccountManager;
import com.gnod.geekr.tool.manager.DrawableManager;
import com.gnod.geekr.tool.manager.SettingManager;
import com.gnod.geekr.tool.manager.StatusManager;
import com.gnod.geekr.ui.activity.TimeLineActivity;

public class AppConfig extends Application {
	
	private final String APP_PREFERENCE = "com.gnod.preference";

	public static final int NETWORK_NONE = 0;
	public static final int NETWORK_WIFI = 1;
	public static final int NETWORK_CMNET = 2;
	public static final int NETWORK_CMWAP = 3;
	
	private static AppConfig mContext;
	public static DisplayMetrics mDisplay;
	public static boolean mFetchImage = true;
	private static DrawableManager mDrawableMgr;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		mDisplay = mContext.getResources().getDisplayMetrics();
		init();
	}
	
	private void init() {
		WeiboBaseTool.init(this);
		AccountManager.init(this);
		StatusManager.init(this);
		SettingManager.init(this);
		mFetchImage = isImgFetch();
		
		ImageHelper.initEmotion(this);
	}
	
	//判断当前网络是否为wifi
    public boolean isWifi() {  
 	   ConnectivityManager connectivityManager = 
 		   (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);  
 	   NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();  
 	   if (activeNetInfo != null  && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI){  
     	        return true;  
     	 }  
     return false;  
    }
	
	public static AppConfig getInstance() {
        return mContext;
    }


	public static Context getAppContext() {
		return mContext;
	}
	
	public static DrawableManager getDrawableManager() {
		if(mDrawableMgr == null) 
			mDrawableMgr = new DrawableManager();
		return mDrawableMgr;
	}
	
	public SharedPreferences getSharedPreferences(int mode) {
		SharedPreferences pref = getSharedPreferences(APP_PREFERENCE, mode);
		return pref;
	}
	
	public SharedPreferences getSharedPreferences() {
		SharedPreferences pref = getSharedPreferences(APP_PREFERENCE, Context.MODE_APPEND);
		return pref;
	}
	
	public boolean isAutoFetch() {
		SharedPreferences pref = getSharedPreferences();
		return pref.getBoolean("AutoFetch", true);
	}
	
	public void setAutoFetch(boolean checked) {
		SharedPreferences pref = getSharedPreferences();
		Editor editor = pref.edit();
		editor.putBoolean("AutoFetch", checked);
		editor.commit();
	}
	
	public boolean isShowSplash() {
		SharedPreferences pref = getSharedPreferences();
		return pref.getBoolean("ShowSplash", true);
	}
	
	public void setShowSplash(boolean checked) {
		SharedPreferences pref = getSharedPreferences();
		Editor editor = pref.edit();
		editor.putBoolean("ShowSplash", checked);
		editor.commit();
	}
	
	public String getImgPath() {
		SharedPreferences pref = getSharedPreferences();
		String dir = Environment.getExternalStorageDirectory().getPath();

		return dir + File.separator + pref.getString("ImageSaveFolder", "Geekr");
	}

	public String getImgFolder() {
		SharedPreferences pref = getSharedPreferences();
		return pref.getString("ImageSaveFolder", "Geekr");
	}

	public void setImgFolder(String path) {
		SharedPreferences pref = getSharedPreferences();
		Editor editor = pref.edit();
		editor.putString("ImageSaveFolder", path);
		editor.commit();
	}
	
	public boolean isImgFetch() {
		SharedPreferences pref = getSharedPreferences();
		return pref.getBoolean("ImageFetch", true);
	}
	
	public void setImgFetch(boolean checked) {
		SharedPreferences pref = getSharedPreferences();
		Editor editor = pref.edit();
		editor.putBoolean("ImageFetch", checked);
		editor.commit();
	}
	
	public boolean isShowMenuAnim() {
		SharedPreferences pref = getSharedPreferences();
		return pref.getBoolean("MenuAnim", true);
	}
	
	public void setMenuAnim(boolean checked) {
		SharedPreferences pref = getSharedPreferences();
		Editor editor = pref.edit();
		editor.putBoolean("MenuAnim", checked);
		editor.commit();
		TimeLineActivity.isShowMenuAnimation = checked;
	}
	
	public boolean isNetworkConnected() {
		NetworkInfo info = getNetworkInfo();
		return info != null && info.isConnectedOrConnecting();
	}
	
	public NetworkInfo getNetworkInfo() {
		ConnectivityManager manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		return manager.getActiveNetworkInfo();
	}
	
	public int getNetworkType() {
		NetworkInfo info = getNetworkInfo();
		if(info == null) {
			return NETWORK_NONE;
		}
		int type = info.getType();
		if(type == ConnectivityManager.TYPE_WIFI) {
			return NETWORK_WIFI;
		}else if(type == ConnectivityManager.TYPE_MOBILE) {
			String extraInfo = info.getExtraInfo();
			if(StringUtils.isNullOrEmpty(extraInfo))
				return NETWORK_NONE;
			if(extraInfo.equalsIgnoreCase("cmnet")) {
				return NETWORK_CMNET;
			} else {
				return NETWORK_CMWAP;
			}
		}
		return NETWORK_NONE;
	}
	
	public boolean writeObject(Serializable ser, String file) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try{
			fos = openFileOutput(file, MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(ser);
			oos.flush();
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}finally{
			try {
				oos.close();
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public <T extends Serializable> T readObject(String file){
		if(!isFileExist(file))
			return null;
		
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try{
			fis = openFileInput(file);
			ois = new ObjectInputStream(fis);
			return (T)ois.readObject();
		}catch(FileNotFoundException e){
		}catch(Exception e){
			e.printStackTrace();
			if(e instanceof InvalidClassException){
				File data = getFileStreamPath(file);
				data.delete();
			}
		}finally{
			try {
				ois.close();
			} catch (Exception e) {}
			try {
				fis.close();
			} catch (Exception e) {}
		}
		return null;
	}
	
	public File getCustomCacheDir() {
		File dir = mContext.getFilesDir();
		File cacheDir = new File(dir, "CacheObjects" + File.separator);
		if(!cacheDir.exists()){
			cacheDir.mkdir();
		}
		return cacheDir;
	}
	
	public boolean saveObjectCache(Serializable ser, String fileName) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try{
			File dir = getCustomCacheDir();
			File file = new File(dir, fileName);
			fos = new FileOutputStream(file);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(ser);
			oos.flush();
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}finally{
			try {
				oos.close();
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public <T extends Serializable> T readObjectCache(String fileName){
		File dir = getCustomCacheDir();
		File file = new File(dir, fileName);
		if(!file.exists())
			return null;
		
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try{
			fis = new FileInputStream(file);
			ois = new ObjectInputStream(fis);
			return (T)ois.readObject();
		}catch(FileNotFoundException e){
		}catch(Exception e){
			e.printStackTrace();
			if(e instanceof InvalidClassException){
				file.delete();
			}
		}finally{
			try {
				ois.close();
			} catch (Exception e) {}
			try {
				fis.close();
			} catch (Exception e) {}
		}
		return null;
	}
	
	public String getObjectCacheSize() {
		File cacheDir = getCustomCacheDir();
		long cacheSize = FileUtils.getDirSize(cacheDir);
		return FileUtils.formatSize(cacheSize);
	}
	
	public void clearObjectCache(){
		File cacheDir = getCustomCacheDir();
		int count = FileUtils.deleteDir(cacheDir, System.currentTimeMillis());
	}
	
	private boolean isFileExist(String path)
	{
		File file = getFileStreamPath(path);
		if(file.exists())
			return true;
		return false;
	}
}
