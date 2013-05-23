package com.gnod.geekr.tool;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.Toast;

import com.gnod.geekr.R;
import com.gnod.geekr.app.AppConfig;
import com.gnod.geekr.app.AppManager;
import com.gnod.geekr.widget.NotifiedToast;

public class ToastHelper {
	
	/**
	 * @param type 指定背景颜色。0:蓝色背景， 1：灰色背景， 2：红色背景
	 */
	public static void show(final String content, final int type, final Boolean isBottom)
	{
		if(content == null)
			return;	
		
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				innerShow(content, type, isBottom);		
			}
		});
	}

	/**
	 * @param type 指定背景颜色。0:蓝色背景， 1：灰色背景， 2：红色背景
	 */
	public static void show(final String content, final int type)
	{
		if(content == null)
			return;	
		
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				innerShow(content, type, false);		
			}
		});
	}
	
	public static void show(final String content)
	{
		if(content == null)
			return;	
		
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast t = Toast.makeText(AppConfig.getAppContext(), content, 
						Toast.LENGTH_SHORT);
				t.show();
			}
		});
	}	

	
	public static void innerShow(String content, int type, Boolean isBottom)
	{		
		Context cx = AppConfig.getAppContext();
		Toast t = NotifiedToast.makeText(cx, content, type, false);
		if(isBottom){
			t.setGravity(Gravity.BOTTOM, 0, 0);
		}
		t.show();
		
	}
}
