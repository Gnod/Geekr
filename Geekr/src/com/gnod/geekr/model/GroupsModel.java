package com.gnod.geekr.model;

import java.io.Serializable;
import java.util.Date;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.gnod.geekr.tool.StringUtils;
import com.gnod.geekr.tool.manager.AccountManager;

public class GroupsModel implements Serializable{

	public int position;
	public String id;
	public String name;
	public String mode;
	
	public int memberCount;
	
	public static GroupsModel getModel(int pos, String name) {
		GroupsModel model = new GroupsModel();
		model.position = pos;
		model.name = name;
		return model;
	}
}
