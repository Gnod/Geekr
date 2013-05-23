package com.gnod.geekr.tool.manager;

import java.util.ArrayList;
import java.util.HashMap;

import com.gnod.geekr.app.AppConfig;
import com.gnod.geekr.model.AccountModel;
import com.gnod.geekr.model.StatusModel;
import com.gnod.geekr.tool.StringUtils;

public class StatusManager {
	public static final String CACHE_SINA_WEIBO = "Cache_SinaWeibo";

	private static AppConfig appConfig;
	private static HashMap<String, ArrayList> statusMap;
	
	/**
	 * 标记fetch 类型， 1：所有status， 2：@ 我的status
	 */
	public static int mStatusType = 0;
	
	/**
	 * 0: All, 1: 相互关注/我关注的人, 2... group
	 */
	public static int mGroupType = 0;
	
	public static void init(AppConfig config) {
		appConfig = config;
		statusMap = new HashMap<String, ArrayList>();
	}
	
	public static  ArrayList<StatusModel> getCacheStatus(String type, AccountModel account) {
		if(StringUtils.isNullOrEmpty(type) || account == null)
			return new ArrayList<StatusModel>();
		String key = getKey(type, account);
		
		if(!statusMap.containsKey(key)){
			ArrayList<StatusModel> list = appConfig.readObjectCache(
					CACHE_SINA_WEIBO + "_" + key);
			if(list == null) {
				list = new ArrayList<StatusModel>();
			} 
			statusMap.put(key, list);
		}
		return statusMap.get(key);
	}
	
	public static void cacheStatus(String type, AccountModel account, ArrayList<StatusModel> list) {
		if(StringUtils.isNullOrEmpty(type) || account == null)
			return;
		
		String key = getKey(type, account);
		if(StringUtils.isNullOrEmpty(key))
			return;
		
		if(!statusMap.containsKey(key)) {
			statusMap.put(key, list);
		}
		//存缓存
		appConfig.saveObjectCache(list, CACHE_SINA_WEIBO + "_" + key);
	}
	
	public static void setSingleStatus(String type, AccountModel account, int position, StatusModel model) {
		if(StringUtils.isNullOrEmpty(type) || account == null)
			return;
		String key = getKey(type, account);
		
		ArrayList<StatusModel> list = getCacheStatus(type, account);
		if(position >= list.size())
			return;
		list.set(position, model);
	}
	
	public static void setCacheStatus(ArrayList<StatusModel> list) {
		AccountModel account = AccountManager.getActivityAccount();
		cacheStatus(getCacheTag(), account, list);
	}
	
	public static ArrayList<StatusModel> getCacheStatus() {
		AccountModel account = AccountManager.getActivityAccount();
		return getCacheStatus(getCacheTag(), account);
	}
	
	private static String getKey(String type, AccountModel account) {
		return type + "_" + account.uID;
	}
	
	public static String getCacheTag() {
		return mStatusType + "_" + mGroupType;
	}
}
