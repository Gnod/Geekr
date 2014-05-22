package com.gnod.geekr.tool.manager;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.gnod.geekr.app.AppConfig;
import com.gnod.geekr.model.AccountModel;

public class AccountManager {
	public static final int TYPE_SINA_WEIBO = 1;
	
	public static final String CACHE_ACCOUNTS = "Data_Accounts";
	
	public static ArrayList<AccountModel> mAccounts;
	
	private static AppConfig mAppConfig;
	
	public static void init(AppConfig config) {
		mAppConfig = config;

		mAccounts = config.readObject(CACHE_ACCOUNTS);
		if(mAccounts == null) {
			mAccounts = new ArrayList<AccountModel>();
			setActivityIndex(-1);
		}
	}
	
	public static void addAccount(AccountModel accountModel) {
		for(AccountModel model: mAccounts) {
			if(model.uID.equalsIgnoreCase(accountModel.uID)){
				mAccounts.remove(model);
			}
		}
		setActivityIndex(mAccounts.size());
		mAccounts.add(accountModel);
		mAppConfig.writeObject(mAccounts, CACHE_ACCOUNTS);
	}
	
	public static void updateActivityAccount(AccountModel accountModel) {
		int curAccount = getActivityIndex();
		mAccounts.set(curAccount, accountModel);
		mAppConfig.writeObject(mAccounts, CACHE_ACCOUNTS);
	}
	
	public static void removeAccount(int pos) {
		int curAccount = getActivityIndex();
		mAccounts.remove(pos);
		mAppConfig.writeObject(mAccounts, CACHE_ACCOUNTS);
		//前面remove后accouts 大小减了1
		if(curAccount == pos && pos == mAccounts.size()){
			setActivityIndex(curAccount - 1);
		}
	}
	
	public static int getActivityIndex() {
		SharedPreferences pref = mAppConfig.getSharedPreferences();
		int index = pref.getInt("Current_Account_Index", -1);
		if(index >= mAccounts.size()) {
			index = (mAccounts.size() > 0)? 0 : -1;
		}
		return index;
	}
	
	public static AccountModel getActivityAccount(){
		int index = getActivityIndex();
		if(index < 0){
			return null;
		}
		return mAccounts.get(index);
	}
	
	public static void setActivityIndex(int index){
		SharedPreferences pref = mAppConfig.getSharedPreferences();
		Editor editor = pref.edit();
		editor.putInt("Current_Account_Index", index);
		editor.commit();
	}
	
	public static AccountModel getAccount(int index){
		if(index > mAccounts.size() - 1) 
			return null;
		return mAccounts.get(index);
	}

	public static ArrayList<AccountModel> getAccounts() {
		return mAccounts;
	}
	
	public static int getSize() {
		return mAccounts.size();
	}
}
