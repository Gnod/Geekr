package com.gnod.geekr.model;

import java.io.Serializable;
import java.util.Date;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.gnod.geekr.tool.StringUtils;
import com.gnod.geekr.tool.manager.AccountManager;

public class AccountModel implements Serializable{

	public int type;
	public int verifiedType = -1; //-1 普通， 0：加v，220：微博达人，2， 3：机构认证
	
	public String uID;
	public String token;
	public long expTime;
	
	public String iconURL;
	public String name;
	
	public String getTypeName(){
		switch (type) {
		case AccountManager.TYPE_SINA_WEIBO:
			return "Sina Weibo";
		}

		return "Unknown";
	}
}
