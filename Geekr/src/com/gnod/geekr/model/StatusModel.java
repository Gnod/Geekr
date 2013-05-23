package com.gnod.geekr.model;

import java.io.Serializable;
import java.util.Date;

import com.gnod.geekr.tool.StringUtils;

public class StatusModel implements Serializable{

	public int type;	

	public String ID;
	public String content;
	
	public String imageURL;
	public String midImageURL;
	public String fullImageURL;	
	
	public Date time;
	public String source;
	public String commentCount;
	public String retweetCount;
	
	public UserInfoModel userInfo;
	public StatusModel retweetItem;

	public CharSequence getCommentCount() {
		return StringUtils.isNullOrEmpty(commentCount)? "0" : commentCount;
	}
	
	public CharSequence getRetweetCount() {
		return StringUtils.isNullOrEmpty(retweetCount)? "0" : retweetCount;
	}
}
