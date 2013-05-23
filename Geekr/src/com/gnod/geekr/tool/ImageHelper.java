package com.gnod.geekr.tool;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.gnod.geekr.R;
import com.gnod.geekr.tool.manager.SettingManager;
import com.weibo.sdk.android.util.Utility;

public class ImageHelper {

	public static Map<String,String> emotionsMap = new HashMap<String,String>();
	
	public static Bitmap getBitmap(Context context,String fileName) {
		FileInputStream fis = null;
		Bitmap bitmap = null;
		try {
			String filepath = context.getFilesDir() + File.separator + fileName;
			File file = new File(filepath);
			if(file.exists()){
				fis = context.openFileInput(fileName);
				bitmap = BitmapFactory.decodeStream(fis);
			}
		} catch (FileNotFoundException e) {
			Log.e("getImage", e.getMessage());
		} catch (OutOfMemoryError e) {
			Log.e("getImage", e.getMessage());
		}finally{
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
		return bitmap;
	}
	
	public static void saveImage(Context context, String fileName, Bitmap bitmap, int quality) throws IOException 
	{ 
		if(bitmap==null || fileName==null || context==null)	return;		

		FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, quality, stream);
		byte[] bytes = stream.toByteArray();
		fos.write(bytes);
		stream.close();
		fos.flush();
		fos.close();
	}
	
	
	public static String getCacheSize(Context context) {
		if(context == null) return "";
		
		File dir = context.getFilesDir();
		File cacheDir = new File(dir, "CacheImage" + File.separator);
		if(!cacheDir.exists()){
			return "";
		}
		long cacheSize = FileUtils.getDirSize(cacheDir);
		return FileUtils.formatSize(cacheSize);
	}
	
	/**
	 * 缓存删除, 建议开辟线程调用该方法。
	 */
	public static void clearCache(Context context) {
		if(context == null) return;
		
		File dir = context.getFilesDir();
		File cacheDir = new File(dir, "CacheImage" + File.separator);
		if(!cacheDir.exists())	return;
		
		FileUtils.deleteDir(cacheDir, System.currentTimeMillis());
	}
	
	public static void saveCacheImage(Context context, 
			String fileName, Bitmap bitmap, int quality) throws IOException 
	{ 
		if(bitmap==null || fileName==null || context==null)	return;		
		File dir = context.getFilesDir();
		File cacheDir = new File(dir, "CacheImage" + File.separator);
		if(!cacheDir.exists()){
			cacheDir.mkdir();
		}
		File file = new File(cacheDir, fileName);
		FileOutputStream fos = new FileOutputStream(file);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, quality, stream);
		byte[] bytes = stream.toByteArray();
		fos.write(bytes);
		stream.close();
		fos.flush();
		fos.close();
	}
	
	public static Bitmap getCacheImage(Context context,String fileName) {
		FileInputStream fis = null;
		Bitmap bitmap = null;
		try {
			File dir = context.getFilesDir();
			File cacheDir = new File(dir, "CacheImage" + File.separator);
			if(cacheDir.exists()){
				File file = new File(cacheDir, fileName);
				if(file.exists()){
					fis = new FileInputStream(file);
					bitmap = BitmapFactory.decodeStream(fis);
				}
			}
		} catch (FileNotFoundException e) {
			Log.e("CacheImage", e.getMessage());
		} catch (OutOfMemoryError e) {
			Log.e("CacheImage", e.getMessage());
		}finally{
			try {
				if(fis != null)
					fis.close();
			} catch (Exception e) {
				Log.e("CacheImage", e.getMessage());
			}
		}
		return bitmap;
	}
	
	public static String compressImage(Context context, String picPath) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = false;
		opt.inSampleSize = 1;
		
		switch (SettingManager.getUploadQuality()) {
		case SettingManager.QUALITY_HIGHT:
			return picPath;
		case SettingManager.QUALITY_MID:
			opt.inSampleSize = 2;
			break;
		case SettingManager.QUALITY_LOW:
			opt.inSampleSize = 4;
			break;
		case SettingManager.QUALITY_AUTO:
			if(Utility.isWifi(context))
				return picPath;
			opt.inSampleSize = 2;
			break;
		}
		
		Bitmap bitmap = BitmapFactory.decodeFile(picPath, opt);
		FileOutputStream output = null;
		String tempFilePath = getTempUploadFile(context);
		
		try {
			new File(tempFilePath).getParentFile().mkdirs();
			new File(tempFilePath).createNewFile();
			output = new FileOutputStream(new File(tempFilePath));
		} catch (IOException e) {
		}
		
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
		if(output != null) {
			try {
				output.close();
				bitmap.recycle();
			} catch (IOException e) {
			}
		}
		return tempFilePath;
	}

	private static String getTempUploadFile(Context context) {
		File dir = context.getFilesDir();
		File cacheDir = new File(dir, "CacheImage" + File.separator);
		return cacheDir.getAbsolutePath() + File.separator + "upload.jpg";
	}

	public static void initEmotion(Context context) {
		InputStream is;
		try {
			is = context.getAssets().open("smiley_des.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuilder sbr = new StringBuilder();
			String str;
			while((str = br.readLine())!= null) {
				sbr.append(str);
			}
			prepareFaceMap(sbr.toString());
		} catch (IOException e) {
			Log.e("error", e.getMessage());
		}
	}
	
	private static void prepareFaceMap(String arg) {
		try {
			JSONObject root = new JSONObject(arg);
			JSONArray array = root.getJSONArray("smileyList");
			if(array == null || array.length() == 0)
				return;
			for(int i=0; i < array.length(); i ++) {
				JSONObject pair = array.getJSONObject(i);
				emotionsMap.put(pair.optString("smileyString"), pair.optString("fileName"));
			}
			
		} catch (JSONException e) {
			Log.e("error", e.getMessage());
		}
	}
	
	public static void setVerifiedImage(ImageView view, int type){
		if(type == 3 || type == 2 || type == 7){
			view.setVisibility(View.VISIBLE);
			view.setImageResource(R.drawable.ic_verified_blue);
		} else if(type == 220){
			view.setVisibility(View.VISIBLE);
			view.setImageResource(R.drawable.ic_daren);
		} else if(type == 0){
			view.setVisibility(View.VISIBLE);
			view.setImageResource(R.drawable.ic_verified);
		} else {
			view.setVisibility(View.GONE);
		}
	}
}
