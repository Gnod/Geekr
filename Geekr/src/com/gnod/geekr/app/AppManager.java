package com.gnod.geekr.app;

import java.util.Stack;

import com.gnod.geekr.tool.manager.SettingManager;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

/**
 * Activity 管理类
 * @author Gnod 
 * 
 */
public class AppManager {

	private static Stack<Activity> activityStack;
	private static AppManager instance;
	
	private AppManager(){
	}
	
	public static AppManager getInstance() {
		if(instance == null)
			instance = new AppManager();
		return instance;
	}
	
	public void addActivity(Activity ac) {
		if(activityStack == null)
			activityStack = new Stack<Activity>();
		activityStack.add(ac);
	}
	
	public Activity getCurrentActivity(){
		if(activityStack == null)
			return null;
		return activityStack.lastElement();
	}
	
	public void finishActivity(Activity ac) {
		if(activityStack != null && ac != null){
			activityStack.remove(ac);
			ac.finish();
			ac = null;
		}
	}
	
	public void finishActivity() {
		if(activityStack != null) {
			Activity ac = activityStack.lastElement();
			finishActivity(ac);
		}
	}
	
	public void finishActivity(Class<?> cls) {
		if(activityStack == null)
			return;
		for(Activity ac: activityStack){
			if(ac.getClass().equals(cls)){
				finishActivity(ac);
			}
		}
	}
	
	public void finishAllActivity(){
		if(activityStack == null)
			return;
		Activity ac;
		for(int i = 0, size = activityStack.size(); i < size; i ++) {
			ac = activityStack.get(i);
			if(ac != null) {
				ac.finish();
			}
		}
		activityStack.clear();
	}
	
	public void appExit(Context context) {
		try {
			finishAllActivity();
			System.exit(0);
		} catch (Exception e) {
			Log.e("AppManager", "exit error:" + e.getMessage());
		}
	}
}
