package com.gnod.geekr.tool.manager;

import java.util.ArrayList;
import java.util.HashMap;

import com.gnod.geekr.app.AppConfig;
import com.gnod.geekr.model.AccountModel;
import com.gnod.geekr.model.GroupsModel;

public class GroupsManager {

	private static final String GROUPS_PRIX = "Date_Groups";
	private HashMap<String, ArrayList<GroupsModel>> map = 
		new HashMap<String, ArrayList<GroupsModel>>();
	
	public ArrayList<GroupsModel> getGroupsList(AccountModel model) {
		String key = getKey(model.uID);
		if(map.containsKey(key))
			return map.get(key);
//		if(AppConfig.getInstance().isNetworkConnected()) 
//			return null;
		ArrayList<GroupsModel> list = getConfig().readObject(key);
		if(list != null)
			map.put(key, list);
		return list;
	}
	
	public void setGroupsList(AccountModel model, ArrayList<GroupsModel> list) {
		String key = getKey(model.uID);
		map.put(key, list);
		getConfig().writeObject(list, key);
	}
	
	public AppConfig getConfig() {
		return AppConfig.getInstance();
	}
	
	public String getKey(String uId) {
		return GROUPS_PRIX + uId;
	}
	
	
}
