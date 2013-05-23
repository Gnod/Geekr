package com.gnod.geekr.model;

import java.io.Serializable;

public class UserInfoModel implements Serializable{

	public int type;	

	public String userID;
	public String nickName;
	public String iconURL;
	public String largeIconURL;
	
	public int province;
	public int city;
	public String location;
	public int verifiedType = -1; //-1 普通， 0：加v，220：微博达人，2， 3：机构认证
	public String verifiedReason;
	
	public String description;
	
	/**
	 * m:male, f:female, n:unknown
	 */
	public String gender; //m:male, f:female
	
	public String followersCount;
	public String friendsCount;
	public String statusCount;
	
	public boolean followed;
	
	/**
	 * @return 0:male, 1:female, 2:unknown
	 */
	public int getGender() {
		if(gender.equalsIgnoreCase("m")) {
			return 0;
		} else if(gender.equalsIgnoreCase("f")){
			return 1;
		} else {
			return 2;
		}
	}
}
